#ifndef SHARED_DATA_H
#define SHARED_DATA_H

#include "simulation_types.h"

void deactivate_flight(FlightStatus *status, int *step_fd, int *active_flights, FlightExecutionStatus final_status);
void update_status_from_child_exit(FlightStatus *status, int child_status);
const char* flight_status_to_text(FlightExecutionStatus status);
void close_fd_if_open(int *fd);

#endif