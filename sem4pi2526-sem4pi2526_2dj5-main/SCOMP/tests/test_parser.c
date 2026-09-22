#include "test_framework.h"
#include "simulation_types.h"
#include "parser.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/* A minimal but valid .fp flight plan exercising the .fp parser. */
static const char *SAMPLE_FP =
    "flight TEST1 {\n"
    "    segment {\n"
    "        mode: climb\n"
    "        start: { latitude: 38.0 longitude: -9.0 altitude: 100 m }\n"
    "        end: { latitude: 39.0 longitude: -8.0 altitude: 9000 m }\n"
    "    }\n"
    "    segment {\n"
    "        mode: cruise\n"
    "        start: { latitude: 39.0 longitude: -8.0 altitude: 9000 m }\n"
    "        end: { latitude: 41.0 longitude: -8.7 altitude: 9000 m }\n"
    "    }\n"
    "}\n";

static void write_temp_fp(const char *path, const char *content) {
    FILE *f = fopen(path, "wb");
    if (!f) { perror("temp fp"); exit(EXIT_FAILURE); }
    fputs(content, f);
    fclose(f);
}

void test_parser(void) {
    /* derive_flight_id: deterministic, non-zero, and distinct for distinct input */
    int id_a = derive_flight_id("TP123");
    int id_b = derive_flight_id("TP123");
    int id_c = derive_flight_id("TP124");
    CHECK(id_a != 0);
    CHECK(id_a == id_b);          /* deterministic */
    CHECK(id_a != id_c);          /* different designators -> different ids */
    CHECK(id_a > 0);              /* masked to a positive int */

    /* parse_fp_flight_plan: valid file is parsed into the expected structure */
    const char *path = "_tmp_plan.fp";
    write_temp_fp(path, SAMPLE_FP);

    FlightPlan plan = parse_fp_flight_plan(path);
    CHECK_STR_EQ(plan.designator, "TEST1");
    CHECK(plan.id == derive_flight_id("TEST1"));
    CHECK(plan.num_segments == 2);

    CHECK_STR_EQ(plan.segments[0].mode, "climb");
    CHECK_NEAR(plan.segments[0].start.lat, 38.0, 1e-9);
    CHECK_NEAR(plan.segments[0].start.lon, -9.0, 1e-9);
    CHECK_NEAR(plan.segments[0].start.alt, 100.0, 1e-9);
    CHECK_NEAR(plan.segments[0].end.alt, 9000.0, 1e-9);

    CHECK_STR_EQ(plan.segments[1].mode, "cruise");
    CHECK_NEAR(plan.segments[1].end.lat, 41.0, 1e-9);

    free(plan.segments);
    remove(path);
}
