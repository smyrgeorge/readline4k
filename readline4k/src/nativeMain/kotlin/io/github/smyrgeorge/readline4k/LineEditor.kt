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
        editor_read_line(rl, linePrefix).toResult()

    fun loadHistory(path: String) {
        if (!SystemFileSystem.exists(Path(path))) return
        editor_load_history(rl, path)
    }

    fun addHistoryEntry(entry: String): Unit =
        editor_add_history_entry(rl, entry)

    fun saveHistory(path: String): Unit =
        editor_save_history(rl, path)

    @Suppress("CanBeParameter")
    class LineEditorError(
        val code: Code,
        message: String? = null,
    ) : RuntimeException("[$code] :: $message") {
        enum class Code {
            // IMPORTANT: Do not change the order the errors.
            // Error from the underlying driver:
            Eof,
            Interrupted,
            Unknown
        }
    }

    private fun CPointer<ReadLineResult>?.toResult(): Result<String> {
        fun ReadLineResult.isError(): Boolean = error >= 0
        fun ReadLineResult.toError(): LineEditorError {
            val code = LineEditorError.Code.entries[error]
            val message = error_message?.toKString()
            return LineEditorError(code, message)
        }

        inline fun <T> CPointer<ReadLineResult>?.use(block: (ReadLineResult) -> T): T {
            try {
                return this?.pointed?.let(block)
                    ?: error("Invalid ReadLineResult pointer: cannot dereference null pointer")
            } finally {
                free_read_line_result(this)
            }
        }

        return use { result ->
            if (result.isError()) Result.failure(result.toError())
            else Result.success(result.result!!.toKString())
        }
    }
}