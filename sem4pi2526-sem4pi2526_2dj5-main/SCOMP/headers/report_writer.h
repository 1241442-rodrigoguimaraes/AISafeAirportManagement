#ifndef REPORT_WRITER_H
#define REPORT_WRITER_H

#include "simulation_types.h"
#include "stdio.h"

void* generate_simulation_report(void *arg);
void* report_generation_worker(void *arg);
int calculate_validation_result(SharedSimulationArea *shared_data, int total_flights, int violation_count);
void ensure_report_directory(void);
void write_position(FILE *report, const char *label, Position position);
void write_velocity(FILE *report, const char *label, VelocityVector velocity);

#endif
