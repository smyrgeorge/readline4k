@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.LineEditorError.Companion.couldNotInstantiateTheEditor
import kotlinx.cinterop.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.strdup
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class CustomLineEditor(
    override val linePrefix: String = "> ",
    override val config: LineEditorConfig = LineEditorConfig(),
    completer: ((line: String, pos: Int) -> Pair<Int, List<String>>)? = null,
    highlighter: ((hint: String) -> String)? = null,
) : AbstractLineEditor() {
    override val rl: COpaquePointer = memScoped {
        val cfg: CValue<EditorConfig> = config.toCValue()
        new_custom_editor_with_config(cfg.ptr) ?: couldNotInstantiateTheEditor()
    }

    private val holder: CallbacksHolder = CallbacksHolder(completer, highlighter)
    private val holderRef: StableRef<CallbacksHolder> = StableRef.create(holder)
    private val holderPointer: COpaquePointer = holderRef.asCPointer()

    init {
        completer?.let {
            custom_editor_set_completer(rl, staticCFunction(::completerCallback), holderPointer)
        }
        highlighter?.let {
            custom_editor_set_highlighter(rl, staticCFunction(::highlighterCallback), holderPointer)
        }
    }

    override fun readLine(): Result<String> = custom_editor_read_line(rl, linePrefix).toStringResult()
    override fun loadHistory(path: String): Result<Unit> {
        val exists = SystemFileSystem.exists(Path(path))
        return if (!exists) Result.success(Unit)
        else custom_editor_load_history(rl, path).toUnitResult()
    }

    override fun addHistoryEntry(entry: String) = custom_editor_add_history_entry(rl, entry)
    override fun saveHistory(path: String): Result<Unit> = custom_editor_save_history(rl, path).toUnitResult()
    override fun clearHistory(): Result<Unit> = custom_editor_clear_history(rl).toUnitResult()

    internal class CallbacksHolder(
        val completer: ((line: String, pos: Int) -> Pair<Int, List<String>>)?,
        val highlighter: ((hint: String) -> String)?,
    )
}

private fun completerCallback(
    holderPointer: COpaquePointer?,
    line: CPointer<ByteVar>?,
    pos: Int,
    outStart: CPointer<IntVar>?,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (line == null || outStart == null) return null

    val holder = holderPointer.asStableRef<CustomLineEditor.CallbacksHolder>().get()
    val completer = holder.completer ?: return null

    val (start, items) = completer(line.toKString(), pos)
    outStart.pointed.value = start
    val joined = items.joinToString("_#_*_")
    // return malloc-allocated string for Rust to free via free()
    return strdup(joined)?.reinterpret()
}

private fun highlighterCallback(
    holderPointer: COpaquePointer?,
    hint: CPointer<ByteVar>?,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (hint == null) return null

    val holder = holderPointer.asStableRef<CustomLineEditor.CallbacksHolder>().get()
    val highlighter = holder.highlighter ?: return null

    val highlighted = highlighter(hint.toKString())
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}
