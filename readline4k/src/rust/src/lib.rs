use std::ffi::{c_char, c_int, c_void, CStr, CString};
use std::ptr::null_mut;

use rustyline::completion::FilenameCompleter;
use rustyline::config::{
    Behavior, BellStyle, ColorMode, CompletionType, Config, Configurer, EditMode, HistoryDuplicates,
};
use rustyline::error::ReadlineError;
use rustyline::hint::HistoryHinter;
use rustyline::history::FileHistory;
use rustyline::{DefaultEditor, Editor};
use rustyline_derive::{Completer, Helper, Highlighter, Hinter, Validator};

pub const OK: c_int = -1;
pub const ERROR_EOF: c_int = 0;
pub const ERROR_INTERRUPTED: c_int = 1;
pub const ERROR_UNKNOWN: c_int = 2;

#[repr(C)]
pub struct ReadLineResult {
    pub error: c_int,
    pub error_message: *mut c_char,
    pub result: *mut c_char,
}

impl ReadLineResult {
    pub fn leak(self) -> *mut ReadLineResult {
        let result = Box::new(self);
        let result = Box::leak(result);
        result
    }
}

impl Default for ReadLineResult {
    fn default() -> Self {
        Self {
            error: OK,
            error_message: null_mut(),
            result: null_mut(),
        }
    }
}

#[repr(C)]
pub struct EditorConfig {
    pub max_history_size: i32,
    pub history_duplicates: i32, // 0 = ALWAYS_ADD, 1 = IGNORE_CONSECUTIVE
    pub history_ignore_space: bool,
    pub completion_type: i32, // 0 = CIRCULAR, 1 = LIST
    pub completion_show_all_if_ambiguous: bool,
    pub completion_prompt_limit: i32,
    pub key_seq_timeout: i32, // millis, -1 means None
    pub edit_mode: i32,       // 0 = EMACS, 1 = VI
    pub auto_add_history: bool,
    pub bell_style: i32, // 0 = AUDIBLE, 1 = NONE, 2 = VISIBLE
    pub color_mode: i32, // 0 = ENABLED, 1 = FORCED, 2 = DISABLED
    pub behavior: i32,   // 0 = STDIO, 1 = PREFER_TERM
    pub tab_stop: u8,
    pub indent_size: u8,
    pub check_cursor_position: bool,
    pub enable_bracketed_paste: bool,
    pub enable_synchronized_output: bool,
    pub enable_signals: bool,
}

#[derive(Helper, Completer, Hinter, Highlighter, Validator)]
pub struct FileCompleterHelper {
    #[rustyline(Completer)]
    completer: FilenameCompleter,
    #[rustyline(Hinter)]
    hinter: HistoryHinter,
}

#[no_mangle]
pub extern "C" fn free_read_line_result(ptr: *mut ReadLineResult) {
    let ptr: ReadLineResult = unsafe { *Box::from_raw(ptr) };

    if ptr.error >= 0 {
        let error_message = unsafe { CString::from_raw(ptr.error_message) };
        std::mem::drop(error_message);
    }

    if ptr.result == null_mut() {
        return;
    }

    let result = unsafe { CString::from_raw(ptr.result) };
    std::mem::drop(result);
}

#[no_mangle]
pub extern "C" fn new_default_editor() -> *mut c_void {
    let rl: Editor<(), FileHistory> = DefaultEditor::new().unwrap();
    let rl = Box::new(rl);
    let rl = Box::leak(rl);
    rl as *mut _ as *mut c_void
}

#[no_mangle]
pub extern "C" fn new_editor_with_config(cfg: *const EditorConfig) -> *mut c_void {
    let cfg = unsafe { &*cfg };
    let cfg = map_config(cfg);
    let rl: Editor<(), FileHistory> = Editor::with_config(cfg).unwrap();
    let rl = Box::new(rl);
    let rl = Box::leak(rl);
    rl as *mut _ as *mut c_void
}

