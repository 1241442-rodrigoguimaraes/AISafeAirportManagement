#ifndef SAFETY_MONITOR_H
#define SAFETY_MONITOR_H

#include "simulation_types.h"
#include <math.h>

double calculate_distance(Position p1, Position p2);
ViolationType check_violation(Position p1, Position p2);
const char* violation_type_to_text(ViolationType violation);
VelocityVector calculate_velocity_vector(const FlightStatus *status);
void record_violation_event(SafetyViolationEvent *events, int *stored_count, int time_step, 
    const FlightStatus *flight_a, const FlightStatus *flight_b, ViolationType violation);
void* monitor_flights_worker(void *arg);

#endif