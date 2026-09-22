#ifndef FLIGHT_RUNNER_H
#define FLIGHT_RUNNER_H

#include <signal.h>
#include <semaphore.h>
#include "simulation_types.h"

extern int global_step_pipe_fd;

void close_child_inherited_pipes(int current_index, int step_pipes[][2]);
void handle_sigusr1(int sig, siginfo_t* info, void* context);
void run_flight(FlightPlan plan, int step_pipe_fd, SharedSimulationArea* shared_data, int i, sem_t* sem_mem);
void* step_engine_worker(void *arg);

#endif