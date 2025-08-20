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

## Supported Platforms

- Unix (tested on FreeBSD, Linux and macOS)
- Windows
    - cmd.exe
    - Powershell

### Note that:

- Powershell ISE is not supported
- Mintty (Cygwin/MinGW) is not supported
- Highlighting / Colors are not supported on Windows < Windows 10 except with ConEmu and ColorMode::Forced.

## Quick start

A minimal REPL-style loop with history persistence:

```kotlin
fun main() {
    val history = "history.txt" // Filesystem path to the history file.

    // Configure the LineEditor.
    val config = LineEditorConfig(
        maxHistorySize = 100,
    )

    // Create a new LineEditor instance.
    val editor = LineEditor(linePrefix = "> ", config).also { le ->
        // Load the history from disk (throws LineEditorError if it fails).
        le.loadHistory(history).getOrThrow()
    }

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

## Notes

Under the hood, readline4k leverages the [rustyline](https://github.com/kkawakam/rustyline) project to provide
comprehensive readline functionality, with communication between Kotlin and the Rust library handled through FFI (
Foreign Function Interface).

## License

MIT — see LICENSE.

