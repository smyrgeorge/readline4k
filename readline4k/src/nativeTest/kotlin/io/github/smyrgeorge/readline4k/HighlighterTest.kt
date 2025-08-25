package io.github.smyrgeorge.readline4k

import io.github.smyrgeorge.readline4k.Highlighter
import io.github.smyrgeorge.readline4k.LineEditorConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class HighlighterTest {
    @Test
    fun defaultHighlighterPassThrough() {
        val h = object : Highlighter {}
        assertEquals("hint", h.highlightHint("hint"))
        assertEquals("> ", h.highlightPrompt("> ", isDefault = true))
        assertEquals("cand", h.highlightCandidate("cand", LineEditorConfig.CompletionType.CIRCULAR))
    }
}
