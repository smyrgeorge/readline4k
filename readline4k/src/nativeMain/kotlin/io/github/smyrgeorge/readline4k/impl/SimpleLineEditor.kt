package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.LineEditorError.Companion.couldNotInstantiateTheEditor
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class SimpleLineEditor(
    override val linePrefix: String = "> ",
    override val config: LineEditorConfig = LineEditorConfig(),
) : AbstractLineEditor() {
    override val rl: COpaquePointer = memScoped {
        val cfg: CValue<EditorConfig> = config.toCValue()
        new_editor_with_config(cfg.ptr) ?: couldNotInstantiateTheEditor()
    }

    override fun readLine(): Result<String> = editor_read_line(rl, linePrefix).toStringResult()
    override fun loadHistory(path: String): Result<Unit> {
        val exists = SystemFileSystem.exists(Path(path))
        return if (!exists) Result.success(Unit)
        else editor_load_history(rl, path).toUnitResult()
    }

    override fun addHistoryEntry(entry: String) = editor_add_history_entry(rl, entry)
    override fun saveHistory(path: String): Result<Unit> = editor_save_history(rl, path).toUnitResult()
    override fun clearHistory(): Result<Unit> = editor_clear_history(rl).toUnitResult()
}