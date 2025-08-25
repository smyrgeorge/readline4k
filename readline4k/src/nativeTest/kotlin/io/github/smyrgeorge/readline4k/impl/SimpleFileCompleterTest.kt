@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleFileCompleterTest {
    @Test
    fun completesFilesWithinAbsoluteDirectory() {
        val tmpDir = makeTempDir()
        try {
            // Create files and dirs
            touch("$tmpDir/file1.txt")
            touch("$tmpDir/fish.sh")
            touch("$tmpDir/.hidden.txt")
            mkdir("$tmpDir/final", 0x1FF.toUShort()) // 0777

            val completer = SimpleFileCompleter()
            val prefix = "$tmpDir/fi"
            val (start, candidates) = completer.complete(prefix, prefix.length)

            // start should point to after last space in line; our line has no spaces
            assertEquals(0, start, "start index should be 0 for a single-token absolute path")

            // Should include matches starting with "fi"; directory should end with path separator
            val sep = getPathSeparator()
            assertTrue(candidates.contains("${tmpDir}${sep}file1.txt"), "Expected file1.txt in candidates: $candidates")
            assertTrue(candidates.contains("${tmpDir}${sep}fish.sh"), "Expected fish.sh in candidates: $candidates")
            assertTrue(
                candidates.contains("${tmpDir}${sep}final${sep}"),
                "Expected directory 'final' with trailing separator in candidates: $candidates"
            )

            // Hidden file should not be suggested because prefix does not start with '.'
            assertTrue(
                candidates.none { it.endsWith(".hidden.txt") },
                "Hidden files must be filtered out when prefix doesn't start with '.'"
            )
        } finally {
            // Best-effort cleanup
            rm("$tmpDir/file1.txt")
            rm("$tmpDir/fish.sh")
            rm("$tmpDir/.hidden.txt")
            rmdir("$tmpDir/final")
            rmdir(tmpDir)
        }
    }

    @Test
    fun completesRelativePathsFromCurrentDirectory() {
        val oldCwdBuf = ByteArray(4096)
        val oldCwdPtr = getcwd(oldCwdBuf.refTo(0), oldCwdBuf.size.convert())
        val oldCwd = oldCwdPtr?.toKString() ?: "."

        val tmpDir = makeTempDir()
        try {
            // Prepare files in tmpDir and chdir into it
            touch("$tmpDir/apple.txt")
            touch("$tmpDir/application.md")
            mkdir("$tmpDir/app", 0x1FF.toUShort())

            chdir(tmpDir)

            val completer = SimpleFileCompleter()
            val (start, candidates) = completer.complete("ap", 2)

            // Expect start at 0 and candidates within current dir (no dirPart prefix)
            assertEquals(0, start)
            val sep = getPathSeparator()
            assertTrue(candidates.contains("apple.txt"))
            assertTrue(candidates.contains("application.md"))
            assertTrue(candidates.contains("app${sep}"), "Directory must have trailing separator: $candidates")

            // Ensure results are sorted
            val sorted = candidates.sorted()
            assertEquals(sorted, candidates, "Candidates must be sorted")
        } finally {
            chdir(oldCwd)
            rm("$tmpDir/apple.txt")
            rm("$tmpDir/application.md")
            rmdir("$tmpDir/app")
            rmdir(tmpDir)
        }
    }

    @Test
    fun showsHiddenWhenPrefixStartsWithDot() {
        val tmpDir = makeTempDir()
        try {
            touch("$tmpDir/.dotfile")
            touch("$tmpDir/.dog")
            touch("$tmpDir/normal")

            val completer = SimpleFileCompleter()
            val (start, candidates) = completer.complete("$tmpDir/.do", ("$tmpDir/.do").length)

            assertEquals(0, start)
            val sep = getPathSeparator()
            assertTrue(
                candidates.contains("${tmpDir}${sep}.dotfile"),
                "Hidden files should be included when prefix starts with '.'"
            )
            assertTrue(
                candidates.contains("${tmpDir}${sep}.dog"),
                "Hidden files should be included when prefix starts with '.'"
            )
            // 'normal' should not appear due to prefix mismatch
            assertTrue(candidates.none { it.endsWith("normal") })
        } finally {
            rm("$tmpDir/.dotfile")
            rm("$tmpDir/.dog")
            rm("$tmpDir/normal")
            rmdir(tmpDir)
        }
    }

    @Test
    fun nonexistentDirectoryYieldsNoCandidates() {
        val completer = SimpleFileCompleter()
        val prefix = "/path/that/does/not/exist/fi"
        val (_, candidates) = completer.complete(prefix, prefix.length)
        assertTrue(candidates.isEmpty(), "Expected no candidates for nonexistent directory")
    }


    @Test
    fun computesStartIndexAfterLastSpace() {
        val tmpDir = makeTempDir()
        try {
            touch("$tmpDir/alpha")
            val sep = getPathSeparator()
            val prefix = "echo ${tmpDir}${sep}al"
            val completer = SimpleFileCompleter()
            val (start, candidates) = completer.complete(prefix, prefix.length)

            // Start should be after "echo " (5 chars)
            assertEquals(5, start)
            assertTrue(candidates.any { it.endsWith("alpha") })
        } finally {
            rm("$tmpDir/alpha")
            rmdir(tmpDir)
        }
    }

    private fun getPathSeparator(): String {
        // SimpleFileCompleter uses kotlinx.io.files.SystemPathSeparator which translates to OS-specific
        // Here we approximate via platform detection: on Windows, use '\\', otherwise '/'
        return if (isWindows()) "\\" else "/"
    }

    private fun isWindows(): Boolean {
        // Very lightweight detection using preprocessor-like macros aren't available; read from uname/OS
        val os = getenv("OS")?.toKString() ?: ""
        if (os.contains("Windows", ignoreCase = true)) return true
        // Fallback by checking path separator observed by runtime
        // If current working directory contains backslash, assume Windows
        val buf = ByteArray(4096)
        val path = getcwd(buf.refTo(0), buf.size.convert())
        if (path != null) {
            val s = path.toKString()
            if (s.contains('\\')) return true
        }
        return false
    }

    private fun touch(path: String) {
        val f = fopen(path, "w") ?: return
        fclose(f)
    }

    private fun rm(path: String) {
        remove(path)
    }

    private fun makeTempDir(): String {
        val base = getenv("TMPDIR")?.toKString()
            ?.ifBlank { null }
            ?: getenv("TEMP")?.toKString()?.ifBlank { null }
            ?: "/tmp"
        var idx = 0
        while (true) {
            val candidate = $$"$$base/readline4k_test_$pid_$idx"
            if (mkdir(candidate, 0x1C0.toUShort()) == 0) { // 0700
                return candidate
            }
            idx++
            if (idx > 1000) error("Could not create a temp directory under $base")
        }
    }
}