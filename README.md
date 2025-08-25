# readline4k

![Build](https://github.com/smyrgeorge/readline4k/actions/workflows/ci.yml/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.github.smyrgeorge/readline4k)
![GitHub License](https://img.shields.io/github/license/smyrgeorge/readline4k)
![GitHub commit activity](https://img.shields.io/github/commit-activity/w/smyrgeorge/readline4k)
![GitHub issues](https://img.shields.io/github/issues/smyrgeorge/readline4k)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

![](https://img.shields.io/static/v1?label=&message=Platforms&color=grey)
![](https://img.shields.io/static/v1?label=&message=Linux&color=blue)
![](https://img.shields.io/static/v1?label=&message=macOS&color=blue)
![](https://img.shields.io/static/v1?label=&message=Windows&color=blue)

Cross-platform Kotlin/Native readline library with history support for interactive console apps.

📖 [Documentation](https://smyrgeorge.github.io/readline4k/)

🏠 [Homepage](https://smyrgeorge.github.io/) (under construction)

> [!IMPORTANT]  
> The project is in a very early stage; thus, breaking changes should be expected.

## Supported Platforms

- Unix (tested on FreeBSD, Linux, and macOS)
- Windows
    - cmd.exe
    - Powershell

### Note that:

- Powershell ISE is not supported
- Mintty (Cygwin/MinGW) is not supported
- Highlighting / Colors are not supported on Windows < Windows 10 except with ConEmu and ColorMode::Forced.

## Features

- Cross-platform line editing for Unix (Linux, macOS, FreeBSD) and Windows consoles.
- Simple, composable API:
  - Read one line with a prompt prefix and get Result<String> back (non-throwing API).
  - Clear screen, manage history, and attach completion/highlighting strategies.
- History management:
  - In-memory history with max size and duplicate handling policy.
  - Load from/save to a file, clear history, and optional auto-add on successful read.
  - Optionally ignore lines starting with a space.
- Pluggable completion:
  - Interface-based Completer with cursor-aware token replacement.
  - Built-in SimpleFileCompleter for filesystem paths (tilde expansion, hidden files rules, dir trailing slash).
  - Multiple completion modes: Circular cycling or List with common-prefix and paging; show-all-if-ambiguous option.
- Configurable highlighting:
  - Highlighter interface to style prompt, inline hints, and candidates (e.g., via ANSI colors).
  - Color modes: Enabled, Forced, or Disabled to match terminal capabilities.
- Editing behavior and keymaps:
  - Emacs or Vi editing modes.
  - Bell styles: audible or none (with sensible Windows default).
- Terminal features (where supported):
  - Bracketed paste, synchronized output, and signal handling on Unix-like systems.
- I/O behavior:
  - STDIO by default or Prefer terminal behavior when available.

## Quick start

A minimal REPL-style loop with history persistence:

```kotlin
fun main() {
    val history = "history.txt" // Filesystem path to the history file.

    // Configure the LineEditor.
    val config = LineEditorConfig(
        maxHistorySize = 100,
        completionType = CompletionType.LIST,
        // See the documentation for more options.
    )

    // Create a new LineEditor instance.
    val editor = SimpleLineEditor(
        linePrefix = "> ",
        config = config,
    ).also { editor ->
        // Set up the completer and highlighter.
        editor
            .withCompleter(SimpleFileCompleter()) // Provides file completion (optional).
            .withHighlighter(SimpleHighlighter()) // Provides color highlighting (optional).

        // Load the history from the disk (throws LineEditorError if it fails).
        editor.loadHistory(history).getOrThrow()
    }

    println("Welcome to the LineEditor example!")
    println("Press Ctrl+C to exit")

    while (true) {
        // Read a line from the user.
        editor.readLine()
            .onFailure { err ->
                // err is a LineEditorError
                println(err.message)
                break

            }
            .onSuccess { line ->
                editor.addHistoryEntry(line)
                println(line)
            }
    }

    // Save the history to disk.
    editor.saveHistory(history)
}
```

## Actions

For all modes:

| Keystroke             | Action                                                                      |
|-----------------------|-----------------------------------------------------------------------------|
| Home                  | Move cursor to the beginning of line                                        |
| End                   | Move cursor to end of line                                                  |
| Left                  | Move cursor one character left                                              |
| Right                 | Move cursor one character right                                             |
| Ctrl-C                | Interrupt/Cancel edition                                                    |
| Ctrl-D, Del           | (if line is _not_ empty) Delete character under cursor                      |
| Ctrl-D                | (if line _is_ empty) End of File                                            |
| Ctrl-J, Ctrl-M, Enter | Finish the line entry                                                       |
| Ctrl-R                | Reverse Search history (Ctrl-S forward, Ctrl-G cancel)                      |
| Ctrl-T                | Transpose previous character with current character                         |
| Ctrl-U                | Delete from start of line to cursor                                         |
| Ctrl-V (unix)         | Insert any special character without performing its associated action (#65) |
| Ctrl-V (windows)      | Paste from clipboard                                                        |
| Ctrl-W                | Delete word leading up to cursor (using white space as a word boundary)     |
| Ctrl-Y                | Paste from Yank buffer                                                      |
| Ctrl-Z                | Suspend (Unix only)                                                         |
| Ctrl-\_               | Undo                                                                        |

### Emacs mode (default mode)

| Keystroke         | Action                                                                                           |
|-------------------|--------------------------------------------------------------------------------------------------|
| Ctrl-A, Home      | Move cursor to the beginning of line                                                             |
| Ctrl-B, Left      | Move cursor one character left                                                                   |
| Ctrl-E, End       | Move cursor to end of line                                                                       |
| Ctrl-F, Right     | Move cursor one character right (or complete hint if cursor is at the end of line)               |
| Ctrl-H, Backspace | Delete character before cursor                                                                   |
| Shift-Tab         | Previous completion                                                                              |
| Ctrl-I, Tab       | Next completion                                                                                  |
| Ctrl-K            | Delete from cursor to end of line                                                                |
| Ctrl-L            | Clear screen                                                                                     |
| Ctrl-N, Down      | Next match from history                                                                          |
| Ctrl-P, Up        | Previous match from history                                                                      |
| Ctrl-X Ctrl-G     | Abort                                                                                            |
| Ctrl-X Esc        | Abort                                                                                            |
| Ctrl-X Ctrl-U     | Undo                                                                                             |
| Ctrl-X Backspace  | Delete from cursor to the beginning of line                                                      |
| Ctrl-Y            | Paste from Yank buffer (Meta-Y to paste next yank instead)                                       |
| Ctrl-] <char>     | Search character forward                                                                         |
| Ctrl-Alt-] <char> | Search character backward                                                                        |
| Meta-<            | Move to first entry in history                                                                   |
| Meta->            | Move to last entry in history                                                                    |
| Meta-B, Alt-Left  | Move cursor to previous word                                                                     |
| Ctrl-Left         | See Alt-Left                                                                                     |
| Meta-C            | Capitalize the current word                                                                      |
| Meta-D            | Delete forwards one word                                                                         |
| Meta-F, Alt-Right | Move cursor to next word                                                                         |
| Ctrl-Right        | See Alt-Right                                                                                    |
| Meta-L            | Lower-case the next word                                                                         |
| Meta-T            | Transpose words                                                                                  |
| Meta-U            | Upper-case the next word                                                                         |
| Meta-Y            | See Ctrl-Y                                                                                       |
| Meta-Backspace    | Kill from the start of the current word, or, if between words, to the start of the previous word |
| Meta-0, 1, ..., - | Specify the digit to the argument. `–` starts a negative argument.                               |

[Readline Emacs Editing Mode Cheat Sheet](http://www.catonmat.net/download/readline-emacs-editing-mode-cheat-sheet.pdf)

### vi command mode

| Keystroke            | Action                                                                      |
|----------------------|-----------------------------------------------------------------------------|
| $, End               | Move cursor to end of line                                                  |
| .                    | Redo the last text modification                                             |
| ;                    | Redo the last character finding command                                     |
| ,                    | Redo the last character finding command in opposite direction               |
| 0, Home              | Move cursor to the beginning of line                                        |
| ^                    | Move to the first non-blank character of line                               |
| a                    | Insert after cursor                                                         |
| A                    | Insert at the end of line                                                   |
| b                    | Move one word or token left                                                 |
| B                    | Move one non-blank word left                                                |
| c<movement>          | Change text of a movement command                                           |
| C                    | Change text to the end of line (equivalent to c$)                           |
| d<movement>          | Delete text of a movement command                                           |
| D, Ctrl-K            | Delete to the end of the line                                               |
| e                    | Move to the end of the current word                                         |
| E                    | Move to the end of the current non-blank word                               |
| f<char>              | Move right to the next occurrence of `char`                                 |
| F<char>              | Move left to the previous occurrence of `char`                              |
| h, Ctrl-H, Backspace | Move one character left                                                     |
| l, Space             | Move one character right                                                    |
| Ctrl-L               | Clear screen                                                                |
| i                    | Insert before cursor                                                        |
| I                    | Insert at the beginning of line                                             |
| +, j, Ctrl-N         | Move forward one command in history                                         |
| -, k, Ctrl-P         | Move backward one command in history                                        |
| p                    | Insert the yanked text at the cursor (paste)                                |
| P                    | Insert the yanked text before the cursor                                    |
| r                    | Replaces a single character under the cursor (without leaving command mode) |
| R                    | Replaces a single character under the cursor (entering the replace mode)    |
| s                    | Delete a single character under the cursor and enter input mode             |
| S                    | Change current line (equivalent to 0c$)                                     |
| t<char>              | Move right to the next occurrence of `char`, then one char backward         |
| T<char>              | Move left to the previous occurrence of `char`, then one char forward       |
| u                    | Undo                                                                        |
| w                    | Move one word or token right                                                |
| W                    | Move one non-blank word right                                               |
| x                    | Delete a single character under the cursor                                  |
| X                    | Delete a character before the cursor                                        |
| y<movement>          | Yank a movement into buffer (copy)                                          |
| <<movement>          | Dedent                                                                      |
| ><movement>          | Indent                                                                      |

### vi insert mode

| Keystroke         | Action                                        |
|-------------------|-----------------------------------------------|
| Ctrl-H, Backspace | Delete character before cursor                |
| Shift-Tab         | Previous completion                           |
| Ctrl-I, Tab       | Next completion                               |
| Right             | Complete hint if cursor is at the end of line |
| Alt-<char>        | Fast command mode                             |
| Esc               | Switch to command mode                        |

[Readline vi Editing Mode Cheat Sheet](http://www.catonmat.net/download/bash-vi-editing-mode-cheat-sheet.pdf)

[ANSI escape code](https://en.wikipedia.org/wiki/ANSI_escape_code)

## Usage

The library is published to Maven Central.
Use the latest version shown by the badge above.

Kotlin DSL (example for a native target):

```kotlin
kotlin {
    // Choose your native targets, e.g.:
    macosX64()
    linuxX64()
    mingwX64()

    sourceSets {
        // With the default hierarchy, a shared nativeMain is available when you have multiple native targets
        val nativeMain by getting {
            dependencies {
                implementation("io.github.smyrgeorge:readline4k:<latest>")
            }
        }
    }
}
```

If you use only one native target, add the dependency to that target's Main source set (e.g., macosX64Main,
linuxX64Main, or mingwX64Main).

## Acknowledgements

Under the hood, `readline4k` leverages the [rustyline](https://github.com/kkawakam/rustyline) project to provide
comprehensive readline functionality, with communication between Kotlin and the Rust library handled through FFI
(Foreign Function Interface).

## License

MIT — see LICENSE.

