import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.impl.CustomLineEditor

fun main() {
    val history = "history.txt" // Filesystem path to the history file.

    // Configure the LineEditor.
    val config = LineEditorConfig(
        maxHistorySize = 100,
        completionType = LineEditorConfig.CompletionType.LIST,
        // See the documentation for more options.
    )

    // Create a new LineEditor instance.
    val editor = CustomLineEditor(
        linePrefix = "> ",
        config = config,
        completer = { line, pos ->
            0 to listOf("test-1", "test-2", "test-3", "test-4")
        },
        hintHighlighter = { hint -> "\u001B[90m$hint\u001B[0m" },
        candidateHighlighter = { candidate, _ -> "\u001B[36m$candidate\u001B[0m" },
        promptHighlighter = { prompt, _ -> "\u001B[32m$prompt\u001B[0m" },
    ).also { le ->
        // Load the history from the disk (throws LineEditorError if it fails).
        le.loadHistory(history).getOrThrow()
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
