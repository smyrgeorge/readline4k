import io.github.smyrgeorge.readline4k.LineEditor

fun main() {
    val history = "history.txt"
    val editor = LineEditor(linePrefix = "> ").also {
        it.loadHistory(history)
    }
    while (true) {
        val line = editor.readLine().getOrElse { err ->
            // err is a LineEditorError
            println(err.message)
            break
        }
        editor.addHistoryEntry(line)
        println(line)
    }
    editor.saveHistory(history)
}
