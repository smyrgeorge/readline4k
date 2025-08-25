package io.github.smyrgeorge.readline4k.tools

/**
 * Provides completion candidates for the current input line.
 *
 * The completer is consulted when the user triggers completion (e.g. presses Tab)
 * while editing input in a LineEditor implementation. Your implementation
 * should analyze the current line and caret position, determine the start index
 * of the token to replace, and return a list of possible completions for that token.
 *
 * Notes:
 * - The returned candidates are plain strings; any coloring/highlighting of candidates
 *   is handled by [io.github.smyrgeorge.readline4k.tools.Highlighter].
 * - If no completions are available, return an empty list. The editor will handle it
 *   gracefully according to the configured [io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType].
 * - The API is platform-agnostic; internals will bridge to the native backend.
 *
 * Example:
 * ```kotlin
 * editor.withCompleter(object : Completer {
 *     override fun complete(line: String, pos: Int): Pair<Int, List<String>> {
 *         // Offer static commands when cursor is at the beginning of the line
 *         if (pos == 0) return 0 to listOf("help", "exit", "version")
 *
 *         // Complete the last word of the line
 *         val prefixStart = line.lastIndexOf(' ').let { if (it == -1) 0 else it + 1 }
 *         val prefix = line.substring(prefixStart, pos)
 *         val all = listOf("start", "status", "stop", "restart")
 *         val matches = all.filter { it.startsWith(prefix) }
 *         return prefixStart to matches
 *     }
 * })
 * ```
 */
interface Completer {
    /**
     * Compute completions for the given [line] and caret position [pos].
     *
     * Parameters:
     * - line: the full current input buffer as shown to the user.
     * - pos: the caret index within [line] where completion is requested (0..line.length).
     *
     * Returns:
     * - Pair(start, items) where:
     *   - start: the index within [line] from which the replacement should begin.
     *     The editor will replace the text in the range [start, pos) with one of the
     *     provided [items]. Typically, this is the start of the current token/prefix.
     *   - items: a list of candidate strings. Return an empty list if there are no matches.
     */
    fun complete(line: String, pos: Int): Pair<Int, List<String>>
}