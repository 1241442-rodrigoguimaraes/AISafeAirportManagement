#include "simulation_types.h"
#include "safety_monitor.h"
#include "utils.h"
#include "pthread.h"
#include "stdio.h"
#include "stddef.h"
#include "signal.h"
#include "shared_state.h"
#include "unistd.h"

#define M_PI 3.14159265358979323846

double calculate_distance(Position p1, Position p2) {
    double lat_diff = (p2.lat - p1.lat) * DEGREES_TO_METERS;
    double avg_lat_rad = ((p1.lat + p2.lat) / 2.0) * (M_PI / 180.0);
    double lon_diff = (p2.lon - p1.lon) * DEGREES_TO_METERS * cos(avg_lat_rad);

    return sqrt(lat_diff * lat_diff + lon_diff * lon_diff);
}

ViolationType check_violation(Position p1, Position p2) {
    double horizontal_distance = calculate_distance(p1, p2);
    double vertical_distance = fabs(p1.alt - p2.alt);

    if (horizontal_distance < MIN_COLLISION_DISTANCE && vertical_distance < MIN_COLLISION_DISTANCE)
        return CRITICAL_COLLISION_VIOLATION;

    if (horizontal_distance < HORIZONTAL_LIMIT && vertical_distance < VERTICAL_LIMIT)
        return SAFETY_PROXIMITY_VIOLATION;

    return NO_VIOLATION;
}

const char* violation_type_to_text(ViolationType violation) {
    switch (violation) {
        case CRITICAL_COLLISION_VIOLATION:
            return "Critical collision";
        case SAFETY_PROXIMITY_VIOLATION:
            return "Proximity";
        default:
            return "none";
    }
}

VelocityVector calculate_velocity_vector(const FlightStatus *status) {
    VelocityVector velocity = {0.0, 0.0, 0.0};

    if (status->history_count < 2) {
        return velocity;
    }

    PositionRecord current = status->history[status->history_count - 1];
    PositionRecord previous = status->history[status->history_count - 2];
    double elapsed = (double)(current.time_step - previous.time_step) * TIME_STEP_SECONDS;

    if (elapsed <= 0.0) {
        elapsed = (double)TIME_STEP_SECONDS;
    }

    velocity.lat_per_second = (current.pos.lat - previous.pos.lat) / elapsed;
    velocity.lon_per_second = (current.pos.lon - previous.pos.lon) / elapsed;
    velocity.alt_per_second = (current.pos.alt - previous.pos.alt) / elapsed;

    return velocity;
}

void record_violation_event(SafetyViolationEvent *events, int *stored_count, int time_step,
    const FlightStatus *flight_a, const FlightStatus *flight_b, ViolationType violation) {
    if (*stored_count >= MAX_VIOLATION_EVENTS) return;

    SafetyViolationEvent *event = &events[*stored_count];
    event->time_step = time_step;
    event->flight_id_a = flight_a->id;
    event->flight_id_b = flight_b->id;
    copy_designator(event->flight_designator_a, flight_a->designator);
    copy_designator(event->flight_designator_b, flight_b->designator);
    event->violation_type = violation;
    event->position_a = flight_a->last_pos;
    event->position_b = flight_b->last_pos;
    event->velocity_a = calculate_velocity_vector(flight_a);
    event->velocity_b = calculate_velocity_vector(flight_b);
    event->horizontal_distance = calculate_distance(flight_a->last_pos, flight_b->last_pos);
    event->vertical_distance = fabs(flight_a->last_pos.alt - flight_b->last_pos.alt);

    (*stored_count)++;
}

static int flight_updated_for_step(const FlightStatus *flight, int step) {
    if (!flight->updated_in_step || flight->history_count <= 0) {
        return 0;
    }

    return flight->history[flight->history_count - 1].time_step == step;
}

