package io.github.smyrgeorge.readline4k

import io.github.smyrgeorge.readline4k.LineEditorError.Companion.couldNotInstantiateTheEditor
import io.github.smyrgeorge.readline4k.impl.*
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
        new_editor_with_config(cfg.ptr, holderPointer) ?: couldNotInstantiateTheEditor()
    }

    /**
     * Reads a single line of input from the user, optionally displaying a [prefix] at the start of the line.
     *
     * The method captures input using a line editor and returns it as a [Result] object, containing
     * either the successfully read line as a string or an error if the operation fails.
     *
     * @param prefix The optional string to display as a prompt at the start of the line. Defaults to the editor's [linePrefix].
     * @return A [Result] containing the read line as a [String] on success, or an error description on failure.
     */
    fun readLine(prefix: String = linePrefix): Result<String> = editor_read_line(rl, prefix).toStringResult()

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
     * Clears the terminal screen using the native line editor's rendering capabilities.
     */
    fun clearScreen(): Result<Unit> = editor_clear_screen(rl).toUnitResult()

    /**
     * Show or hide the terminal cursor while the editor is active.
     *
     * @param visible true to show the cursor; false to hide it.
     */
    fun setCursorVisibility(visible: Boolean) = editor_set_cursor_visibility(rl, visible)

    /**
     * Enable or disable automatic addition of accepted lines to the history buffer.
     *
     * When enabled, every successful [readLine] call will append the produced line to history,
     * subject to the history-related options in [config] (e.g., size, duplicate policy).
     *
     * @param value true to enable auto-add; false to require manual calls to [addHistoryEntry].
     */
    fun setAutoAddHistory(value: Boolean) = editor_set_auto_add_history(rl, value)

    /**
     * Set the color rendering mode used by the editor for prompts, hints, and highlights.
     *
     * This controls how ANSI color sequences are produced or suppressed based on the selected
     * [LineEditorConfig.ColorMode]. Typically you would choose AUTO to respect terminal support,
     * FORCE to always emit colors, or NONE to disable colors entirely.
     *
     * @param value The desired color mode.
     */
    fun setColorMode(value: LineEditorConfig.ColorMode) = editor_set_color_mode(rl, value.ordinal)

    /**
     * Install a [Completer] which will be consulted during completion (e.g., Tab).
     * Returns this editor instance for chaining.
     */
    fun withCompleter(completer: Completer): AbstractLineEditor {
        holder.completer = completer
        editor_set_completer(rl, staticCFunction(::completerCallback))
        return this
    }

    /**
     * Install a [Highlighter] to customize visual presentation of hints, prompts, and candidates.
     * Returns this editor instance for chaining.
     */
    fun withHighlighter(highlighter: Highlighter): AbstractLineEditor {
        holder.highlighter = highlighter
        editor_set_highlighter(rl, staticCFunction(::highlighterCallback))
        editor_set_hint_highlighter(rl, staticCFunction(::hintHighlighterCallback))
        editor_set_prompt_highlighter(rl, staticCFunction(::promptHighlighterCallback))
        editor_set_candidate_highlighter(rl, staticCFunction(::candidateHighlighterCallback))
        editor_set_char_highlighter(rl, staticCFunction(::charHighlighterCallback))
        return this
    }

    /**
     * Holds user-supplied strategy objects so they can be accessed from native callbacks.
     * Stored behind a StableRef and passed to native as an opaque pointer.
     */
    internal class CallbacksHolder(
        var completer: Completer? = null,
        var highlighter: Highlighter? = null
    )
}