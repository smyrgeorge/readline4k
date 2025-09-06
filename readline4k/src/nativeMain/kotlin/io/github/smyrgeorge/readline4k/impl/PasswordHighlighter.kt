package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.Highlighter

/**
 * A simple [Highlighter] that masks all input characters as asterisks.
 *
 * This highlighter is intended for password-like input. It replaces the visual
 * representation of the current line with a sequence of '*' of the same length,
 * keeping the actual input intact in the editor's buffer.
 *
 * Behavior notes:
 * - The [highlight] method always returns a masked string regardless of the cursor position.
 * - The [highlightChar] method returns false for [Highlighter.CmdKind.MOVE_CURSOR] so that
 *   pure cursor movement does not trigger unnecessary re-rendering of characters; other
 *   command kinds return true to ensure the masked view stays up to date.
 */
class PasswordHighlighter : Highlighter {
    /**
     * Returns a masked string ("*****") with the same length as [line]. The [pos]
     * parameter is ignored because masking applies to the whole line.
     */
    override fun highlight(line: String, pos: Int): String =
        "*".repeat(line.length)

    /**
     * Controls whether a character re-render is necessary for a given command [kind].
     *
     * - Returns false for [Highlighter.CmdKind.MOVE_CURSOR] to avoid redundant redraws
     *   when only the cursor moves.
     * - Returns true for all other kinds to keep the masked output current.
     */
    override fun highlightChar(line: String, pos: Int, kind: Highlighter.CmdKind): Boolean {
        return when (kind) {
            Highlighter.CmdKind.MOVE_CURSOR -> false
            else -> true
        }
    }
}