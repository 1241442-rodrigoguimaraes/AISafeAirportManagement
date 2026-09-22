#include "test_framework.h"
#include <stdio.h>

int tests_run = 0;
int tests_failed = 0;

void test_utils(void);
void test_parser(void);
void test_safety_monitor(void);
void test_shared_state(void);
void test_report_writer(void);

int main(void) {
    printf("### SCOMP unit tests ###\n\n");

    RUN_SUITE(test_utils);
    RUN_SUITE(test_parser);
    RUN_SUITE(test_safety_monitor);
    RUN_SUITE(test_shared_state);
    RUN_SUITE(test_report_writer);

    printf("\n----------------------------------------\n");
    printf("Checks run: %d | Passed: %d | Failed: %d\n",
           tests_run, tests_run - tests_failed, tests_failed);

    if (tests_failed == 0) {
        printf("RESULT: ALL TESTS PASSED\n");
        return 0;
    }
    printf("RESULT: %d CHECK(S) FAILED\n", tests_failed);
    return 1;
}
