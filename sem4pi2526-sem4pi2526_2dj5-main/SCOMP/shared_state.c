#include "simulation_types.h"
#include "shared_state.h"
#include "utils.h"
#include "stdlib.h"
#include "unistd.h"

void deactivate_flight(FlightStatus *status, int *step_fd, int *active_flights, FlightExecutionStatus final_status) {
    if (status->active) {
        status->active = 0;
        (*active_flights)--;
    }

    if (status->execution_status == FLIGHT_STATUS_RUNNING) {
        status->execution_status = final_status;
    }

    close_fd_if_open(step_fd);
}

void update_status_from_child_exit(FlightStatus *status, int child_status) {
    if (status->execution_status == FLIGHT_STATUS_TERMINATED_SAFETY ||
        status->execution_status == FLIGHT_STATUS_ERROR) {
        return;
    }

    if (WIFEXITED(child_status) && WEXITSTATUS(child_status) == 0) {
        if (status->execution_status == FLIGHT_STATUS_RUNNING) {
            status->execution_status = FLIGHT_STATUS_COMPLETED;
        }
        return;
    }

    status->execution_status = FLIGHT_STATUS_ERROR;
}

const char* flight_status_to_text(FlightExecutionStatus status) {
    switch (status) {
        case FLIGHT_STATUS_COMPLETED:
            return "COMPLETED";
        case FLIGHT_STATUS_TERMINATED_SAFETY:
            return "TERMINATED_SAFETY";
        case FLIGHT_STATUS_ERROR:
            return "ERROR";
        case FLIGHT_STATUS_RUNNING:
        default:
            return "RUNNING";
    }
}

void close_fd_if_open(int *fd) {
    if (*fd != -1) {
        close(*fd);
        *fd = -1;
    }
}
