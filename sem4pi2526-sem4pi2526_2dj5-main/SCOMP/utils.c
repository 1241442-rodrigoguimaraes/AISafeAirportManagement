#include "simulation_types.h"
#include "string.h"
#include "stddef.h"
#include "utils.h"

void copy_mode(char *destination, const char *source) {
    strncpy(destination, source != NULL ? source : "unknown", MAX_MODE_LENGTH - 1);
    destination[MAX_MODE_LENGTH - 1] = '\0';
}

void copy_designator(char *destination, const char *source) {
    strncpy(destination, source != NULL ? source : "unknown", MAX_DESIGNATOR_LENGTH - 1);
    destination[MAX_DESIGNATOR_LENGTH - 1] = '\0';
}

int has_extension(const char *filename, const char *extension) {
    size_t filename_length = strlen(filename);
    size_t extension_length = strlen(extension);

    if (filename_length < extension_length) {
        return 0;
    }

    return strcmp(filename + filename_length - extension_length, extension) == 0;
}
