package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.Highlighter
import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleHighlighterTest {
    @Test
    fun defaultHighlighterPassThrough() {
        val h = object : Highlighter {}
        assertEquals("hint", h.highlightHint("hint"))
        assertEquals("> ", h.highlightPrompt("> ", isDefault = true))
        assertEquals("cand", h.highlightCandidate("cand", CompletionType.CIRCULAR))
    }

    @Test
    fun simpleHighlighterAppliesAnsi() {
        val h = SimpleHighlighter()
        assertEquals("\u001B[90mh\u001B[0m", h.highlightHint("h"))
        assertEquals("\u001B[32m> \u001B[0m", h.highlightPrompt("> ", isDefault = true))
        assertEquals("\u001B[36mc\u001B[0m", h.highlightCandidate("c", CompletionType.LIST))
    }

    @Test
    fun simpleHighlighterEmptyStrings() {
        val h = SimpleHighlighter()
        assertEquals("\u001B[90m\u001B[0m", h.highlightHint(""))
        assertEquals("\u001B[32m\u001B[0m", h.highlightPrompt("", isDefault = true))
        assertEquals("\u001B[36m\u001B[0m", h.highlightCandidate("", CompletionType.LIST))
    }

    @Test
    fun simpleHighlighterPromptIgnoresIsDefault() {
        val h = SimpleHighlighter()
        val a = h.highlightPrompt("PROMPT", isDefault = true)
        val b = h.highlightPrompt("PROMPT", isDefault = false)
        assertEquals(a, b, "SimpleHighlighter should ignore isDefault and always color the same way")
    }

    @Test
    fun highlighterPassThroughWithUnicode() {
        val h = object : Highlighter {}
        val text = "提示🙂 > Πράξη"
        assertEquals(text, h.highlightHint(text))
        assertEquals(text, h.highlightPrompt(text, isDefault = false))
        assertEquals(text, h.highlightCandidate(text, CompletionType.CIRCULAR))
    }

    @Test
    fun simpleHighlighterPreservesUnicodeContent() {
        val h = SimpleHighlighter()
        val content = "π🙂ABC"
        assertEquals("\u001B[90m${content}\u001B[0m", h.highlightHint(content))
        assertEquals("\u001B[32m${content}\u001B[0m", h.highlightPrompt(content, isDefault = false))
        assertEquals("\u001B[36m${content}\u001B[0m", h.highlightCandidate(content, CompletionType.CIRCULAR))
    }
}
