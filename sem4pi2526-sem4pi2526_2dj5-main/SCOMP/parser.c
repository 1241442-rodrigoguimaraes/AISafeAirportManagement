#include "simulation_types.h"
#include "parser.h"
#include "utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "cJSON.h"

static char* trim_left(char *text) {
    while (*text != '\0' && isspace((unsigned char)*text)) {
        text++;
    }

    return text;
}

static int starts_with(const char *text, const char *prefix) {
    return strncmp(text, prefix, strlen(prefix)) == 0;
}

int derive_flight_id(const char *flight_designator) {
    unsigned int hash = 5381;

    for (const char *c = flight_designator; *c != '\0'; c++) {
        hash = ((hash << 5) + hash) + (unsigned char)*c;
    }

    return (int)(hash & 0x7fffffff);
}

static int parse_position_line(const char *line, const char *label, Position *position) {
    char parsed_label[16];
    char unit[16];

    int matches = sscanf(line,
                         " %15[^:]: { latitude: %lf longitude: %lf altitude: %lf %15s }",
                         parsed_label, &position->lat, &position->lon, &position->alt, unit);

    return matches == 5 && strcmp(parsed_label, label) == 0;
}

static void append_segment(FlightPlan *plan, FlightSegment segment) {
    FlightSegment *segments = (FlightSegment*)realloc(
        plan->segments,
        sizeof(FlightSegment) * (plan->num_segments + 1)
    );

    if (segments == NULL) {
        perror("'Realloc' error.");
        free(plan->segments);
        exit(EXIT_FAILURE);
    }

    plan->segments = segments;
    plan->segments[plan->num_segments] = segment;
    plan->num_segments++;
}

FlightPlan parse_fp_flight_plan(const char* filepath) {
    FILE* f = fopen(filepath, "rb");
    if (!f) {
        perror("Error opening flight plan.");
        exit(EXIT_FAILURE);
    }

    FlightPlan plan;
    memset(&plan, 0, sizeof(plan));
    copy_designator(plan.designator, "unknown");
    plan.id = 0;
    plan.num_segments = 0;
    plan.segments = NULL;

    char line[1024];
    int in_segment = 0;
    int has_mode = 0;
    int has_start = 0;
    int has_end = 0;
    FlightSegment current_segment;
    memset(&current_segment, 0, sizeof(current_segment));

    while (fgets(line, sizeof(line), f) != NULL) {
        char *trimmed = trim_left(line);

        if (plan.id == 0 && starts_with(trimmed, "flight ")) {
            char designator[64];
            if (sscanf(trimmed, "flight %63s", designator) == 1) {
                char *brace = strchr(designator, '{');
                if (brace != NULL) {
                    *brace = '\0';
                }

                copy_designator(plan.designator, designator);
                plan.id = derive_flight_id(designator);
            }
        }

        if (!in_segment && starts_with(trimmed, "segment")) {
            in_segment = 1;
            has_mode = 0;
            has_start = 0;
            has_end = 0;
            memset(&current_segment, 0, sizeof(current_segment));
            current_segment.wind_direction_deg = 0.0;
            current_segment.wind_speed_ms = 0.0;
            current_segment.has_wind = 0;
            continue;
        }

        if (!in_segment) {
            continue;
        }

        if (starts_with(trimmed, "mode:")) {
            char mode[MAX_MODE_LENGTH];
            if (sscanf(trimmed, "mode: %19s", mode) == 1) {
                copy_mode(current_segment.mode, mode);
                has_mode = 1;
            }
        } else if (starts_with(trimmed, "start:")) {
            has_start = parse_position_line(trimmed, "start", &current_segment.start);
        } else if (starts_with(trimmed, "end:")) {
            has_end = parse_position_line(trimmed, "end", &current_segment.end);
        } else if (starts_with(trimmed, "wind:")) {
            double direction;
            double speed;

            if (sscanf(trimmed, "wind: %lf deg %lf m/s", &direction, &speed) == 2) {
                current_segment.wind_direction_deg = direction;
                current_segment.wind_speed_ms = speed;
                current_segment.has_wind = 1;
            }
        } else if (trimmed[0] == '}') {
            if (has_mode && has_start && has_end) {
                append_segment(&plan, current_segment);
            }

            in_segment = 0;
        }
    }

    fclose(f);

    if (plan.id == 0) {
        fprintf(stderr, "Missing flight designator in file: %s\n", filepath);
        free(plan.segments);
        exit(EXIT_FAILURE);
    }

    if (plan.num_segments == 0) {
        fprintf(stderr, "No valid segments found in file: %s\n", filepath);
        free(plan.segments);
        exit(EXIT_FAILURE);
    }

    return plan;
}

