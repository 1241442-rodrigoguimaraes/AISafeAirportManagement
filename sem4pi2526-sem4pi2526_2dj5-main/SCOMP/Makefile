CC = gcc
CFLAGS = -Wall -Wextra -Iheaders -IExternalLib -MMD -MP
LDFLAGS = -lm -lpthread -lrt

TARGET = flight_simulation
SRC = main.c parser.c flight_runner.c safety_monitor.c \
	  shared_state.c report_writer.c weather.c utils.c \
      ExternalLib/cJSON.c
OBJ = $(SRC:.c=.o)
DEP = $(OBJ:.o=.d)

$(TARGET): $(OBJ)
	$(CC) $(OBJ) -o $(TARGET) $(LDFLAGS)

%.o: %.c
	$(CC) $(CFLAGS) -c $< -o $@

-include $(DEP)

test:
	$(MAKE) -C tests test

clean:
	rm -f $(OBJ) $(DEP) $(TARGET)
	$(MAKE) -C tests clean

.PHONY: test clean
