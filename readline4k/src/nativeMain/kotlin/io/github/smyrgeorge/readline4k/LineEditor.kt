package io.github.smyrgeorge.readline4k

import io.github.smyrgeorge.readline4k.impl.toStringResult
import io.github.smyrgeorge.readline4k.impl.toUnitResult
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

@OptIn(ExperimentalForeignApi::class)
class LineEditor(
    val linePrefix: String = "> ",
    val config: LineEditorConfig = LineEditorConfig(),
) {
    private val rl: COpaquePointer? = memScoped {
        val cfg = cValue<EditorConfig> {
            max_history_size = config.maxHistorySize
            history_duplicates = config.historyDuplicates.ordinal
            history_ignore_space = config.historyIgnoreSpace
            completion_type = config.completionType.ordinal
            completion_show_all_if_ambiguous = config.completionShowAllIfAmbiguous
            completion_prompt_limit = config.completionPromptLimit
            key_seq_timeout = config.keySeqTimeout ?: -1
            edit_mode = config.editMode.ordinal
            auto_add_history = config.autoAddHistory
            bell_style = config.bellStyle.ordinal
            color_mode = config.colorMode.ordinal
            behavior = config.behavior.ordinal
            tab_stop = config.tabStop.toUByte()
            indent_size = config.indentSize.toUByte()
            check_cursor_position = config.checkCursorPosition
            enable_bracketed_paste = config.enableBracketedPaste
            enable_synchronized_output = config.enableSynchronizedOutput
            enable_signals = config.enableSignals
        }
        new_editor_with_config(cfg.ptr)
    }

    /**
     * Reads a single line of input from the user, applying the line editor's current configuration
     * and handling of the input. The method blocks until input is received or the operation is interrupted.
     *
     * @return A result containing the input string if successful, or an error encapsulated in a `LineEditorError`
     *         if the operation fails (e.g., end of input or interruption).
     */
    fun readLine(): Result<String> =
        editor_read_line(rl, linePrefix).toStringResult()

    /**
     * Loads the command history from a specified file path into the line editor.
     * If the file does not exist, the function completes silently without executing further operations.
     *
     * @param path The filesystem path to the history file to be loaded.
     * @return A [Result] indicating success or failure of the operation.
     *         The result contains a successful [Unit] value if the history is loaded successfully.
     *         If an error occurs, it encapsulates the error as a [LineEditorError].
     */
    fun loadHistory(path: String): Result<Unit> {
        if (!SystemFileSystem.exists(Path(path))) return Result.success(Unit)
        return editor_load_history(rl, path).toUnitResult()
    }

    fun addHistoryEntry(entry: String) {
        editor_add_history_entry(rl, entry)
    }

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
    fun saveHistory(path: String): Result<Unit> {
        return editor_save_history(rl, path).toUnitResult()
    }
}