FlightPlan parse_json_flight_plan(const char* filepath) {
    FILE* f = fopen(filepath, "rb");
    if (!f) {
        perror("Error opening JSON.");
        exit(EXIT_FAILURE);
    }

    fseek(f, 0, SEEK_END);
    long len = ftell(f);
    fseek(f, 0, SEEK_SET);
    char* data = (char*)malloc(len + 1);
    fread(data, 1, len, f);
    fclose(f);
    data[len] = '\0';

    cJSON* root = cJSON_Parse(data);
    free(data);
    if (!root) {
        fprintf(stderr, "JSON parse error in file: %s\n", filepath);
        exit(EXIT_FAILURE);
    }

    cJSON* json = cJSON_IsArray(root) ? cJSON_GetArrayItem(root, 0) : root;
    if (!json) {
        fprintf(stderr, "Empty flight plan array in file: %s\n", filepath);
        cJSON_Delete(root);
        exit(EXIT_FAILURE);
    }

    FlightPlan plan;
    memset(&plan, 0, sizeof(plan));
    copy_designator(plan.designator, "unknown");

    cJSON* flight_id_item = cJSON_GetObjectItem(json, "flightId");
    if (cJSON_IsString(flight_id_item)) {
        int hash = 0;
        for (const char* c = flight_id_item->valuestring; *c; c++) hash += (unsigned char)*c;
        plan.id = hash;
        copy_designator(plan.designator, flight_id_item->valuestring);
    } else if (cJSON_IsNumber(flight_id_item)) {
        plan.id = flight_id_item->valueint;
        snprintf(plan.designator, sizeof(plan.designator), "%d", plan.id);
    } else {
        plan.id = 0;
    }

    cJSON* legs = cJSON_GetObjectItem(json, "legs");
    cJSON* leg  = cJSON_GetArrayItem(legs, 0);
    cJSON* segments_json = cJSON_GetObjectItem(leg, "segments");

    plan.num_segments = cJSON_GetArraySize(segments_json);
    plan.segments = (FlightSegment*)malloc(sizeof(FlightSegment) * plan.num_segments);

    for (int i = 0; i < plan.num_segments; i++) {
        cJSON* item = cJSON_GetArrayItem(segments_json, i);

        copy_mode(plan.segments[i].mode, cJSON_GetObjectItem(item, "mode")->valuestring);

        cJSON* start = cJSON_GetObjectItem(item, "start");
        plan.segments[i].start.lat = cJSON_GetObjectItem(start, "latitude")->valuedouble;
        plan.segments[i].start.lon = cJSON_GetObjectItem(start, "longitude")->valuedouble;
        plan.segments[i].start.alt = cJSON_GetObjectItem(start, "altitudeMeters")->valuedouble;

        cJSON* end = cJSON_GetObjectItem(item, "end");
        plan.segments[i].end.lat = cJSON_GetObjectItem(end, "latitude")->valuedouble;
        plan.segments[i].end.lon = cJSON_GetObjectItem(end, "longitude")->valuedouble;
        plan.segments[i].end.alt = cJSON_GetObjectItem(end, "altitudeMeters")->valuedouble;

        cJSON* wind_direction = cJSON_GetObjectItem(item, "windDirectionDeg");
        cJSON* wind_speed = cJSON_GetObjectItem(item, "windSpeedMs");
        if (cJSON_IsNumber(wind_direction) && cJSON_IsNumber(wind_speed)) {
            plan.segments[i].wind_direction_deg = wind_direction->valuedouble;
            plan.segments[i].wind_speed_ms = wind_speed->valuedouble;
            plan.segments[i].has_wind = 1;
        } else {
            plan.segments[i].wind_direction_deg = 0.0;
            plan.segments[i].wind_speed_ms = 0.0;
            plan.segments[i].has_wind = 0;
        }
    }

    cJSON_Delete(root);
    return plan;
}
