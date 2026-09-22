#ifndef FLIGHT_SIMULATION_H
#define FLIGHT_SIMULATION_H

#include <sys/types.h>
#include <semaphore.h>
#include <pthread.h>

#define MAX_HISTORY 1000
#define MAX_MODE_LENGTH 20
#define MAX_DESIGNATOR_LENGTH 64
#define MAX_PATH_LENGTH 512
#define MAX_VIOLATION_LIMIT 5
#define HORIZONTAL_LIMIT 9620.0
#define VERTICAL_LIMIT 300.0
#define MIN_COLLISION_DISTANCE 150.0
#define DEGREES_TO_METERS 111320.0
#define TIME_STEP_SECONDS 1
#define MAX_VIOLATION_EVENTS 1000
#define MAX_FLIGHTS 50
#define REPORT_OUTPUT_DIR "./reports"
#define REPORT_OUTPUT_FILE "./reports/simulation_report.txt"

typedef struct {
    double lat;
    double lon;
    double alt;
} Position;

typedef struct {
    double wind_speed_ms;
    double wind_direction_deg;
    int time_step;
    int available;
} WeatherStats;

typedef struct {
    char mode[MAX_MODE_LENGTH];
    Position start;
    Position end;
    double wind_speed_ms;
    double wind_direction_deg;
    int has_wind;
} FlightSegment;

typedef struct {
    int id;
    char designator[MAX_DESIGNATOR_LENGTH];
    int num_segments;
    FlightSegment *segments;
} FlightPlan;

typedef struct {
    double lat_per_second;
    double lon_per_second;
    double alt_per_second;
} VelocityVector;

typedef enum {
    NO_VIOLATION,
    SAFETY_PROXIMITY_VIOLATION,
    CRITICAL_COLLISION_VIOLATION
} ViolationType;

typedef enum {
    FLIGHT_STATUS_RUNNING,
    FLIGHT_STATUS_COMPLETED,
    FLIGHT_STATUS_TERMINATED_SAFETY,
    FLIGHT_STATUS_ERROR
} FlightExecutionStatus;

typedef struct {
    int time_step;
    Position pos;
    char mode[MAX_MODE_LENGTH];
} PositionRecord;

typedef struct {
    int id;
    char designator[MAX_DESIGNATOR_LENGTH];
    pid_t pid;
    Position last_pos;
    PositionRecord history[MAX_HISTORY];
    int history_count;
    int active;
    int has_position;
    int updated_in_step;
    int completed;
    int violation_counter;
    FlightExecutionStatus execution_status;
    WeatherStats weather;
} FlightStatus;

typedef struct {
    int time_step;
    int flight_id_a;
    int flight_id_b;
    char flight_designator_a[MAX_DESIGNATOR_LENGTH];
    char flight_designator_b[MAX_DESIGNATOR_LENGTH];
    ViolationType violation_type;
    Position position_a;
    Position position_b;
    VelocityVector velocity_a;
    VelocityVector velocity_b;
    double horizontal_distance;
    double vertical_distance;
} SafetyViolationEvent;

typedef struct {
    int time_step;
} StepCommand;

typedef struct {
    FlightStatus flights[MAX_FLIGHTS];
    int total_flights;
    WeatherStats weather;
} SharedSimulationArea;

typedef struct {
    SharedSimulationArea *shared_data;
    sem_t *sem_mem;
    pthread_mutex_t mutex;
    pthread_cond_t cond_violation;
    FlightPlan *plans;
    int num_plans;
    int (*step_pipes)[2];
    int active_flights;
    int global_step;
    SafetyViolationEvent *violation_events;
    int *stored_violation_count;
    int simulation_done;
    int report_shutdown;
} SimulationContext;

typedef struct {
    int total_flights;
    SharedSimulationArea *shared_data;
    int total_violation_count;
    SafetyViolationEvent *violation_events;
    const char *report_path;
} SimulationReportData;

#endif
