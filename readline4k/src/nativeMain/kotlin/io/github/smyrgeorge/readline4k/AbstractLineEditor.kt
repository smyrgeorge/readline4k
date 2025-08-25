package io.github.smyrgeorge.readline4k

import io.github.smyrgeorge.readline4k.LineEditorError.Companion.couldNotInstantiateTheEditor
import io.github.smyrgeorge.readline4k.impl.*
import io.github.smyrgeorge.readline4k.tools.Completer
import io.github.smyrgeorge.readline4k.tools.Highlighter
import kotlinx.cinterop.*
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import readline4k.*

/**
 * Abstract base for interactive line editors backed by a native engine.
 *
 * This class wires the Kotlin API to the underlying native implementation and
 * exposes a small, composable API for reading input lines, managing history,
 * and plugging in completion and highlighting.
 *
 * Typical usage:
 * - Instantiate a concrete editor (e.g., SimpleLineEditor) with a prompt prefix and optional [config].
 * - Optionally attach a [Completer] via [withCompleter] and/or a [Highlighter] via [withHighlighter].
 * - Call [readLine] in a loop; handle the returned [Result].
 * - Optionally persist history with [loadHistory] and [saveHistory].
 *
 * Notes on behavior and threading:
 * - All operations interact with a native resource created at construction time. The resource is
 *   freed by the underlying runtime when the process exits; you do not need to close it manually.
 * - Instances are not intended to be used concurrently from multiple threads.
 * - Methods returning Result wrap native errors into [LineEditorError].
 *
 * @property linePrefix The prompt/prefix displayed before each input (e.g., "> ").
 * @property config The immutable configuration used to initialize the native editor.
 */
@Suppress("unused")
@OptIn(ExperimentalForeignApi::class)
abstract class AbstractLineEditor(
    val linePrefix: String,
    val config: LineEditorConfig,
) {
    private val holder: CallbacksHolder = CallbacksHolder()
    private val holderRef: StableRef<CallbacksHolder> = StableRef.create(holder)
    private val holderPointer: COpaquePointer = holderRef.asCPointer()

    private val rl: COpaquePointer = memScoped {
        val cfg: CValue<EditorConfig> = config.toCValue()
        new_editor_with_config(cfg.ptr) ?: couldNotInstantiateTheEditor()
    }

    /**
     * Read a single line from the user.
     *
     * The native editor renders [linePrefix], processes key bindings according to [config],
     * applies optional completion/highlighting if configured, and returns the accepted line.
     *
     * Returns:
     * - Result.success(String) with the user input when a line is accepted.
     * - Result.failure(LineEditorError) when the input is interrupted (e.g., Ctrl-C), EOF, or
     *   an unknown native error occurs. Inspect [LineEditorError.code].
     */
    fun readLine(): Result<String> = editor_read_line(rl, linePrefix).toStringResult()

    /**
     * Load history entries from the given file [path].
     *
     * If the file does not exist, this is a no-op and returns success. When it exists, entries
     * are appended/replaced according to the native backend policy and current [config].
     */
    fun loadHistory(path: String): Result<Unit> {
        val exists = SystemFileSystem.exists(Path(path))
        return if (!exists) Result.success(Unit)
        else editor_load_history(rl, path).toUnitResult()
    }

    /**
     * Add a single [entry] to the in-memory history buffer.
     *
     * Whether duplicates are kept depends on [LineEditorConfig.historyDuplicates].
     */
    fun addHistoryEntry(entry: String): Unit = editor_add_history_entry(rl, entry)

    /**
     * Save current history to the file at [path]. Creates or overwrites as needed.
     */
    fun saveHistory(path: String): Result<Unit> = editor_save_history(rl, path).toUnitResult()

    /**
     * Clear the in-memory history.
     */
    fun clearHistory(): Result<Unit> = editor_clear_history(rl).toUnitResult()

    /**
     * Install a [Completer] which will be consulted during completion (e.g., Tab).
     * Returns this editor instance for chaining.
     */
    fun withCompleter(completer: Completer): AbstractLineEditor {
        holder.completer = completer
        editor_set_completer(rl, staticCFunction(::completerCallback), holderPointer)
        return this
    }

    /**
     * Install a [Highlighter] to customize visual presentation of hints, prompts, and candidates.
     * Returns this editor instance for chaining.
     */
    fun withHighlighter(highlighter: Highlighter): AbstractLineEditor {
        holder.highlighter = highlighter
        editor_set_hint_highlighter(rl, staticCFunction(::hintHighlighterCallback), holderPointer)
        editor_set_prompt_highlighter(rl, staticCFunction(::promptHighlighterCallback), holderPointer)
        editor_set_candidate_highlighter(rl, staticCFunction(::candidateHighlighterCallback), holderPointer)
        return this
    }

    /**
     * Holds user-supplied strategy objects so they can be accessed from native callbacks.
     * Stored behind a StableRef and passed to native as an opaque pointer.
     */
    internal class CallbacksHolder(
        var completer: Completer? = null,
        var highlighter: Highlighter? = null,
    )

    /**
     * Convert the high-level [LineEditorConfig] to the FFI struct consumed by the native editor.
     */
    private fun LineEditorConfig.toCValue(): CValue<EditorConfig> =
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