package io.github.smyrgeorge.readline4k.tools

import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType

/**
 * Allows customizing the visual appearance of various editor elements by
 * transforming the raw text into a styled string (e.g., by adding ANSI
 * escape codes for colors, bold, etc.).
 *
 * All methods have sensible pass-through defaults, so you can override only
 * what you need. If your terminal does not support colors, consider returning
 * the input unmodified or using [io.github.smyrgeorge.readline4k.LineEditorConfig.ColorMode]
 * to control colorization globally.
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
 */
interface Highlighter {
    /**
     * Highlight the inline hint shown after the cursor.
     *
     * The hint is typically a gray/dim preview of what could be auto-completed.
     * Return a styled string (e.g., with ANSI escape codes) or the same [hint]
     * if no styling is desired.
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
     */
    fun highlightPrompt(prompt: String, isDefault: Boolean): String = prompt

    /**
     * Highlight a completion candidate displayed to the user.
     *
     * Parameters:
     * - candidate: the candidate text.
     * - completionType: the active completion mode (e.g., CIRCULAR or LIST). This can be
     *   used to tune styling depending on how candidates are presented.
     */
    fun highlightCandidate(candidate: String, completionType: CompletionType): String = candidate
}