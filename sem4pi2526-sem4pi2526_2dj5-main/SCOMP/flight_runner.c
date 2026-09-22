#include "simulation_types.h"
#include "stdio.h"
#include "flight_runner.h"
#include "utils.h"
#include "signal.h"
#include "string.h"
#include "stddef.h"
#include "stdlib.h"
#include "unistd.h"
#include "shared_state.h"
#include "semaphore.h"
#include "pthread.h"
#include "errno.h"
#include "math.h"

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

static volatile sig_atomic_t safety_stop_requested = 0;

static Position apply_wind(Position pos, WeatherStats weather) {
    if (!weather.available || weather.wind_speed_ms <= 0.0) {
        return pos;
    }

    double direction_rad = weather.wind_direction_deg * M_PI / 180.0;
    double north_m = cos(direction_rad) * weather.wind_speed_ms * TIME_STEP_SECONDS;
    double east_m = sin(direction_rad) * weather.wind_speed_ms * TIME_STEP_SECONDS;
    double lat_rad = pos.lat * M_PI / 180.0;
    double lon_divisor = DEGREES_TO_METERS * cos(lat_rad);

    pos.lat += north_m / DEGREES_TO_METERS;

    if (lon_divisor != 0.0) {
        pos.lon += east_m / lon_divisor;
    }

    return pos;
}

static int all_active_flights_updated(SimulationContext *ctx) {
    int all_updated = 1;

    sem_wait(ctx->sem_mem);
    for (int i = 0; i < ctx->num_plans; i++) {
        FlightStatus *flight = &ctx->shared_data->flights[i];

        if (flight->active && !flight->updated_in_step) {
            all_updated = 0;
            break;
        }
    }
    sem_post(ctx->sem_mem);

    return all_updated;
}

static int weather_ready_for_step(SimulationContext *ctx, int step) {
    int ready;

    sem_wait(ctx->sem_mem);
    ready = ctx->shared_data->weather.available && ctx->shared_data->weather.time_step == step;
    sem_post(ctx->sem_mem);

    return ready;
}

void close_child_inherited_pipes(int current_index, int step_pipes[][2]) {
    for (int i = 0; i < current_index; i++) {
        close_fd_if_open(&step_pipes[i][0]);
        close_fd_if_open(&step_pipes[i][1]);
    }
}

void handle_sigusr1(int sig, siginfo_t* info, void* context) {
    (void)sig;
    (void)info;
    (void)context;

    const char msg[] = "[ALERT] Safety violation detected! Performing emergency procedure...\n";
    write(STDOUT_FILENO, msg, sizeof(msg) - 1);
    safety_stop_requested = 1;
}

void run_flight(FlightPlan plan, int step_pipe_fd, SharedSimulationArea* shared_data, int i, sem_t* sem_mem) {
    global_step_pipe_fd = step_pipe_fd;
    safety_stop_requested = 0;

    struct sigaction sa;
    memset(&sa, 0, sizeof(sa));
    sa.sa_sigaction = handle_sigusr1;
    sa.sa_flags = SA_SIGINFO;
    sigfillset(&sa.sa_mask);

    if (sigaction(SIGUSR1, &sa, NULL) == -1) {
        perror("Error sigaction");
        exit(EXIT_FAILURE);
    }

    int update_index = 0;
    int total_updates = plan.num_segments * 2;

    while (update_index < total_updates) {
        if (safety_stop_requested) {
            break;
        }

        StepCommand command;
        ssize_t command_bytes = read(step_pipe_fd, &command, sizeof(StepCommand));

        if (command_bytes == -1 && errno == EINTR && safety_stop_requested) {
            break;
        }

        if (command_bytes == 0) {
            break;
        }

        if (command_bytes != sizeof(StepCommand)) {
            perror("Step command reading fail.");
            close(step_pipe_fd);
            exit(EXIT_FAILURE);
        }

        int segment_index = update_index / 2;
        int point_index = update_index % 2;
        FlightSegment seg = plan.segments[segment_index];
        Position points[2] = {seg.start, seg.end};

        sem_wait(sem_mem);
        WeatherStats weather = shared_data->flights[i].weather;
        Position adjusted_pos = apply_wind(points[point_index], weather);

        shared_data->flights[i].last_pos = adjusted_pos;
        shared_data->flights[i].completed = (update_index == total_updates - 1);
        shared_data->flights[i].has_position = 1;

        if (shared_data->flights[i].history_count < MAX_HISTORY) {
            int h = shared_data->flights[i].history_count;

            shared_data->flights[i].history[h].time_step = command.time_step;
            shared_data->flights[i].history[h].pos = adjusted_pos;
            copy_mode(shared_data->flights[i].history[h].mode, seg.mode);

            shared_data->flights[i].history_count++;
        }

        shared_data->flights[i].updated_in_step = 1;
        sem_post(sem_mem);

        update_index++;
    }

    close(step_pipe_fd);
    exit(EXIT_SUCCESS);
}

