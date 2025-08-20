use std::ffi::{c_char, c_void, CStr, CString};
use std::ptr::null_mut;

use rustyline::error::ReadlineError;
use rustyline::history::FileHistory;
use rustyline::{DefaultEditor, Editor};

#[no_mangle]
pub extern "C" fn new_default_editor() -> *mut c_void {
    let rl: Editor<(), FileHistory> = DefaultEditor::new().unwrap();
    let rl = Box::new(rl);
    let rl = Box::leak(rl);
    rl as *mut _ as *mut c_void
}

#[no_mangle]
pub extern "C" fn editor_read_line(rl: *mut c_void, prefix: *const c_char) -> *mut c_char {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let prefix = c_chars_to_str(prefix);
    let readline = rl.readline(prefix);
    match readline {
        Ok(line) => CString::new(line).unwrap().into_raw(),
        Err(ReadlineError::Interrupted) => {
            println!("CTRL-C");
            null_mut()
        }
        Err(ReadlineError::Eof) => {
            println!("CTRL-D");
            null_mut()
        }
        Err(err) => {
            println!("Error: {:?}", err);
            null_mut()
        }
    }
}

#[no_mangle]
pub extern "C" fn editor_load_history(rl: *mut c_void, path: *const c_char) {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let path = c_chars_to_str(path);
    rl.load_history(path).unwrap();
}

#[no_mangle]
pub extern "C" fn editor_add_history_entry(rl: *mut c_void, entry: *const c_char) {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let entry = c_chars_to_str(entry);
    rl.add_history_entry(entry).unwrap();
}

#[no_mangle]
pub extern "C" fn editor_save_history(rl: *mut c_void, path: *const c_char) {
    let rl = unsafe { &mut *(rl as *mut Editor<(), FileHistory>) };
    let path = c_chars_to_str(path);
    rl.save_history(path).unwrap();
}

pub fn c_chars_to_str<'a>(c_chars: *const c_char) -> &'a str {
    unsafe { CStr::from_ptr(c_chars).to_str().unwrap() }
}
