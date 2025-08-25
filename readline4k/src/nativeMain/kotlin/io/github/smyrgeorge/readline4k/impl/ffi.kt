@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig
import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import io.github.smyrgeorge.readline4k.LineEditorError
import kotlinx.cinterop.*
import platform.posix.strdup
import readline4k.EditorConfig
import readline4k.ReadLineResult
import readline4k.free_read_line_result

internal fun LineEditorConfig.toCValue(): CValue<EditorConfig> = cValue<EditorConfig> {
    max_history_size = this@toCValue.maxHistorySize
    history_duplicates = this@toCValue.historyDuplicates.ordinal
    history_ignore_space = this@toCValue.historyIgnoreSpace
    completion_type = this@toCValue.completionType.ordinal
    completion_show_all_if_ambiguous = this@toCValue.completionShowAllIfAmbiguous
    completion_prompt_limit = this@toCValue.completionPromptLimit
    key_seq_timeout = this@toCValue.keySeqTimeout ?: -1
    edit_mode = this@toCValue.editMode.ordinal
    auto_add_history = this@toCValue.autoAddHistory
    bell_style = this@toCValue.bellStyle.ordinal
    color_mode = this@toCValue.colorMode.ordinal
    behavior = this@toCValue.behavior.ordinal
    tab_stop = this@toCValue.tabStop.toUByte()
    indent_size = this@toCValue.indentSize.toUByte()
    check_cursor_position = this@toCValue.checkCursorPosition
    enable_bracketed_paste = this@toCValue.enableBracketedPaste
    enable_synchronized_output = this@toCValue.enableSynchronizedOutput
    enable_signals = this@toCValue.enableSignals
}

internal fun CPointer<ReadLineResult>?.toUnitResult(): Result<Unit> {
    return use { result ->
        if (result.isError()) Result.failure(result.toError())
        else Result.success(Unit)
    }
}

internal fun CPointer<ReadLineResult>?.toStringResult(): Result<String> {
    return use { result ->
        if (result.isError()) Result.failure(result.toError())
        else Result.success(result.result!!.toKString())
    }
}

private fun ReadLineResult.isError(): Boolean = error >= 0
private fun ReadLineResult.toError(): LineEditorError {
    val code = LineEditorError.Code.entries[error]
    val message = error_message?.toKString()
    return LineEditorError(code, message)
}

private inline fun <T> CPointer<ReadLineResult>?.use(block: (ReadLineResult) -> T): T {
    try {
        return this?.pointed?.let(block)
            ?: error("Invalid ReadLineResult pointer: cannot dereference null pointer")
    } finally {
        free_read_line_result(this)
    }
}

private fun getHolder(holderPointer: COpaquePointer?): AbstractLineEditor.CallbacksHolder {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    return holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
}

internal fun completerCallback(
    holderPointer: COpaquePointer?,
    line: CPointer<ByteVar>?,
    pos: Int,
    outStart: CPointer<IntVar>?,
): CPointer<ByteVar>? {
    if (line == null || outStart == null) return null
    val holder = getHolder(holderPointer)
    val completer = holder.completer ?: return null
    val (start, items) = completer.complete(line.toKString(), pos)
    outStart.pointed.value = start
    val joined = items.joinToString("_*#*_")
    // return malloc-allocated string for Rust to free via free()
    return strdup(joined)?.reinterpret()
}

internal fun hintHighlighterCallback(
    holderPointer: COpaquePointer?,
    hint: CPointer<ByteVar>?,
): CPointer<ByteVar>? {
    if (hint == null) return null
    val holder = getHolder(holderPointer)
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightHint(hint.toKString())
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}

internal fun promptHighlighterCallback(
    holderPointer: COpaquePointer?,
    prompt: CPointer<ByteVar>?,
    isDefault: Boolean,
): CPointer<ByteVar>? {
    if (prompt == null) return null
    val holder = getHolder(holderPointer)
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightPrompt(prompt.toKString(), isDefault)
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}

internal fun candidateHighlighterCallback(
    holderPointer: COpaquePointer?,
    candidate: CPointer<ByteVar>?,
    completion: Int,
): CPointer<ByteVar>? {
    if (candidate == null) return null
    require(holderPointer != null) { "The holderPointer must not be null!" }
    val holder = holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightCandidate(candidate.toKString(), CompletionType.entries[completion])
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}
