package io.github.smyrgeorge.readline4k

/**
 * Represents an error that occurs during line editing operations in the `LineEditor` class.
 *
 * This exception is thrown when specific issues, often originating from the underlying driver,
 * occur during the execution of line editing features such as reading input, managing history,
 * or other related functionality.
 *
 * @property code The specific error code indicating the type of error.
 * @constructor Creates a new `LineEditorError` with the specified error code and an optional message.
 */
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