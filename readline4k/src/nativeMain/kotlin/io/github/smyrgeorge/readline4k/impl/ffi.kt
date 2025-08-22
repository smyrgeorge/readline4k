@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.LineEditorError
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
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