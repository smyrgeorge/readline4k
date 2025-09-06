package io.github.smyrgeorge.readline4k.examples

import io.github.smyrgeorge.readline4k.impl.SimpleLineEditor

object Minimal {
    fun main() {
        val editor = SimpleLineEditor()

        println("Welcome to the LineEditor Minimal example!")
        println("Press Ctrl+C to exit")

        while (true) {
            val line = editor.readLine().getOrElse { break }
            println(line)
        }
    }
}