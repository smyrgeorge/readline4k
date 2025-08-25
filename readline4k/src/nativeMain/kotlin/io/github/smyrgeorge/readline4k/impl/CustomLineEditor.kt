@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.LineEditorError.Companion.couldNotInstantiateTheEditor
import io.github.smyrgeorge.readline4k.tools.Completer
import io.github.smyrgeorge.readline4k.tools.Highlighter
import kotlinx.cinterop.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class CustomLineEditor(
    override val linePrefix: String = "> ",
    override val config: LineEditorConfig = LineEditorConfig(),
) : AbstractLineEditor() {
    override val rl: COpaquePointer = memScoped {
        val cfg: CValue<EditorConfig> = config.toCValue()
        new_custom_editor_with_config(cfg.ptr) ?: couldNotInstantiateTheEditor()
    }

    private val holder: CallbacksHolder = CallbacksHolder()
    private val holderRef: StableRef<CallbacksHolder> = StableRef.create(holder)
    private val holderPointer: COpaquePointer = holderRef.asCPointer()

    override fun readLine(): Result<String> = custom_editor_read_line(rl, linePrefix).toStringResult()
    override fun loadHistory(path: String): Result<Unit> {
        val exists = SystemFileSystem.exists(Path(path))
        return if (!exists) Result.success(Unit)
        else custom_editor_load_history(rl, path).toUnitResult()
    }

    override fun addHistoryEntry(entry: String) = custom_editor_add_history_entry(rl, entry)
    override fun saveHistory(path: String): Result<Unit> = custom_editor_save_history(rl, path).toUnitResult()
    override fun clearHistory(): Result<Unit> = custom_editor_clear_history(rl).toUnitResult()

    fun withCompleter(completer: Completer): CustomLineEditor {
        holder.completer = completer
        custom_editor_set_completer(rl, staticCFunction(::completerCallback), holderPointer)
        return this
    }

    fun withHighlighter(highlighter: Highlighter): CustomLineEditor {
        holder.highlighter = highlighter
        custom_editor_set_hint_highlighter(rl, staticCFunction(::hintHighlighterCallback), holderPointer)
        custom_editor_set_prompt_highlighter(rl, staticCFunction(::promptHighlighterCallback), holderPointer)
        custom_editor_set_candidate_highlighter(rl, staticCFunction(::candidateHighlighterCallback), holderPointer)
        return this
    }

    internal class CallbacksHolder(
        var completer: Completer? = null,
        var highlighter: Highlighter? = null,
    )
}
