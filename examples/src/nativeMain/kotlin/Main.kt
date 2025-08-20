import io.github.smyrgeorge.readline4k.LineEditor

fun main() {
    println("Hello, from Kotlin/Native!")
    val history = "history.txt"
    val editor = LineEditor().also {
        it.loadHistory(history)
    }
    while (true) {
        print("> ")
        val line = editor.readLine().getOrElse {
            // it as LineEditorError
            println(it.message)
            break
        }
        editor.addHistoryEntry(line)
        println(line)
    }
    editor.saveHistory(history)
    println("Bye, from Kotlin/Native!")
}
