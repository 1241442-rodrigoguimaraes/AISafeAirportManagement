#ifndef TEST_FRAMEWORK_H
#define TEST_FRAMEWORK_H

#include <stdio.h>
#include <math.h>

/* Minimal, dependency-free unit-test helpers.
 * Each test function reports failures but never aborts, so the whole
 * suite always runs to completion and prints a final summary. */

extern int tests_run;
extern int tests_failed;

#define CHECK(cond)                                                       \
    do {                                                                  \
        tests_run++;                                                      \
        if (!(cond)) {                                                    \
            tests_failed++;                                               \
            printf("  [FAIL] %s:%d: %s\n", __FILE__, __LINE__, #cond);    \
        }                                                                 \
    } while (0)

#define CHECK_STR_EQ(a, b)      CHECK(strcmp((a), (b)) == 0)
#define CHECK_NEAR(a, b, eps)   CHECK(fabs((double)(a) - (double)(b)) < (eps))

#define RUN_SUITE(fn)                                                     \
    do {                                                                  \
        printf("== %s ==\n", #fn);                                        \
        fn();                                                             \
    } while (0)

#endif
