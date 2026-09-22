#include "weather.h"
#include "simulation_types.h"
#include "unistd.h"
#include "stdio.h"

typedef struct {
    char designator[MAX_DESIGNATOR_LENGTH];
    WeatherStats weather;
} WeatherPrintEntry;

static WeatherStats weather_from_segment(FlightPlan *plan, int step) {
    WeatherStats weather;
    int segment_index = step / 2;

    if (segment_index >= plan->num_segments) {
        segment_index = plan->num_segments - 1;
    }

    if (segment_index < 0 || plan->num_segments <= 0 || !plan->segments[segment_index].has_wind) {
        weather.wind_direction_deg = 0.0;
        weather.wind_speed_ms = 0.0;
    } else {
        weather.wind_direction_deg = plan->segments[segment_index].wind_direction_deg;
        weather.wind_speed_ms = plan->segments[segment_index].wind_speed_ms;
    }

    weather.time_step = step;
    weather.available = 1;
    return weather;
}

void* weather_worker(void *arg) {
    SimulationContext *ctx = (SimulationContext*) arg;
    int last_step = -1;

    while (1) {
        pthread_mutex_lock(&ctx->mutex);
        int done = ctx->simulation_done;
        int current_step = ctx->global_step;
        pthread_mutex_unlock(&ctx->mutex);

        if (done) {
            break;
        }

        if (current_step != last_step) {
            WeatherPrintEntry print_entries[MAX_FLIGHTS];
            int print_count = 0;

            sem_wait(ctx->sem_mem);

            int global_weather_set = 0;
            for (int i = 0; i < ctx->num_plans; i++) {
                WeatherStats weather = weather_from_segment(&ctx->plans[i], current_step);
                ctx->shared_data->flights[i].weather = weather;

                if (!global_weather_set && ctx->shared_data->flights[i].active) {
                    ctx->shared_data->weather = weather;
                    global_weather_set = 1;
                }

                if (ctx->shared_data->flights[i].active && print_count < MAX_FLIGHTS) {
                    snprintf(print_entries[print_count].designator,
                             sizeof(print_entries[print_count].designator),
                             "%s",
                             ctx->shared_data->flights[i].designator);
                    print_entries[print_count].weather = weather;
                    print_count++;
                }
            }

            if (!global_weather_set) {
                ctx->shared_data->weather.time_step = current_step;
                ctx->shared_data->weather.available = 0;
                ctx->shared_data->weather.wind_direction_deg = 0.0;
                ctx->shared_data->weather.wind_speed_ms = 0.0;
            }

            sem_post(ctx->sem_mem);

            char output[4096];
            int offset = snprintf(output, sizeof(output), "\n[Weather] Step %d\n", current_step + 1);

            for (int i = 0; i < print_count; i++) {
                if (offset < 0 || offset >= (int)sizeof(output)) {
                    break;
                }

                offset += snprintf(output + offset,
                                   sizeof(output) - offset,
                                   "  Flight %s | Wind %.1f deg %.1f m/s\n",
                                   print_entries[i].designator,
                                   print_entries[i].weather.wind_direction_deg,
                                   print_entries[i].weather.wind_speed_ms);
            }

            fputs(output, stdout);
            fflush(stdout);
            last_step = current_step;
        }

        usleep(1000);
    }

    return NULL;
}
