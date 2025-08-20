import io.github.smyrgeorge.readline4k.LineEditor

fun main() {
    val history = "history.txt" // Filesystem path to the history file.

    // Create a new LineEditor instance.
    val editor = LineEditor(linePrefix = "> ").also { le ->
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
