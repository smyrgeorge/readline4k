@file:OptIn(ExperimentalForeignApi::class)

package io.github.smyrgeorge.readline4k.impl

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.convert
import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemPathSeparator
import platform.posix.chdir
import platform.posix.getcwd
import platform.posix.getenv
import platform.posix.getpid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SimpleFileCompleterTest {
    @Test
    fun completesFilesWithinAbsoluteDirectory() {
        val tmpDir = makeTempDir()
        try {
            // Create files and dirs using kotlinx-io
            touch("$tmpDir/file1.txt")
            touch("$tmpDir/fish.sh")
            touch("$tmpDir/.hidden.txt")
            SystemFileSystem.createDirectories(Path("$tmpDir/final"))

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
            // Best-effort cleanup using kotlinx-io
            deleteFile("$tmpDir/file1.txt")
            deleteFile("$tmpDir/fish.sh")
            deleteFile("$tmpDir/.hidden.txt")
            deleteDirectory("$tmpDir/final")
            deleteDirectory(tmpDir)
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
            SystemFileSystem.createDirectories(Path("$tmpDir/app"))

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
            deleteFile("$tmpDir/apple.txt")
            deleteFile("$tmpDir/application.md")
            deleteDirectory("$tmpDir/app")
            deleteDirectory(tmpDir)
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
            deleteFile("$tmpDir/.dotfile")
            deleteFile("$tmpDir/.dog")
            deleteFile("$tmpDir/normal")
            deleteDirectory(tmpDir)
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
            deleteFile("$tmpDir/alpha")
            deleteDirectory(tmpDir)
        }
    }

    private fun getPathSeparator(): String =
        SystemPathSeparator.toString()

    private fun touch(path: String): Unit =
        SystemFileSystem.sink(Path(path)).use { }

    private fun deleteFile(path: String): Unit =
        SystemFileSystem.delete(Path(path), mustExist = false)

    private fun deleteDirectory(path: String): Unit =
        SystemFileSystem.delete(Path(path), mustExist = false)

    private fun makeTempDir(): String {
        val base = getenv("TMPDIR")?.toKString()
            ?.ifBlank { null }
            ?: getenv("TEMP")?.toKString()?.ifBlank { null }
            ?: "/tmp"
        var idx = 0
        while (true) {
            val candidate = "$base/readline4k_test_${getpid()}_$idx"
            try {
                SystemFileSystem.createDirectories(Path(candidate))
                return candidate
            } catch (_: Throwable) {
            }
            idx++
            if (idx > 1000) error("Could not create a temp directory under $base")
        }
    }
}