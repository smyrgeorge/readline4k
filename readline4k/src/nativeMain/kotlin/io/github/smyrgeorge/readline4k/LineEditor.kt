package io.github.smyrgeorge.readline4k

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class LineEditor(
    val linePrefix: String = "> "
) {
    private val rl = new_default_editor()

    fun readLine(): String? = editor_read_line(rl, linePrefix)?.toKString()
    fun loadHistory(path: String) {
        if (!SystemFileSystem.exists(Path(path))) return
        editor_load_history(rl, path)
    }

    fun addHistoryEntry(entry: String): Unit = editor_add_history_entry(rl, entry)
    fun saveHistory(path: String): Unit = editor_save_history(rl, path)
}