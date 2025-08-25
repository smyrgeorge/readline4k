package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import io.github.smyrgeorge.readline4k.Highlighter

/**
 * A simple ANSI-based [Highlighter] implementation.
 *
 * This highlighter colors:
 * - hints in dim gray (ANSI 90)
 * - prompts in green (ANSI 32)
 * - completion candidates in cyan (ANSI 36)
 *
 * The coloring is applied by surrounding the text with the corresponding escape
 * code and a reset ("\u001B[0m"). It assumes the running terminal supports ANSI
 * escape sequences.
 */
class SimpleHighlighter : Highlighter {
    /**
     * Highlights a trailing hint or suggestion shown after the caret.
     * @param hint the hint text
     * @return the hint wrapped with ANSI dim gray color.
     */
    override fun highlightHint(hint: String): String =
        "\u001B[90m$hint\u001B[0m"

    /**
     * Highlights the input prompt.
     * @param prompt the prompt text
     * @param isDefault whether the prompt is the default provided by the editor
     * @return the prompt wrapped with ANSI green color.
     */
    override fun highlightPrompt(prompt: String, isDefault: Boolean): String =
        "\u001B[32m$prompt\u001B[0m"

    /**
     * Highlights a completion candidate option displayed to the user.
     * @param candidate the candidate label
     * @param completionType the active completion mode (not used by this simple implementation)
     * @return the candidate wrapped with ANSI cyan color.
     */
    override fun highlightCandidate(candidate: String, completionType: CompletionType): String =
        "\u001B[36m$candidate\u001B[0m"
}