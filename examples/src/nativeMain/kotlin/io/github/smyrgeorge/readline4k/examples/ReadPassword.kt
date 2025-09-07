package io.github.smyrgeorge.readline4k.examples

import io.github.smyrgeorge.readline4k.impl.PasswordHighlighter
import io.github.smyrgeorge.readline4k.impl.SimpleLineEditor

object ReadPassword {
    fun main() {
        val editor = SimpleLineEditor().installHighlighter(PasswordHighlighter())
        println("Welcome to the LineEditor ReadPassword example!")
        println("Press Ctrl+C to exit")
        editor.setAutoAddHistory(false) // Make sure password is not added to history.
        val password = editor.readLine("Password: ").getOrElse { return }
        println("Your password is: $password")
    }
}
