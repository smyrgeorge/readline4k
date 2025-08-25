@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.Completer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import platform.posix.getenv

/**
 * A filesystem-aware [Completer] that suggests file and directory paths.
 *
 * Behavior:
 * - Completes only the current token, where tokens are separated by whitespace.
 * - Supports relative segments and tilde ("~/") expansion for lookup.
 * - Filters dotfiles unless the typed prefix itself starts with '.'.
 * - Appends the platform path separator to directory suggestions to ease further navigation.
 * - Sorts suggestions lexicographically.
 *
 * Examples:
 * - Input: "cat src/na", caret at end → suggestions like "src/nativeMain/", "src/nativeTest/".
 * - Input: "open ~/<tab>" → expands HOME for listing only; suggestions contain user folders.
 *
 * Note: Directory detection is best-effort by checking if it can be listed via `SystemFileSystem.list`.
 */
class SimpleFileCompleter : Completer {
    /**
     * Produces completion suggestions based on the current token under the caret.
     *
     * @param line The full input line.
     * @param pos The caret position within [line].
     * @return A pair of (startIndex, suggestions) where startIndex is the index within [line]
     *         where the replacement should begin, and suggestions are candidate strings.
     */
    override fun complete(line: String, pos: Int): Pair<Int, List<String>> {
        val start = lastTokenStart(line, pos)
        val token = safeSubstring(line, start, pos)
        if (token.isEmpty()) {
            // Default to listing current directory when token empty
            val suggestions = suggestFor("", dirPart = "", displayDirPart = "")
            return start to suggestions
        }

        val sepChars = charArrayOf('/', '\\')
        val lastSep = token.lastIndexOfAny(sepChars)
        val dirPart = if (lastSep >= 0) token.take(lastSep + 1) else ""
        val namePrefix = if (lastSep >= 0) token.substring(lastSep + 1) else token

        val suggestions = suggestFor(namePrefix, dirPart = dirPart, displayDirPart = dirPart)
        return start to suggestions
    }

    private fun suggestFor(prefix: String, dirPart: String, displayDirPart: String): List<String> {
        val expandedDir = expandForLookup(dirPart.ifEmpty { ".$PATH_SEPARATOR" })
        val basePath = try {
            Path(expandedDir.removeSuffix(PATH_SEPARATOR))
        } catch (_: Throwable) {
            return emptyList()
        }

        val includeDotfiles = prefix.startsWith('.')

        val list = try {
            SystemFileSystem.list(basePath)
        } catch (_: Throwable) {
            // Directory not accessible
            return emptyList()
        }

        val suggestions = mutableListOf<String>()
        for (child in list) {
            val name = child.name
            if (!includeDotfiles && name.startsWith('.')) continue
            if (!name.startsWith(prefix)) continue

            val isDir = try {
                // If we can list the child path, consider it a directory.
                SystemFileSystem.list(child)
                true
            } catch (_: Throwable) {
                false
            }

            val addSlash = if (isDir) PATH_SEPARATOR else ""
            suggestions += displayDirPart + name + addSlash
        }
        return suggestions.sorted()
    }

    private fun lastTokenStart(line: String, pos: Int): Int {
        if (pos <= 0) return 0
        var i = pos - 1
        while (i >= 0) {
            val c = line[i]
            if (c.isWhitespace()) return i + 1
            i--
        }
        return 0
    }

    private fun safeSubstring(s: String, start: Int, end: Int): String {
        val s0 = start.coerceAtLeast(0).coerceAtMost(s.length)
        val e0 = end.coerceAtLeast(s0).coerceAtMost(s.length)
        return s.substring(s0, e0)
    }

    private fun expandForLookup(p: String): String {
        if (p.startsWith("~/")) {
            val home = getenv("HOME")?.toKString() ?: ""
            if (home.isNotEmpty()) return home.trimEnd('/', '\\') + PATH_SEPARATOR + p.removePrefix("~/")
        }
        return p
    }

    companion object {
        private val PATH_SEPARATOR: String = SystemPathSeparator.toString()
    }
}
