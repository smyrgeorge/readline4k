@file:OptIn(ExperimentalNativeApi::class)
@file:Suppress("unused")

package io.github.smyrgeorge.readline4k

import kotlin.experimental.ExperimentalNativeApi

/**
 * Configuration for the interactive line editor.
 *
 * This data class centralizes all tunables that control history handling, completion behavior,
 * editing mode, colors, I/O behavior, and various terminal capabilities. It is immutable; create
 * a new instance (or use named parameters) to adjust specific settings.
 *
 * Typical usage:
 * - Construct a config, pass it to your LineEditor implementation (e.g., CustomLineEditor).
 * - Most parameters have sensible defaults. Override only what you need.
 * - Some options are platform/terminal-dependent; see notes below.
 *
 * Platform notes:
 * - [BellStyle.default] returns NONE on Windows and AUDIBLE otherwise.
 * - [enableBracketedPaste], [enableSynchronizedOutput], and [enableSignals] are only meaningful on
 *   Unix-like platforms; they are ignored where unsupported.
 * - [colorMode] can be used to force or disable colors regardless of terminal detection.
 *
 * Properties overview:
 * @property maxHistorySize              Maximum number of entries stored in history before older ones are dropped.
 * @property historyDuplicates           Strategy for handling duplicate consecutive history lines.
 * @property historyIgnoreSpace          If true, lines starting with a space are not added to history.
 * @property completionType              Strategy for how completion is applied/presented.
 * @property completionShowAllIfAmbiguous When LIST mode is used, whether to immediately show all ambiguous matches.
 * @property completionPromptLimit       Maximum number of candidates shown per page when listing matches.
 * @property keySeqTimeout               Milliseconds to wait when reading an ambiguous key sequence; null uses the default.
 * @property editMode                    Base keymap style (Emacs or Vi).
 * @property autoAddHistory              If true, non-blank lines returned by readLine are auto-added to history.
 * @property bellStyle                   Audible/visible bell behavior when signaling (e.g., invalid key press).
 * @property colorMode                   Global colorization mode for prompts, hints, and candidates.
 * @property behavior                    Whether to use stdio or prefer terminal-style interaction.
 * @property tabStop                     Horizontal width of a tab character, used for cursor computations.
 * @property indentSize                  Indentation size used by indent/dedent editor commands.
 * @property checkCursorPosition         If true, check cursor is at column 0 before displaying prompt (helps with messy outputs).
 * @property enableBracketedPaste        Enables bracketed paste on Unix-like platforms to avoid accidental execution.
 * @property enableSynchronizedOutput    Enables synchronized output on Unix-like platforms to reduce flicker/tearing.
 * @property enableSignals               If true, termios signals are enabled (Unix); when false, they may be disabled.
 */
data class LineEditorConfig(
    val maxHistorySize: Int = 100,
    val historyDuplicates: HistoryDuplicates = HistoryDuplicates.IGNORE_CONSECUTIVE,
    val historyIgnoreSpace: Boolean = false,
    val completionType: CompletionType = CompletionType.CIRCULAR,
    val completionShowAllIfAmbiguous: Boolean = false,
    val completionPromptLimit: Int = 100,
    val keySeqTimeout: Int? = null,
    val editMode: EditMode = EditMode.EMACS,
    val autoAddHistory: Boolean = false,
    val bellStyle: BellStyle = BellStyle.default(),
    val colorMode: ColorMode = ColorMode.ENABLED,
    val behavior: Behavior = Behavior.STDIO,
    val tabStop: Int = 8,
    val indentSize: Int = 2,
    val checkCursorPosition: Boolean = false,
    val enableBracketedPaste: Boolean = true,
    val enableSynchronizedOutput: Boolean = true,
    val enableSignals: Boolean = false
) {

    /**
     * Controls how the editor signals events such as errors or invalid keys.
     *
     * Depending on the platform and terminal, not all styles may be effective.
     * See [default] for platform-specific defaults.
     */
    enum class BellStyle {
        /** Beep */
        AUDIBLE,

        /** Silent */
        NONE,

        /** Flash screen (not supported) */
        VISIBLE;

        companion object {
            /**
             * Returns the recommended default bell style for the current platform.
             *
             * - Windows: [NONE] to avoid console beeps that are commonly undesirable.
             * - Other OSes: [AUDIBLE], which typically maps to a terminal bell (\u0007).
             */
            fun default(): BellStyle {
                return if (Platform.osFamily == OsFamily.WINDOWS) {
                    NONE
                } else {
                    AUDIBLE
                }
            }
        }
    }

    /**
     * Controls how duplicate lines are handled when adding to history.
     *
     * This only affects insertion; existing history is not retroactively pruned.
     */
    enum class HistoryDuplicates {
        /** No filter */
        ALWAYS_ADD,

        /** A line will not be added to the history if it matches the previous entry */
        IGNORE_CONSECUTIVE
    }

    /**
     * Tab completion style used by the editor.
     *
     * - [CIRCULAR]: cycles through full matches (similar to Vim default).
     * - [LIST]: completes to the longest common prefix and can list all matches
     *   when ambiguity exists (similar to GNU Readline/Bash).
     */
    enum class CompletionType {
        CIRCULAR,
        LIST
    }

    /**
     * Base editing mode that selects a standard keymap and editing semantics.
     *
     * - [EMACS]: common shortcuts such as Ctrl-A, Ctrl-E, etc.
     * - [VI]: modal editing with normal/insert modes and vi-like movements.
     */
    enum class EditMode {
        EMACS,
        VI
    }

    /**
     * Colorization mode for editor-rendered elements (prompt, hint, candidates).
     *
     * This can be used to force color output, auto-enable where supported, or
     * completely disable color regardless of terminal capability.
     */
    enum class ColorMode {
        /** Activate highlighting if platform/terminal is supported */
        ENABLED,

        /** Activate highlighting even if platform is not supported */
        FORCED,

        /** Deactivate highlighting even if platform/terminal is supported */
        DISABLED
    }

    /**
     * Determines how the editor interacts with I/O.
     *
     * - [STDIO]: use standard input/output directly.
     * - [PREFER_TERM]: try to use terminal-style interaction whenever possible,
     *   even if stdin/stdout are not terminals.
     */
    enum class Behavior {
        STDIO,
        PREFER_TERM
    }
}
