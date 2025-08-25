@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import io.github.smyrgeorge.readline4k.LineEditorError
import kotlinx.cinterop.*
import platform.posix.strdup
import readline4k.ReadLineResult
import readline4k.free_read_line_result

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

internal fun completerCallback(
    holderPointer: COpaquePointer?,
    line: CPointer<ByteVar>?,
    pos: Int,
    outStart: CPointer<IntVar>?,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (line == null || outStart == null) return null
    val holder = holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
    val completer = holder.completer ?: return null
    val (start, items) = completer.complete(line.toKString(), pos)
    outStart.pointed.value = start
    val joined = items.joinToString("_*#*_")
    // return malloc-allocated string for Rust to free via free()
    return strdup(joined)?.reinterpret()
}

@Suppress("DuplicatedCode")
internal fun hintHighlighterCallback(
    holderPointer: COpaquePointer?,
    hint: CPointer<ByteVar>?,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (hint == null) return null
    val holder = holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightHint(hint.toKString())
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}

@Suppress("DuplicatedCode")
internal fun promptHighlighterCallback(
    holderPointer: COpaquePointer?,
    prompt: CPointer<ByteVar>?,
    isDefault: Boolean,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (prompt == null) return null
    val holder = holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightPrompt(prompt.toKString(), isDefault)
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}

@Suppress("DuplicatedCode")
internal fun candidateHighlighterCallback(
    holderPointer: COpaquePointer?,
    candidate: CPointer<ByteVar>?,
    completion: Int,
): CPointer<ByteVar>? {
    require(holderPointer != null) { "The holderPointer must not be null!" }
    if (candidate == null) return null
    val holder = holderPointer.asStableRef<AbstractLineEditor.CallbacksHolder>().get()
    val highlighter = holder.highlighter ?: return null
    val highlighted = highlighter.highlightCandidate(candidate.toKString(), CompletionType.entries[completion])
    // return malloc-allocated string for Rust to free via free()
    return strdup(highlighted)?.reinterpret()
}