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

## Features

- Simple API: LineEditor with sensible defaults
- Optional prompt prefix (default: "> ")
- Persistent history: load, append entries, and save
- Result-based API with typed errors (Eof, Interrupted, Unknown)
- Kotlin 2.2 compatible; ships as a native library for desktop targets

## Quick start

A minimal REPL-style loop with history persistence (mirrors the example project):

```kotlin
fun main() {
    val history = "history.txt"
    val editor = LineEditor().also {
        it.loadHistory(history)
    }
    while (true) {
        print("> ")
        val line = editor.readLine().getOrElse { err ->
            // err is a LineEditor.LineEditorError
            println(err.message)
            break
        }
        editor.addHistoryEntry(line)
        println(line)
    }
    editor.saveHistory(history)
}
```

Notes:

- You can customize the editor's prompt prefix via LineEditor(linePrefix = "my-app> ").
- The example prints a prompt explicitly. Choose either to rely on the editor's prefix or print your own to avoid
  duplicate prompts.
- loadHistory() safely no-ops if the file does not exist.

## API overview

Public API (see LineEditor.kt):

- constructor(linePrefix: String = "> ")
- readLine(): Result<String> — Read a line; on failure, the Result contains LineEditorError
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

