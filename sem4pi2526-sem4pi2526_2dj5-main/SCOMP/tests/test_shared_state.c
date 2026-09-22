#include "test_framework.h"
#include "simulation_types.h"
#include "shared_state.h"
#include <string.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/wait.h>

void test_shared_state(void) {
    /* flight_status_to_text: each status maps to its label */
    CHECK_STR_EQ(flight_status_to_text(FLIGHT_STATUS_RUNNING), "RUNNING");
    CHECK_STR_EQ(flight_status_to_text(FLIGHT_STATUS_COMPLETED), "COMPLETED");
    CHECK_STR_EQ(flight_status_to_text(FLIGHT_STATUS_TERMINATED_SAFETY), "TERMINATED_SAFETY");
    CHECK_STR_EQ(flight_status_to_text(FLIGHT_STATUS_ERROR), "ERROR");

    /* close_fd_if_open: a real open fd is closed and reset to -1 */
    int fd = open("/dev/null", O_RDONLY);
    CHECK(fd >= 0);
    close_fd_if_open(&fd);
    CHECK(fd == -1);
    /* already closed -> no-op, stays -1 */
    close_fd_if_open(&fd);
    CHECK(fd == -1);

    /* deactivate_flight: running flight becomes inactive and count drops */
    FlightStatus status;
    memset(&status, 0, sizeof(status));
    status.active = 1;
    status.execution_status = FLIGHT_STATUS_RUNNING;
    int active_flights = 2;
    int step_fd = -1;
    deactivate_flight(&status, &step_fd, &active_flights, FLIGHT_STATUS_COMPLETED);
    CHECK(status.active == 0);
    CHECK(active_flights == 1);
    CHECK(status.execution_status == FLIGHT_STATUS_COMPLETED);

    /* deactivate_flight: a final status is only set while RUNNING, not overwritten */
    FlightStatus terminated;
    memset(&terminated, 0, sizeof(terminated));
    terminated.active = 1;
    terminated.execution_status = FLIGHT_STATUS_TERMINATED_SAFETY;
    int af = 1;
    int fd2 = -1;
    deactivate_flight(&terminated, &fd2, &af, FLIGHT_STATUS_COMPLETED);
    CHECK(terminated.active == 0);
    CHECK(af == 0);
    CHECK(terminated.execution_status == FLIGHT_STATUS_TERMINATED_SAFETY);

    /* update_status_from_child_exit: clean exit (code 0) -> COMPLETED */
    FlightStatus clean;
    memset(&clean, 0, sizeof(clean));
    clean.execution_status = FLIGHT_STATUS_RUNNING;
    int ok_status = 0;                       /* WIFEXITED true, WEXITSTATUS 0 */
    update_status_from_child_exit(&clean, ok_status);
    CHECK(clean.execution_status == FLIGHT_STATUS_COMPLETED);

    /* update_status_from_child_exit: non-zero exit -> ERROR */
    FlightStatus bad;
    memset(&bad, 0, sizeof(bad));
    bad.execution_status = FLIGHT_STATUS_RUNNING;
    int fail_status = (1 << 8);              /* WIFEXITED true, WEXITSTATUS 1 */
    update_status_from_child_exit(&bad, fail_status);
    CHECK(bad.execution_status == FLIGHT_STATUS_ERROR);

    /* update_status_from_child_exit: a safety termination is preserved */
    FlightStatus safety;
    memset(&safety, 0, sizeof(safety));
    safety.execution_status = FLIGHT_STATUS_TERMINATED_SAFETY;
    update_status_from_child_exit(&safety, ok_status);
    CHECK(safety.execution_status == FLIGHT_STATUS_TERMINATED_SAFETY);
}
