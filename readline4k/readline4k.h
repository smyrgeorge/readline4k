#include <stdarg.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

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

typedef char *(*CompleterCallCb)(void *k_callback_holder, const char *line, int pos, int *out_start);

typedef char *(*HighlighterCb)(void *k_callback_holder, const char *line, int pos);

typedef char *(*HintHighlighterCb)(void *k_callback_holder, const char *hint);

typedef char *(*PromptHighlighterCb)(void *k_callback_holder, const char *prompt, bool is_default);

typedef char *(*CandidateHighlighterCb)(void *k_callback_holder,
                                        const char *candidate,
                                        int completion);

typedef bool (*CharHighlighterCb)(void *k_callback_holder, const char *line, int pos, int kind);

void free_read_line_result(struct ReadLineResult *ptr);

void *new_editor_with_config(const struct EditorConfig *cfg, void *k_callback_holder);

void editor_set_completer(void *rl, CompleterCallCb cb);

void editor_set_highlighter(void *rl, HighlighterCb cb);

void editor_set_hint_highlighter(void *rl, HintHighlighterCb cb);

void editor_set_prompt_highlighter(void *rl, PromptHighlighterCb cb);

void editor_set_candidate_highlighter(void *rl, CandidateHighlighterCb cb);

void editor_set_char_highlighter(void *rl, CharHighlighterCb cb);

struct ReadLineResult *editor_read_line(void *rl, const char *prefix);

struct ReadLineResult *editor_load_history(void *rl, const char *path);

void editor_add_history_entry(void *rl, const char *entry);

struct ReadLineResult *editor_save_history(void *rl, const char *path);

struct ReadLineResult *editor_clear_history(void *rl);

struct ReadLineResult *editor_clear_screen(void *rl);

void editor_set_cursor_visibility(void *rl, bool visible);

void editor_set_auto_add_history(void *rl, bool value);

void editor_set_color_mode(void *rl, int value);

void free_editor(void *ptr);

extern void free(void *ptr);
