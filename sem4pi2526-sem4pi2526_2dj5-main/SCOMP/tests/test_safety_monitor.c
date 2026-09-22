#include "test_framework.h"
#include "simulation_types.h"
#include "safety_monitor.h"
#include <string.h>

void test_safety_monitor(void) {
    /* calculate_distance: same point is zero distance */
    Position a = {38.0, -9.0, 0.0};
    CHECK_NEAR(calculate_distance(a, a), 0.0, 1e-6);

    /* calculate_distance: 0.001 deg of latitude ~= 111.32 m */
    Position b = {38.001, -9.0, 0.0};
    CHECK_NEAR(calculate_distance(a, b), 111.32, 0.5);

    /* check_violation: overlapping positions -> critical collision */
    CHECK(check_violation(a, a) == CRITICAL_COLLISION_VIOLATION);

    /* check_violation: ~1 km apart, same altitude -> proximity (not collision) */
    Position near = {38.0, -8.988, 0.0};   /* ~1 km east */
    CHECK(check_violation(a, near) == SAFETY_PROXIMITY_VIOLATION);

    /* check_violation: far apart -> no violation */
    Position far = {41.0, -8.0, 9000.0};
    CHECK(check_violation(a, far) == NO_VIOLATION);

    /* check_violation: close horizontally but large vertical gap -> no violation */
    Position above = {38.0, -9.0, 5000.0};
    CHECK(check_violation(a, above) == NO_VIOLATION);

    /* violation_type_to_text: each enum maps to its label */
    CHECK_STR_EQ(violation_type_to_text(CRITICAL_COLLISION_VIOLATION), "Critical collision");
    CHECK_STR_EQ(violation_type_to_text(SAFETY_PROXIMITY_VIOLATION), "Proximity");
    CHECK_STR_EQ(violation_type_to_text(NO_VIOLATION), "none");

    /* calculate_velocity_vector: fewer than 2 records -> zero vector */
    FlightStatus status;
    memset(&status, 0, sizeof(status));
    status.history_count = 1;
    VelocityVector v0 = calculate_velocity_vector(&status);
    CHECK_NEAR(v0.lat_per_second, 0.0, 1e-9);

    /* calculate_velocity_vector: delta over one time step equals the raw delta */
    status.history_count = 2;
    status.history[0].time_step = 0;
    status.history[0].pos = (Position){0.0, 0.0, 100.0};
    status.history[1].time_step = 1;
    status.history[1].pos = (Position){0.5, -0.25, 130.0};
    VelocityVector v = calculate_velocity_vector(&status);
    CHECK_NEAR(v.lat_per_second, 0.5, 1e-9);
    CHECK_NEAR(v.lon_per_second, -0.25, 1e-9);
    CHECK_NEAR(v.alt_per_second, 30.0, 1e-9);
}
