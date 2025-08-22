import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.impl.FileCompleterLineEditor

fun main() {
    val history = "history.txt" // Filesystem path to the history file.

    // Configure the LineEditor.
    val config = LineEditorConfig(
        maxHistorySize = 100,
        // See the documentation for more options.
    )

    // Create a new LineEditor instance.
    val editor = FileCompleterLineEditor(linePrefix = "> ", config).also { le ->
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