void* monitor_flights_worker(void *arg) {
    SimulationContext *ctx = (SimulationContext*) arg;
    int last_checked_step = -1;

    while (1) {
        pthread_mutex_lock(&ctx->mutex);
        if (ctx->simulation_done) {
            pthread_mutex_unlock(&ctx->mutex);
            break;
        }
        int current_step = ctx->global_step;
        pthread_mutex_unlock(&ctx->mutex);

        if (current_step == last_checked_step) {
            usleep(50000);
            continue;
        }

        for (int i = 0; i < ctx->num_plans; i++) {
            sem_wait(ctx->sem_mem);
            int is_active = ctx->shared_data->flights[i].active;
            sem_post(ctx->sem_mem);

            if (!is_active) continue;

            int pronto = 0;
            while (!pronto) {
                sem_wait(ctx->sem_mem);

                FlightStatus *flight = &ctx->shared_data->flights[i];

                if (!flight->active || flight_updated_for_step(flight, current_step)) pronto = 1;

                sem_post(ctx->sem_mem);

                if (!pronto) usleep(1000); //ATENÇÃO: CHAMADA PARA SINCRONIZAÇÃO TEMPORÁRIA. RESPONSÁVEL PELA USER STORY 108, ATUALIZAR.
            }

            sem_wait(ctx->sem_mem);
            FlightStatus flight = ctx->shared_data->flights[i];
            if (flight.active && flight_updated_for_step(&flight, current_step)) {
                int h = flight.history_count - 1;
                char* actual_mode = (h >= 0) ? flight.history[h].mode : "UNKNOWN";

                printf("[Controller] Flight %s (id: %d) | Step %d | Mode %s: Lat=%.4f, Lon=%.4f, Alt=%.0f%s\n",
                    flight.designator, flight.id, flight.history[h].time_step, actual_mode, flight.last_pos.lat,
                    flight.last_pos.lon, flight.last_pos.alt, flight.completed ? " | completed" : "");

            }
            sem_post(ctx->sem_mem);
        }

        for (int i = 0; i < ctx->num_plans; i++) {
            sem_wait(ctx->sem_mem);
            if (ctx->shared_data->flights[i].violation_counter > MAX_VIOLATION_LIMIT) {
                sem_post(ctx->sem_mem);
                continue;
            }

            int i_ready = (ctx->shared_data->flights[i].active &&
                           flight_updated_for_step(&ctx->shared_data->flights[i], current_step));
            sem_post(ctx->sem_mem);

            if (!i_ready) continue;

            for (int j = i + 1; j < ctx->num_plans; j++) {
                sem_wait(ctx->sem_mem);

                if (!ctx->shared_data->flights[j].active ||
                    !flight_updated_for_step(&ctx->shared_data->flights[j], current_step)) {
                    sem_post(ctx->sem_mem);
                    continue;
                }

                Position pos_i = ctx->shared_data->flights[i].last_pos;
                Position pos_j = ctx->shared_data->flights[j].last_pos;

                ViolationType violation = check_violation(pos_i, pos_j);

                if (violation == NO_VIOLATION) {
                    sem_post(ctx->sem_mem);
                    continue;
                }

                ctx->shared_data->flights[i].violation_counter++;
                ctx->shared_data->flights[j].violation_counter++;

                int vc_i = ctx->shared_data->flights[i].violation_counter;
                int vc_j = ctx->shared_data->flights[j].violation_counter;
                pid_t pid_i = ctx->shared_data->flights[i].pid;
                pid_t pid_j = ctx->shared_data->flights[j].pid;
                char desig_i[MAX_DESIGNATOR_LENGTH], desig_j[MAX_DESIGNATOR_LENGTH];
                copy_designator(desig_i, ctx->shared_data->flights[i].designator);
                copy_designator(desig_j, ctx->shared_data->flights[j].designator);
                int id_i = ctx->shared_data->flights[i].id;
                int id_j = ctx->shared_data->flights[j].id;
                FlightStatus flight_i_snapshot = ctx->shared_data->flights[i];
                FlightStatus flight_j_snapshot = ctx->shared_data->flights[j];
                sem_post(ctx->sem_mem);

                pthread_mutex_lock(&ctx->mutex);
                record_violation_event(ctx->violation_events, ctx->stored_violation_count, current_step,
                    &flight_i_snapshot, &flight_j_snapshot, violation);
                pthread_cond_signal(&ctx->cond_violation);
                pthread_mutex_unlock(&ctx->mutex);

                printf("[Controller] WARNING: %s violation between Flight %s (id: %d) and Flight %s (id: %d) at Step %d.\n",
                       violation_type_to_text(violation),
                       desig_i, id_i, desig_j, id_j, current_step);
                fflush(stdout);

                kill(pid_i, SIGUSR1);
                kill(pid_j, SIGUSR1);

                pthread_mutex_lock(&ctx->mutex);
                sem_wait(ctx->sem_mem);
                deactivate_flight(&ctx->shared_data->flights[i], &ctx->step_pipes[i][1], &ctx->active_flights, FLIGHT_STATUS_TERMINATED_SAFETY);
                deactivate_flight(&ctx->shared_data->flights[j], &ctx->step_pipes[j][1], &ctx->active_flights, FLIGHT_STATUS_TERMINATED_SAFETY);
                sem_post(ctx->sem_mem);
                pthread_mutex_unlock(&ctx->mutex);

                int deactivate_i = (vc_i > MAX_VIOLATION_LIMIT);
                int deactivate_j = (vc_j > MAX_VIOLATION_LIMIT);

                if (deactivate_i) {
                    printf("[Controller] TOO MANY VIOLATIONS! Flight %s terminated for safety.\n", desig_i);
                    kill(pid_i, SIGKILL);

                    pthread_mutex_lock(&ctx->mutex);
                    sem_wait(ctx->sem_mem);
                    deactivate_flight(&ctx->shared_data->flights[i], &ctx->step_pipes[i][1], &ctx->active_flights, FLIGHT_STATUS_TERMINATED_SAFETY);
                    sem_post(ctx->sem_mem);
                    pthread_mutex_unlock(&ctx->mutex);
                }

                if (deactivate_j) {
                    printf("[Controller] TOO MANY VIOLATIONS! Flight %s terminated for safety.\n", desig_j);
                    kill(pid_j, SIGKILL);

                    pthread_mutex_lock(&ctx->mutex);
                    sem_wait(ctx->sem_mem);
                    deactivate_flight(&ctx->shared_data->flights[j], &ctx->step_pipes[j][1], &ctx->active_flights, FLIGHT_STATUS_TERMINATED_SAFETY);
                    sem_post(ctx->sem_mem);
                    pthread_mutex_unlock(&ctx->mutex);
                }

                if (!deactivate_i) break;
            }
        }

        last_checked_step = current_step;

        usleep(50000);
    }

    return NULL;
}
