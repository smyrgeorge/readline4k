package io.github.smyrgeorge.readline4k.examples

import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import io.github.smyrgeorge.readline4k.impl.SimpleFileCompleter
import io.github.smyrgeorge.readline4k.impl.SimpleHighlighter
import io.github.smyrgeorge.readline4k.impl.SimpleLineEditor

object Default {
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
                .setCompleter(SimpleFileCompleter()) // Provides file completion (optional).
                .setHighlighter(SimpleHighlighter()) // Provides color highlighting (optional).

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
                    // We can also add the line to the history automatically by setting autoAddHistory = true in the config.
                    editor.addHistoryEntry(line)
                    println(line)
                }
        }

        // Save the history to disk.
        editor.saveHistory(history)
    }
}