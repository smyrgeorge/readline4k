package io.github.smyrgeorge.readline4k.impl

import io.github.smyrgeorge.readline4k.AbstractLineEditor
import io.github.smyrgeorge.readline4k.LineEditorConfig

/**
 * A ready‑to‑use [AbstractLineEditor] with sensible defaults.
 *
 * SimpleLineEditor wires the base line editor with a default prompt ("> ") and a
 * default [LineEditorConfig]. It is intended for quick setups and examples where
 * no custom behavior is required.
 *
 * Typical usage:
 * - Create an instance and call [AbstractLineEditor.readLine] in a loop.
 * - For syntax highlighting or completion, compose it with [SimpleHighlighter] and
 *   [SimpleFileCompleter] via the configuration object if needed.
 *
 * Note: If you need to customize key bindings, history persistence, or rendering,
 * consider using [AbstractLineEditor] directly with a tailored [LineEditorConfig].
 *
 * @param linePrefix The prompt displayed before each input line. Defaults to "> ".
 * @param config Editor configuration controlling behavior such as completion type,
 * history, rendering, and key handling. Defaults to a new [LineEditorConfig].
 */
class SimpleLineEditor(
    linePrefix: String = "> ",
    config: LineEditorConfig = LineEditorConfig()
) : AbstractLineEditor(linePrefix, config)