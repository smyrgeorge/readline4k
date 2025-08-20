@file:OptIn(ExperimentalNativeApi::class)
@file:Suppress("unused")

package io.github.smyrgeorge.readline4k

import kotlin.experimental.ExperimentalNativeApi

/**
 * User preferences for line editor configuration
 */
data class LineEditorConfig(
    /** Maximum number of entries in History */
    val maxHistorySize: Int = 100,
    val historyDuplicates: HistoryDuplicates = HistoryDuplicates.IGNORE_CONSECUTIVE,
    val historyIgnoreSpace: Boolean = false,
    val completionType: CompletionType = CompletionType.CIRCULAR,
    /** Directly show all alternatives or not when CompletionType.LIST is being used */
    val completionShowAllIfAmbiguous: Boolean = false,
    /** When listing completion alternatives, only display one screen of possibilities at a time */
    val completionPromptLimit: Int = 100,
    /** Duration (milliseconds) that we will wait for a character when reading an ambiguous key sequence */
    val keySeqTimeout: Int? = null,
    /** Emacs or Vi mode */
    val editMode: EditMode = EditMode.EMACS,
    /** If true, each nonblank line returned by readline will be automatically added to the history */
    val autoAddHistory: Boolean = false,
    /** Beep or Flash or nothing */
    val bellStyle: BellStyle = BellStyle.default(),
    /** If colors should be enabled */
    val colorMode: ColorMode = ColorMode.ENABLED,
    /** Whether to use stdio or not */
    val behavior: Behavior = Behavior.STDIO,
    /** Horizontal space taken by a tab */
    val tabStop: Int = 8,
    /** Indentation size for indent/dedent commands */
    val indentSize: Int = 2,
    /** Check if cursor position is at leftmost before displaying prompt */
    val checkCursorPosition: Boolean = false,
    /** Bracketed paste on unix platform */
    val enableBracketedPaste: Boolean = true,
    /** Synchronized output on unix platform */
    val enableSynchronizedOutput: Boolean = true,
    /** Whether to disable or not the signals in termios */
    val enableSignals: Boolean = false
) {

    /** Beep or flash or nothing */
    enum class BellStyle {
        /** Beep */
        AUDIBLE,

        /** Silent */
        NONE,

        /** Flash screen (not supported) */
        VISIBLE;

        companion object {
            fun default(): BellStyle {
                return if (Platform.osFamily == OsFamily.WINDOWS) {
                    NONE
                } else {
                    AUDIBLE
                }
            }
        }
    }

    /** History filter */
    enum class HistoryDuplicates {
        /** No filter */
        ALWAYS_ADD,

        /** A line will not be added to the history if it matches the previous entry */
        IGNORE_CONSECUTIVE
    }

    /** Tab completion style */
    enum class CompletionType {
        /** Complete the next full match (like in Vim by default) */
        CIRCULAR,

        /** Complete till longest match. When more than one match, list all matches (like in Bash/Readline) */
        LIST
    }

    /** Style of editing / Standard keymaps */
    enum class EditMode {
        /** Emacs keymap */
        EMACS,

        /** Vi keymap */
        VI
    }

    /** Colorization mode */
    enum class ColorMode {
        /** Activate highlighting if platform/terminal is supported */
        ENABLED,

        /** Activate highlighting even if platform is not supported */
        FORCED,

        /** Deactivate highlighting even if platform/terminal is supported */
        DISABLED
    }

    /** Should the editor use stdio */
    enum class Behavior {
        /** Use stdin / stdout */
        STDIO,

        /** Use terminal-style interaction whenever possible, even if stdin/stdout are not terminals */
        PREFER_TERM
    }
}