#[no_mangle]
pub extern "C" fn editor_read_line(rl: *mut c_void, prefix: *const c_char) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let prefix = c_chars_to_str(prefix);
    let readline: Result<String, ReadlineError> = rl.readline(prefix);
    handle_readline_result(readline)
}

#[no_mangle]
pub extern "C" fn editor_load_history(rl: *mut c_void, path: *const c_char) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let path = c_chars_to_str(path);
    let result = rl.load_history(path);
    handle_simple_result(result)
}

#[no_mangle]
pub extern "C" fn editor_add_history_entry(rl: *mut c_void, entry: *const c_char) {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let entry = c_chars_to_str(entry);
    rl.add_history_entry(entry).unwrap();
}

#[no_mangle]
pub extern "C" fn editor_save_history(rl: *mut c_void, path: *const c_char) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let path = c_chars_to_str(path);
    let result = rl.save_history(path);
    handle_simple_result(result)
}

#[no_mangle]
pub extern "C" fn editor_clear_history(rl: *mut c_void) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let result = rl.clear_history();
    handle_simple_result(result)
}

#[no_mangle]
pub extern "C" fn new_file_completer_editor_with_config(cfg: *const EditorConfig) -> *mut c_void {
    let cfg = unsafe { &*cfg };
    let cfg = map_config(cfg);
    let helper = FileCompleterHelper {
        completer: FilenameCompleter::new(),
        hinter: HistoryHinter {},
    };
    let mut rl: Editor<FileCompleterHelper, FileHistory> = Editor::with_config(cfg).unwrap();
    rl.set_helper(Some(helper));
    let rl = Box::new(rl);
    let rl = Box::leak(rl);
    rl as *mut _ as *mut c_void
}

#[no_mangle]
pub extern "C" fn file_completer_editor_read_line(
    rl: *mut c_void,
    prefix: *const c_char,
) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<FileCompleterHelper, FileHistory>) };
    let prefix = c_chars_to_str(prefix);
    let readline: Result<String, ReadlineError> = rl.readline(prefix);
    handle_readline_result(readline)
}

#[no_mangle]
pub extern "C" fn file_completer_editor_load_history(
    rl: *mut c_void,
    path: *const c_char,
) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<FileCompleterHelper, FileHistory>) };
    let path = c_chars_to_str(path);
    let result = rl.load_history(path);
    handle_simple_result(result)
}

#[no_mangle]
pub extern "C" fn file_completer_editor_add_history_entry(rl: *mut c_void, entry: *const c_char) {
    let rl = unsafe { &mut *(rl as *mut Editor<FileCompleterHelper, FileHistory>) };
    let entry = c_chars_to_str(entry);
    rl.add_history_entry(entry).unwrap();
}

#[no_mangle]
pub extern "C" fn file_completer_editor_save_history(
    rl: *mut c_void,
    path: *const c_char,
) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<FileCompleterHelper, FileHistory>) };
    let path = c_chars_to_str(path);
    let result = rl.save_history(path);
    handle_simple_result(result)
}

#[no_mangle]
pub extern "C" fn file_completer_editor_clear_history(rl: *mut c_void) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<FileCompleterHelper, FileHistory>) };
    let result = rl.clear_history();
    handle_simple_result(result)
}

fn handle_readline_result(readline: Result<String, ReadlineError>) -> *mut ReadLineResult {
    match readline {
        Ok(line) => {
            let result = ReadLineResult {
                result: CString::new(line).unwrap().into_raw(),
                ..Default::default()
            };
            result.leak()
        }
        Err(ReadlineError::Eof) => {
            let error_message = CString::new("Reached end of file").unwrap().into_raw();
            let result = ReadLineResult {
                error: ERROR_EOF,
                error_message,
                ..Default::default()
            };
            result.leak()
        }
        Err(ReadlineError::Interrupted) => {
            let error_message = CString::new("Received interrupt signal")
                .unwrap()
                .into_raw();
            let result = ReadLineResult {
                error: ERROR_INTERRUPTED,
                error_message,
                ..Default::default()
            };
            result.leak()
        }
        Err(err) => {
            let error_message = CString::new(format!("Unknown error: {:?}", err))
                .unwrap()
                .into_raw();
            let result = ReadLineResult {
                error: ERROR_UNKNOWN,
                error_message,
                ..Default::default()
            };
            result.leak()
        }
    }
}

