package io.github.smyrgeorge.readline4k

/**
 * Represents a mechanism to validate user input in an editable context.
 * The validation process determines whether the input is valid, incomplete, or invalid,
 * enabling the editor or related components to provide feedback to the user during typing.
 */
interface Validator {
    /**
     * Validates the given input line and position to determine its state.
     *
     * @param line the current input buffer to validate
     * @param pos the caret position within the input line
     * @return a Validation result indicating whether the input is valid, incomplete, or invalid
     */
    fun validate(line: String, pos: Int): Validation

    /**
     * Determines if the validate method should be invoked during typing.
     *
     * @return true if validation should occur while typing, false otherwise
     */
    fun validateWhileTyping(): Boolean = false

    /**
     * Represents the result of a validation operation performed on user input.
     *
     * This sealed class defines the three possible states of validation:
     * - `Valid`: Indicates that the input is valid. Optionally, a message can accompany the validation.
     * - `Invalid`: Indicates that the input is invalid. Optionally, a message can describe the reason.
     * - `Incomplete`: Indicates the input is not yet complete for validation but not necessarily invalid.
     */
    sealed class Validation {
        data class Valid(val message: String? = null) : Validation()
        data class Invalid(val message: String? = null) : Validation()
        data object Incomplete : Validation()
    }
}
