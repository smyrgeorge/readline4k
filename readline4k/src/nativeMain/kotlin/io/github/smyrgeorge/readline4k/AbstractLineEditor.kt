package io.github.smyrgeorge.readline4k

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import readline4k.EditorConfig

/**
 * Abstract base class for creating a line-based text editor with command history and configurable behavior.
 *
 * This class provides an interface to handle user input line by line, apply customizable configurations,
 * support command history management, and interact with a native backend for advanced input handling.
 * Subclasses are expected to provide specific implementations.
 */
@OptIn(ExperimentalForeignApi::class)
abstract class AbstractLineEditor {
    /**
     * Represents the prefix added to each line of input during editing.
     *
     * The `linePrefix` is used to visually distinguish the input line
     * when displaying it to the user, typically indicating the start
     * of input and providing contextual information or prompting behavior.
     *
     * This property is defined as an abstract member, allowing subclasses
     * to specify their own prefix string.
     */
    abstract val linePrefix: String

    /**
     * The configuration settings applied to the line editor.
     *
     * This configuration governs various aspects of the line editor's behavior,
     * such as history management, editing mode, tab completion, and visual settings.
     * The settings are encapsulated in a [LineEditorConfig] data class that allows
     * customization of features like history size, colorization, and input handling.
     */
    abstract val config: LineEditorConfig

    /**
     * Represents a low-level pointer to the native line editor instance.
     *
     * This pointer is used internally to interface with the underlying C-based implementation of the
     * line editor, enabling operations such as reading input, handling history, and applying configuration.
     *
     * It is initialized during the creation of the line editor and must remain valid for the lifetime
     * of the `AbstractLineEditor` or its implementing classes. Proper cleanup should ensure that any
     * associated resources are released when the editor is closed.
     */
    internal abstract val rl: COpaquePointer

    /**
     * Reads a single line of input from the user, applying the line editor's current configuration
     * and handling of the input. The method blocks until input is received or the operation is interrupted.
     *
     * @return A result containing the input string if successful, or an error encapsulated in a `LineEditorError`
     *         if the operation fails (e.g., end of input or interruption).
     */
    abstract fun readLine(): Result<String>

    /**
     * Loads the command history from a specified file path into the line editor.
     * If the file does not exist, the function completes silently without executing further operations.
     *
     * @param path The filesystem path to the history file to be loaded.
     * @return A [Result] indicating success or failure of the operation.
     *         The result contains a successful [Unit] value if the history is loaded successfully.
     *         If an error occurs, it encapsulates the error as a [LineEditorError].
     */
    abstract fun loadHistory(path: String): Result<Unit>

    /**
     * Adds a new entry to the line editor's command history.
     *
     * @param entry The command string to be appended to the history.
     */
    abstract fun addHistoryEntry(entry: String)

    /**
     * Saves the command history to a specified file path. This allows the persisted history
     * to be stored on the file system for reuse in future sessions.
     *
     * @param path The filesystem path to save the history into. If the file exists, it will
     *             be overwritten. If the file does not exist, it will be created.
     * @return A [Result] indicating the success or failure of the operation. The result contains
     *         a successful [Unit] value if the history is saved successfully, or an error
     *         encapsulated in a [LineEditorError] if the operation fails.
     */
    abstract fun saveHistory(path: String): Result<Unit>

    /**
     * Clears the command history stored within the line editor.
     *
     * This method removes all previously saved history entries, effectively resetting the history
     * to an empty state.
     *
     * @return A [Result] indicating the success or failure of the operation. The result contains
     *         a successful [Unit] value if the history is cleared successfully, or an error
     *         encapsulated in a [LineEditorError] if the operation fails.
     */
    abstract fun clearHistory(): Result<Unit>

    @OptIn(ExperimentalForeignApi::class)
    internal fun LineEditorConfig.toCValue(): CValue<EditorConfig> =
        cValue<EditorConfig> {
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
}