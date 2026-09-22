#include "test_framework.h"
#include "simulation_types.h"
#include "report_writer.h"
#include <stdlib.h>

void test_report_writer(void) {
    /* SharedSimulationArea is large (per-flight history buffers): heap-allocate it. */
    SharedSimulationArea *area = calloc(1, sizeof(SharedSimulationArea));
    CHECK(area != NULL);
    if (!area) return;

    /* validation passes only when every flight completed and there are no violations */
    area->flights[0].execution_status = FLIGHT_STATUS_COMPLETED;
    area->flights[1].execution_status = FLIGHT_STATUS_COMPLETED;
    CHECK(calculate_validation_result(area, 2, 0) == 1);

    /* any recorded violation -> FAIL, even if all flights completed */
    CHECK(calculate_validation_result(area, 2, 1) == 0);

    /* a flight that did not complete -> FAIL */
    area->flights[1].execution_status = FLIGHT_STATUS_ERROR;
    CHECK(calculate_validation_result(area, 2, 0) == 0);

    free(area);
}
