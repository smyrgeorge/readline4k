#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#define OK -1

#define ERROR_EOF 0

#define ERROR_INTERRUPTED 1

#define ERROR_UNKNOWN 2

typedef struct ReadLineResult {
  int error;
  char *error_message;
  char *result;
} ReadLineResult;

void free_read_line_result(struct ReadLineResult *ptr);

void *new_default_editor(void);

struct ReadLineResult *editor_read_line(void *rl, const char *prefix);

struct ReadLineResult *editor_load_history(void *rl, const char *path);

void editor_add_history_entry(void *rl, const char *entry);

struct ReadLineResult *editor_save_history(void *rl, const char *path);
