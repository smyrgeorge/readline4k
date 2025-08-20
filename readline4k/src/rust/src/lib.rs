use std::ffi::{c_char, c_int, c_void, CStr, CString};
use std::ptr::null_mut;

use rustyline::error::ReadlineError;
use rustyline::history::FileHistory;
use rustyline::{DefaultEditor, Editor};

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
pub extern "C" fn editor_read_line(rl: *mut c_void, prefix: *const c_char) -> *mut ReadLineResult {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let prefix = c_chars_to_str(prefix);
    let readline: Result<String, ReadlineError> = rl.readline(prefix);
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
