#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

void *new_default_editor(void);

char *editor_read_line(void *rl, const char *prefix);

void editor_load_history(void *rl, const char *path);

void editor_add_history_entry(void *rl, const char *entry);

void editor_save_history(void *rl, const char *path);
