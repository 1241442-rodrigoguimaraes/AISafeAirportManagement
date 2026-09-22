#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <sys/wait.h>
#include <sys/stat.h>
#include <pthread.h>
#include <dirent.h>
#include <sys/mman.h>
#include <semaphore.h>
#include <fcntl.h>
#include "simulation_types.h"
#include "utils.h"
#include "parser.h"
#include "flight_runner.h"
#include "safety_monitor.h"
#include "shared_state.h"
#include "report_writer.h"
#include "weather.h"

#define FLIGHT_PLAN_DIR "./flight_plans/"
#define FLIGHT_PLAN_EXTENSION ".fp"
#define M_PI 3.14159265358979323846

int global_step_pipe_fd = -1;

int main(int argc, char *argv[]) {
    printf("\n### Simulation Started ###\n\n");
    fflush(stdout);
    signal(SIGPIPE, SIG_IGN);

    DIR *d;
    struct dirent *dir;
    FlightPlan* plans = NULL;
    int num_plans = 0;
    int stored_violation_count = 0;

    if (argc > 1) {
        plans = realloc(plans, sizeof(FlightPlan));
        plans[0] = parse_fp_flight_plan(argv[1]);
        num_plans = 1;
    } else {
        d = opendir(FLIGHT_PLAN_DIR);
        if (!d) {
            perror("Fail opening directory.");
            return EXIT_FAILURE;
        }

        while ((dir = readdir(d)) != NULL) {
            if (has_extension(dir->d_name, FLIGHT_PLAN_EXTENSION)) {
                char path[MAX_PATH_LENGTH];
                snprintf(path, sizeof(path), "%s%s", FLIGHT_PLAN_DIR, dir->d_name);
                plans = realloc(plans, sizeof(FlightPlan) * (num_plans + 1));
                plans[num_plans] = parse_fp_flight_plan(path);
                num_plans++;
            }
        }
        closedir(d);
    }

    if (num_plans == 0) return EXIT_SUCCESS;

    int shm_fd = shm_open("/shm_simulation", O_CREAT | O_RDWR, S_IRUSR | S_IWUSR);
    ftruncate(shm_fd, sizeof(SharedSimulationArea));
    SharedSimulationArea *shared_data = mmap(NULL, sizeof(SharedSimulationArea), PROT_READ | PROT_WRITE, MAP_SHARED, shm_fd, 0);
    sem_t *sem_mem = sem_open("/sem_simulation_mem", O_CREAT, 0644, 1);

    sem_wait(sem_mem);
    shared_data->total_flights = num_plans;
    shared_data->weather.wind_direction_deg = 0.0;
    shared_data->weather.wind_speed_ms = 0.0;
    shared_data->weather.time_step = 0;
    shared_data->weather.available = 0;
    sem_post(sem_mem);

    int step_pipes[num_plans][2];
    SafetyViolationEvent *violation_events = (SafetyViolationEvent*)calloc(MAX_VIOLATION_EVENTS, sizeof(SafetyViolationEvent));
    if (violation_events == NULL) {
        perror("'Calloc' error.");
        return EXIT_FAILURE;
    }

    for (int i = 0; i < num_plans; i++) {
        step_pipes[i][0] = -1;
        step_pipes[i][1] = -1;

        if (pipe(step_pipes[i]) == -1) {
            perror("Step pipe error.");
            return EXIT_FAILURE;
        }

        pid_t pid = fork();

        if (pid == -1) {
            perror("Fork failed.");
            return EXIT_FAILURE;
        }

        if (pid == 0) {
            close_child_inherited_pipes(i, step_pipes);
            close_fd_if_open(&step_pipes[i][1]);
            run_flight(plans[i], step_pipes[i][0], shared_data, i, sem_mem);

            _exit(EXIT_SUCCESS);
        } else {
            close_fd_if_open(&step_pipes[i][0]);

            sem_wait(sem_mem);
            shared_data->flights[i].id = plans[i].id;
            copy_designator(shared_data->flights[i].designator, plans[i].designator);
            shared_data->flights[i].pid = pid;
            shared_data->flights[i].active = 1;
            shared_data->flights[i].history_count = 0;
            shared_data->flights[i].has_position = 0;
            shared_data->flights[i].updated_in_step = 0;
            shared_data->flights[i].completed = 0;
            shared_data->flights[i].execution_status = FLIGHT_STATUS_RUNNING;
            shared_data->flights[i].weather.wind_direction_deg = 0.0;
            shared_data->flights[i].weather.wind_speed_ms = 0.0;
            shared_data->flights[i].weather.time_step = 0;
            shared_data->flights[i].weather.available = 0;
            sem_post(sem_mem);
        }
    }

    SimulationContext ctx;
    ctx.shared_data = shared_data;
    ctx.sem_mem = sem_mem;
    ctx.plans = plans;
    ctx.num_plans = num_plans;
    ctx.step_pipes = step_pipes;
    ctx.active_flights = num_plans;
    ctx.global_step = 0;
    ctx.violation_events = violation_events;
    ctx.stored_violation_count = &stored_violation_count;
    ctx.simulation_done = 0;
    ctx.report_shutdown = 0;

    pthread_mutex_init(&ctx.mutex, NULL);
    pthread_cond_init(&ctx.cond_violation, NULL);

    pthread_t report_thread;
    if (pthread_create(&report_thread, NULL, report_generation_worker, &ctx) != 0) {
        perror("Report thread creation failed.");
        exit(EXIT_FAILURE);
    }

    pthread_t weather_thread;
    if (pthread_create(&weather_thread, NULL, weather_worker, &ctx) != 0) {
        perror("Weather thread creation failed.");
        exit(EXIT_FAILURE);
    }

    pthread_t step_thread;
    if (pthread_create(&step_thread, NULL, step_engine_worker, &ctx) != 0) {
        perror("Step thread creation fail.");
        exit(EXIT_FAILURE);
    }

    pthread_t monitor_thread;
    if (pthread_create(&monitor_thread, NULL, monitor_flights_worker, &ctx) != 0) {
        perror("Monitor thread creation fail.");
        exit(EXIT_FAILURE);
    }

    pthread_join(step_thread, NULL);
    pthread_join(monitor_thread, NULL);
    pthread_join(weather_thread, NULL);

    printf("\n### Position History Summary ###\n");

    for (int i = 0; i < num_plans; i++) {
        sem_wait(sem_mem);

        printf("Flight %s (id: %d) stored %d position updates\n",
               shared_data->flights[i].designator, shared_data->flights[i].id, shared_data->flights[i].history_count);

        for (int h = 0; h < shared_data->flights[i].history_count; h++) {
            PositionRecord r = shared_data->flights[i].history[h];

            printf("  Step %d | %s | Lat=%.4f, Lon=%.4f, Alt=%.0f\n",r.time_step,r.mode,r.pos.lat,r.pos.lon,r.pos.alt);
        }

        sem_post(sem_mem);
    }

    for (int i = 0; i < num_plans; i++) {
        int child_status;

        sem_wait(sem_mem);
        pid_t child_pid = shared_data->flights[i].pid;
        sem_post(sem_mem);

        if (waitpid(child_pid, &child_status, 0) == -1) {
            perror("Waitpid fail.");

            sem_wait(sem_mem);
            if (shared_data->flights[i].execution_status != FLIGHT_STATUS_TERMINATED_SAFETY) {
                shared_data->flights[i].execution_status = FLIGHT_STATUS_ERROR;
            }
            sem_post(sem_mem);

            continue;
        }

        sem_wait(sem_mem);
        update_status_from_child_exit(&shared_data->flights[i], child_status);
        sem_post(sem_mem);
    }

    int result = calculate_validation_result(shared_data, num_plans, stored_violation_count);

    pthread_mutex_lock(&ctx.mutex);
    ctx.report_shutdown = 1;
    pthread_cond_signal(&ctx.cond_violation);
    pthread_mutex_unlock(&ctx.mutex);

    pthread_join(report_thread, NULL);

    pthread_mutex_destroy(&ctx.mutex);
    pthread_cond_destroy(&ctx.cond_violation);

    for (int i = 0; i < num_plans; i++) {
        free(plans[i].segments);
    }

    munmap(shared_data, sizeof(SharedSimulationArea));
    shm_unlink("/shm_simulation");
    sem_close(sem_mem);
    sem_unlink("/sem_simulation_mem");

    free(plans);
    free(violation_events);

    printf("\n### Simulation Ended ###\n\n");
    return (result == 1) ? EXIT_SUCCESS : EXIT_FAILURE;
}
