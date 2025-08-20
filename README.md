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

Readline4k is a minimal Kotlin/Native line editor for interactive console apps. It provides readline-style input with
history support across Linux, macOS, and Windows.

## Features

- Simple API: LineEditor with sensible defaults
- Optional prompt prefix (default: "> ")
- Persistent history: load, append entries, and save
- Returns null on EOF (e.g., Ctrl-D/Ctrl-Z) so loops can exit cleanly
- Kotlin 2.2 compatible; ships as a native library for desktop targets

## Quick start

A minimal REPL-style loop with history persistence:

```kotlin
fun main() {
    val historyFile = "history.txt"
    val editor = LineEditor() // default prompt is "> "

    // Load previous session history if available
    editor.loadHistory(historyFile)

    while (true) {
        // readLine() will render the prompt and handle editing/history keys
        val line = editor.readLine() ?: break // EOF -> exit loop
        editor.addHistoryEntry(line)
        println(line)
    }

    // Persist this session's history
    editor.saveHistory(historyFile)
}
```

Notes:

- You can customize the prompt: LineEditor(linePrefix = "my-app> ")
- readLine() returns null on EOF (Ctrl-D on Unix, Ctrl-Z on Windows), allowing you to break the loop.
- loadHistory() safely no-ops if the file does not exist.

## API overview

Public API (see LineEditor.kt):

- constructor(linePrefix: String = "> ")
- readLine(): String? — Reads an edited line or null on EOF
- loadHistory(path: String) — Loads history from file if it exists
- addHistoryEntry(entry: String) — Appends an entry to the in-memory history
- saveHistory(path: String) — Saves history to file

## Installation

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
        // With default hierarchy, a shared nativeMain is available when you have multiple native targets
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

## Example project

A runnable example lives under examples/src/nativeMain/kotlin/Main.kt and demonstrates loading/saving history and
echoing input lines.

## License

MIT — see LICENSE.

