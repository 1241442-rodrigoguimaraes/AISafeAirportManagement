CC = gcc
CFLAGS = -Wall -Wextra -I../headers -I../ExternalLib -I.
LDFLAGS = -lm -lpthread -lrt

TARGET = run_tests

MODULES = ../utils.c ../parser.c ../safety_monitor.c \
          ../shared_state.c ../report_writer.c \
          ../ExternalLib/cJSON.c

TESTS = test_runner.c test_utils.c test_parser.c \
        test_safety_monitor.c test_shared_state.c \
        test_report_writer.c

$(TARGET): $(MODULES) $(TESTS)
	$(CC) $(CFLAGS) $(MODULES) $(TESTS) -o $(TARGET) $(LDFLAGS)

test: $(TARGET)
	./$(TARGET)

clean:
	rm -f $(TARGET)

.PHONY: test clean
