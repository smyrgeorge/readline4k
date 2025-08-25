package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.LineEditorConfig.CompletionType
import io.github.smyrgeorge.readline4k.tools.Highlighter

class SimpleHighlighter : Highlighter {
    override fun highlightHint(hint: String): String = "\u001B[90m$hint\u001B[0m"
    override fun highlightPrompt(prompt: String, isDefault: Boolean): String = "\u001B[32m$prompt\u001B[0m"
    override fun highlightCandidate(candidate: String, completionType: CompletionType): String =
        "\u001B[36m$candidate\u001B[0m"
}