package io.github.smyrgeorge.readline4k

import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType

/**
 * Customizes the visual appearance of the editor by transforming raw text into a
 * styled string (for example, by adding ANSI escape codes for colors, bold, etc.).
 *
 * You can override any of the methods to influence how the current input line,
 * inline hints, the prompt, or completion candidates are rendered.
 *
 * Notes:
 * - All methods have safe pass-through defaults; you can override only what you need.
 * - If your terminal does not support colors, consider returning the input unmodified
 *   or using [LineEditorConfig.ColorMode] to control colorization globally.
 * - When adding ANSI sequences, ensure they are properly reset ("\u001B[0m") so that
 *   subsequent output is not affected.
 * - Keep performance in mind: these callbacks may be invoked often while the user types.
 *
 * Example:
 * ```kotlin
 * editor.withHighlighter(object : Highlighter {
 *     override fun highlightHint(hint: String): String = "\u001B[90m$hint\u001B[0m" // dim
 *     override fun highlightPrompt(prompt: String, isDefault: Boolean): String =
 *         if (isDefault) "\u001B[32m$prompt\u001B[0m" else "\u001B[33m$prompt\u001B[0m"
 *     override fun highlightCandidate(candidate: String, completionType: CompletionType): String =
 *         "\u001B[36m$candidate\u001B[0m"
 * })
 * ```
 *
 * @see io.github.smyrgeorge.readline4k.impl.SimpleHighlighter for a basic ANSI-based implementation.
 * @see AbstractLineEditor.installHighlighter to attach a highlighter to an editor instance.
 */
interface Highlighter {
    /**
     * Highlight or otherwise decorate the current input [line].
     *
     * The implementation can add styling (e.g., ANSI escape codes) or
     * leave the line unchanged. The [pos] argument is the current caret
     * position (0-based code-unit index into [line]), which you can use to
     * apply different styles before/after the cursor if desired.
     *
     * Considerations:
     * - Always return a valid string for display; if you inject ANSI sequences,
     *   make sure to reset styles ("\u001B[0m").
     * - Avoid expensive work for each keystroke.
     *
     * @param line the current editable buffer
     * @param pos  the caret position within [line]
     * @return the styled text to be rendered
     */
    fun highlight(line: String, pos: Int): String = line

    /**
     * Highlight the inline hint shown after the cursor.
     *
     * The hint is typically a gray/dim preview of what could be auto-completed.
     * Return a styled string (e.g., with ANSI escape codes) or the same [hint]
     * if no styling is desired.
     *
     * @param hint the hint text to display after the caret
     * @return styled hint text
     */
    fun highlightHint(hint: String): String = hint

    /**
     * Highlight the prompt text displayed before the user input.
     *
     * Parameters:
     * - prompt: the raw prompt text (e.g., "> ").
     * - isDefault: whether this is the default prompt supplied by the editor
     *   (as opposed to a custom or transient one). Some UIs may choose different
     *   styling when the prompt is in its default state.
     *
     * @param prompt the prompt text prefix
     * @param isDefault true if the editor-provided default prompt is active
     * @return styled prompt text
     */
    fun highlightPrompt(prompt: String, isDefault: Boolean): String = prompt

    /**
     * Highlight a completion candidate displayed to the user.
     *
     * Parameters:
     * - candidate: the candidate text.
     * - completionType: the active completion mode (e.g., CIRCULAR or LIST). This can be
     *   used to tune styling depending on how candidates are presented.
     *
     * @param candidate the candidate label
     * @param completionType the active completion mode
     * @return styled candidate text
     */
    fun highlightCandidate(candidate: String, completionType: CompletionType): String = candidate

    /**
     * Called by the underlying engine to decide whether a character-level highlight
     * requires a repaint after a command of the given [kind].
     *
     * Return true if your highlighter needs the line to be re-rendered (for example,
     * to update bracket matching, search highlights, or other transient effects)
     * for the current [line] at caret [pos]; return false to keep the default behavior.
     *
     * Implementations that do not need character-level handling can keep the default `false`.
     *
     * @param line the current editable buffer
     * @param pos  the caret position within [line]
     * @param kind the type of editor command that triggered this check
     * @return true to request a redraw/re-highlighting; false otherwise
     */
    fun highlightChar(line: String, pos: Int, kind: CmdKind): Boolean = false

    /**
     * Indicates which kind of command triggered a highlight evaluation.
     */
    enum class CmdKind {
        /** Cursor moved without modifying the buffer. */
        MOVE_CURSOR,
        /** Some other editing command occurred (insertion, deletion, etc.). */
        OTHER,
        /** The editor forces a full refresh/repaint. */
        FORCED_REFRESH
    }
}