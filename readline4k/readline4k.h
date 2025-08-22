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

typedef struct EditorConfig {
  int32_t max_history_size;
  int32_t history_duplicates;
  bool history_ignore_space;
  int32_t completion_type;
  bool completion_show_all_if_ambiguous;
  int32_t completion_prompt_limit;
  int32_t key_seq_timeout;
  int32_t edit_mode;
  bool auto_add_history;
  int32_t bell_style;
  int32_t color_mode;
  int32_t behavior;
  uint8_t tab_stop;
  uint8_t indent_size;
  bool check_cursor_position;
  bool enable_bracketed_paste;
  bool enable_synchronized_output;
  bool enable_signals;
} EditorConfig;

void free_read_line_result(struct ReadLineResult *ptr);

void *new_default_editor(void);

void *new_editor_with_config(const struct EditorConfig *cfg);

struct ReadLineResult *editor_read_line(void *rl, const char *prefix);

struct ReadLineResult *editor_load_history(void *rl, const char *path);

void editor_add_history_entry(void *rl, const char *entry);

struct ReadLineResult *editor_save_history(void *rl, const char *path);

struct ReadLineResult *editor_clear_history(void *rl);

void *new_file_completer_editor_with_config(const struct EditorConfig *cfg);

struct ReadLineResult *file_completer_editor_read_line(void *rl, const char *prefix);

struct ReadLineResult *file_completer_editor_load_history(void *rl, const char *path);

void file_completer_editor_add_history_entry(void *rl, const char *entry);

struct ReadLineResult *file_completer_editor_save_history(void *rl, const char *path);

struct ReadLineResult *file_completer_editor_clear_history(void *rl);

void free_editor(void *ptr);

void free_file_completer_editor(void *ptr);
