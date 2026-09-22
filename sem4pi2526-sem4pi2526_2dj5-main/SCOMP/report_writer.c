#include "simulation_types.h"
#include "report_writer.h"
#include "safety_monitor.h"
#include "shared_state.h"
#include "stddef.h"
#include "time.h"
#include "errno.h"
#include "sys/stat.h"

static void write_violation_event(FILE *report, int event_number, SafetyViolationEvent event) {
    fprintf(report, "Event %d\n", event_number);
    fprintf(report, "- Simulation timestamp: T+%d s (step %d)\n",
            event.time_step * TIME_STEP_SECONDS, event.time_step);
    fprintf(report, "- Type: %s\n", violation_type_to_text(event.violation_type));
    fprintf(report, "- Flights: %s (id: %d) and %s (id: %d)\n",
            event.flight_designator_a, event.flight_id_a,
            event.flight_designator_b, event.flight_id_b);
    fprintf(report, "- Horizontal distance: %.2f m\n", event.horizontal_distance);
    fprintf(report, "- Vertical distance: %.2f m\n", event.vertical_distance);
    write_position(report, "- Flight A position:", event.position_a);
    write_position(report, "- Flight B position:", event.position_b);
    write_velocity(report, "- Flight A velocity vector:", event.velocity_a);
    write_velocity(report, "- Flight B velocity vector:", event.velocity_b);
    fprintf(report, "\n");
}

void* report_generation_worker(void *arg) {
    SimulationContext *ctx = (SimulationContext*)arg;
    int processed_violation_count = 0;

    while (1) {
        pthread_mutex_lock(&ctx->mutex);

        while (processed_violation_count >= *ctx->stored_violation_count && !ctx->report_shutdown) {
            pthread_cond_wait(&ctx->cond_violation, &ctx->mutex);
        }

        while (processed_violation_count < *ctx->stored_violation_count) {
            SafetyViolationEvent event = ctx->violation_events[processed_violation_count];
            processed_violation_count++;

            printf("[Report] Safety event queued: %s between Flight %s and Flight %s at Step %d.\n",
                   violation_type_to_text(event.violation_type),
                   event.flight_designator_a,
                   event.flight_designator_b,
                   event.time_step);
        }

        if (ctx->report_shutdown) {
            pthread_mutex_unlock(&ctx->mutex);
            break;
        }

        pthread_mutex_unlock(&ctx->mutex);
    }

    SimulationReportData report_data = {
        .total_flights = ctx->num_plans,
        .shared_data = ctx->shared_data,
        .total_violation_count = *ctx->stored_violation_count,
        .violation_events = ctx->violation_events,
        .report_path = REPORT_OUTPUT_FILE
    };

    generate_simulation_report(&report_data);
    printf("\n[Report] Final simulation report saved to %s\n", REPORT_OUTPUT_FILE);

    return NULL;
}

void* generate_simulation_report(void *arg) {
    SimulationReportData *data = (SimulationReportData*)arg;
    int validation_passed = calculate_validation_result(
        data->shared_data,
        data->total_flights,
        data->total_violation_count
    );

    ensure_report_directory();

    FILE *report = fopen(data->report_path, "w");
    if (!report) {
        perror("Report file creation failed");
        return NULL;
    }

    time_t now = time(NULL);
    char time_buf[26];
    if (ctime_r(&now, time_buf) == NULL) {
        snprintf(time_buf, sizeof(time_buf), "unknown\n");
    }

    fprintf(report, "# Final Simulation Report\n\n");
    fprintf(report, "Generated at: %s", time_buf);
    fprintf(report, "Total flights: %d\n", data->total_flights);
    fprintf(report, "Total safety violations: %d\n", data->total_violation_count);
    fprintf(report, "Validation result: %s\n\n", validation_passed ? "PASS" : "FAIL");

    fprintf(report, "## Flight Execution Statuses\n\n");
    for (int i = 0; i < data->total_flights; i++) {
        FlightStatus *status = &data->shared_data->flights[i];
        fprintf(report, "Flight %s (id: %d)\n", status->designator, status->id);
        fprintf(report, "- PID: %d\n", status->pid);
        fprintf(report, "- Status: %s\n", flight_status_to_text(status->execution_status));
        fprintf(report, "- Stored positions: %d\n", status->history_count);
        if (status->has_position) {
            write_position(report, "- Last position:", status->last_pos);
        }
        fprintf(report, "\n");
    }

    fprintf(report, "## Safety Violation Events\n\n");
    if (data->total_violation_count == 0) {
        fprintf(report, "No safety violations were recorded.\n");
    } else {
        for (int i = 0; i < data->total_violation_count; i++) {
            write_violation_event(report, i + 1, data->violation_events[i]);
        }
    }

    fclose(report);
    return NULL;
}

int calculate_validation_result(SharedSimulationArea *shared_data, int total_flights, int violation_count) {
    if (violation_count > 0) {
        return 0;
    }

    for (int i = 0; i < total_flights; i++) {
        if (shared_data->flights[i].execution_status != FLIGHT_STATUS_COMPLETED) {
            return 0;
        }
    }

    return 1;
}

void ensure_report_directory(void) {
    if (mkdir(REPORT_OUTPUT_DIR, 0777) == -1 && errno != EEXIST) {
        perror("Report directory creation failed.");
    }
}

void write_position(FILE *report, const char *label, Position position) {
    fprintf(report, "%s Lat=%.6f, Lon=%.6f, Alt=%.2f\n",
            label, position.lat, position.lon, position.alt);
}

void write_velocity(FILE *report, const char *label, VelocityVector velocity) {
    fprintf(report, "%s dLat/s=%.8f, dLon/s=%.8f, dAlt/s=%.2f\n",
            label, velocity.lat_per_second, velocity.lon_per_second, velocity.alt_per_second);
}