void* step_engine_worker(void *arg) {
    SimulationContext *ctx = (SimulationContext*) arg;

    while (1) {
        pthread_mutex_lock(&ctx->mutex);
        if (ctx->active_flights <= 0) {
            ctx->simulation_done = 1;
            pthread_mutex_unlock(&ctx->mutex);
            break;
        }
        int step_to_send = ctx->global_step;
        pthread_mutex_unlock(&ctx->mutex);

        while (!weather_ready_for_step(ctx, step_to_send)) {
            usleep(1000);
        }

        printf("\n--- Time Step %d ---\n", step_to_send + 1);
        fflush(stdout);

        for (int i = 0; i < ctx->num_plans; i++) {
            sem_wait(ctx->sem_mem);
            ctx->shared_data->flights[i].updated_in_step = 0;
            sem_post(ctx->sem_mem);
        }

        for (int i = 0; i < ctx->num_plans; i++) {
            sem_wait(ctx->sem_mem);
            int is_active = ctx->shared_data->flights[i].active;
            sem_post(ctx->sem_mem);

            if (!is_active) continue;

            StepCommand command;
            command.time_step = step_to_send;

            ssize_t written = write(ctx->step_pipes[i][1], &command, sizeof(StepCommand));
            if (written != sizeof(StepCommand)) {
                if (written == -1 && errno != EPIPE) {
                    perror("Step command writing error.");
                }

                sem_wait(ctx->sem_mem);
                pthread_mutex_lock(&ctx->mutex);

                FlightExecutionStatus final_status = ctx->shared_data->flights[i].execution_status == FLIGHT_STATUS_TERMINATED_SAFETY
                    ? FLIGHT_STATUS_TERMINATED_SAFETY
                    : FLIGHT_STATUS_ERROR;
                deactivate_flight(&ctx->shared_data->flights[i], &ctx->step_pipes[i][1], &ctx->active_flights, final_status);

                pthread_mutex_unlock(&ctx->mutex);
                sem_post(ctx->sem_mem);
            }
        }

        while (!all_active_flights_updated(ctx)) {
            usleep(1000);
        }

        sleep(TIME_STEP_SECONDS);

        for (int i = 0; i < ctx->num_plans; i++) {
            sem_wait(ctx->sem_mem);
            int must_deactivate = (ctx->shared_data->flights[i].active && ctx->shared_data->flights[i].completed);
            sem_post(ctx->sem_mem);

            if (must_deactivate) {
                sem_wait(ctx->sem_mem);
                pthread_mutex_lock(&ctx->mutex);

                deactivate_flight(&ctx->shared_data->flights[i], &ctx->step_pipes[i][1], &ctx->active_flights, FLIGHT_STATUS_COMPLETED);

                pthread_mutex_unlock(&ctx->mutex);
                sem_post(ctx->sem_mem);
            }
        }

        pthread_mutex_lock(&ctx->mutex);
        ctx->global_step++;
        pthread_mutex_unlock(&ctx->mutex);
    }

    return NULL;
}
