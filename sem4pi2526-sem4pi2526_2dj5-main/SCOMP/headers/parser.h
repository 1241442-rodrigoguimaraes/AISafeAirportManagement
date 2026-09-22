#ifndef PARSER_H
#define PARSER_H

#include "simulation_types.h"

int derive_flight_id(const char *flight_designator);
FlightPlan parse_fp_flight_plan(const char* filepath);
FlightPlan parse_json_flight_plan(const char* filepath);

#endif