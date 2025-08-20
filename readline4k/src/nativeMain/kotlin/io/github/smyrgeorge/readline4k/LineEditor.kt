package io.github.smyrgeorge.readline4k

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class LineEditor(
    val linePrefix: String = "> "
) {
    private val rl = new_default_editor()

    fun readLine(): Result<String> =
        editor_read_line(rl, linePrefix).toStringResult()

    fun loadHistory(path: String): Result<Unit> {
        if (!SystemFileSystem.exists(Path(path))) return Result.success(Unit)
        return editor_load_history(rl, path).toUnitResult()
    }

    fun addHistoryEntry(entry: String): Unit =
        editor_add_history_entry(rl, entry)

    fun saveHistory(path: String): Result<Unit> {
        return editor_save_history(rl, path).toUnitResult()
    }

    private fun CPointer<ReadLineResult>?.toUnitResult(): Result<Unit> {
        return use { result ->
            if (result.isError()) Result.failure(result.toError())
            else Result.success(Unit)
        }
    }

    private fun CPointer<ReadLineResult>?.toStringResult(): Result<String> {
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
}