fn handle_simple_result(res: Result<(), ReadlineError>) -> *mut ReadLineResult {
    match res {
        Ok(_) => {
            let result = ReadLineResult {
                ..Default::default()
            };
            result.leak()
        }
        Err(err) => {
            let error_message = CString::new(format!("Unknown error: {:?}", err))
                .unwrap()
                .into_raw();
            let result = ReadLineResult {
                error: ERROR_UNKNOWN,
                error_message,
                ..Default::default()
            };
            result.leak()
        }
    }
}

fn c_chars_to_str<'a>(c_chars: *const c_char) -> &'a str {
    unsafe { CStr::from_ptr(c_chars).to_str().unwrap() }
}

#[no_mangle]
pub extern "C" fn free_editor(ptr: *mut c_void) {
    let _editor: Box<Editor<(), FileHistory>> = unsafe { Box::from_raw(ptr as *mut _) };
    // Box will be dropped automatically
}

#[no_mangle]
pub extern "C" fn free_file_completer_editor(ptr: *mut c_void) {
    let _editor: Box<Editor<FileCompleterHelper, FileHistory>> =
        unsafe { Box::from_raw(ptr as *mut _) };
    // Box will be dropped automatically
}

fn map_config(cfg: &EditorConfig) -> Config {
    let history_dupes = match cfg.history_duplicates {
        0 => HistoryDuplicates::AlwaysAdd,
        _ => HistoryDuplicates::IgnoreConsecutive,
    };
    let completion_type = match cfg.completion_type {
        1 => CompletionType::List,
        _ => CompletionType::Circular,
    };
    let edit_mode = match cfg.edit_mode {
        1 => EditMode::Vi,
        _ => EditMode::Emacs,
    };
    let bell_style = match cfg.bell_style {
        1 => BellStyle::None,
        2 => BellStyle::Visible,
        _ => BellStyle::Audible,
    };
    let color_mode = match cfg.color_mode {
        1 => ColorMode::Forced,
        2 => ColorMode::Disabled,
        _ => ColorMode::Enabled,
    };
    let behavior = match cfg.behavior {
        1 => Behavior::PreferTerm,
        _ => Behavior::Stdio,
    };
    let keyseq_timeout: Option<u16> = if cfg.key_seq_timeout >= 0 {
        Some(cfg.key_seq_timeout as u16)
    } else {
        None
    };

    let mut builder = Config::builder();
    builder = builder
        .max_history_size(cfg.max_history_size.max(0) as usize)
        .unwrap();
    // Fallback to boolean API for duplicates handling
    builder = builder
        .history_ignore_dups(!matches!(history_dupes, HistoryDuplicates::AlwaysAdd))
        .unwrap();
    builder = builder.history_ignore_space(cfg.history_ignore_space);
    builder = builder.completion_type(completion_type);
    builder = builder.completion_show_all_if_ambiguous(cfg.completion_show_all_if_ambiguous);
    builder = builder.completion_prompt_limit(cfg.completion_prompt_limit.max(0) as usize);
    builder = builder.keyseq_timeout(keyseq_timeout);
    builder = builder.edit_mode(edit_mode);
    builder = builder.auto_add_history(cfg.auto_add_history);
    builder = builder.bell_style(bell_style);
    builder = builder.color_mode(color_mode);
    builder = builder.behavior(behavior);
    builder = builder.tab_stop(cfg.tab_stop);
    builder = builder.indent_size(cfg.indent_size);
    builder = builder.check_cursor_position(cfg.check_cursor_position);
    builder = builder.bracketed_paste(cfg.enable_bracketed_paste);
    builder.enable_synchronized_output(cfg.enable_synchronized_output);
    builder = builder.enable_signals(cfg.enable_signals);
    builder.build()
}
