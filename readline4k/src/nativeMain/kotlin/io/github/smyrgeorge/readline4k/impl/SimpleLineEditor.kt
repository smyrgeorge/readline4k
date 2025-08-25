package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig

class SimpleLineEditor(
    linePrefix: String = "> ",
    config: LineEditorConfig = LineEditorConfig()
) : AbstractLineEditor(linePrefix, config)