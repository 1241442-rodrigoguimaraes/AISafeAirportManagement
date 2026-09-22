#include "test_framework.h"
#include "simulation_types.h"
#include "utils.h"
#include <string.h>

void test_utils(void) {
    /* copy_mode: normal copy */
    char mode[MAX_MODE_LENGTH];
    copy_mode(mode, "cruise");
    CHECK_STR_EQ(mode, "cruise");

    /* copy_mode: NULL source falls back to "unknown" */
    copy_mode(mode, NULL);
    CHECK_STR_EQ(mode, "unknown");

    /* copy_mode: oversized source is truncated and null-terminated */
    copy_mode(mode, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    CHECK(strlen(mode) == MAX_MODE_LENGTH - 1);
    CHECK(mode[MAX_MODE_LENGTH - 1] == '\0');

    /* copy_designator: normal copy and NULL fallback */
    char desig[MAX_DESIGNATOR_LENGTH];
    copy_designator(desig, "TP123");
    CHECK_STR_EQ(desig, "TP123");
    copy_designator(desig, NULL);
    CHECK_STR_EQ(desig, "unknown");

    /* has_extension: matching, non-matching, and too-short filename */
    CHECK(has_extension("flightPlan.fp", ".fp") == 1);
    CHECK(has_extension("flightPlan.json", ".fp") == 0);
    CHECK(has_extension("fp", ".fp") == 0);          /* shorter than extension */
    CHECK(has_extension(".fp", ".fp") == 1);         /* exact length match */
}
