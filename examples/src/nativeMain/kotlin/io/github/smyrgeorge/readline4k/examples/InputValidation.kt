package io.github.smyrgeorge.readline4k.examples

import io.github.smyrgeorge.readline4k.Validator
import io.github.smyrgeorge.readline4k.Validator.Validation
import io.github.smyrgeorge.readline4k.impl.SimpleLineEditor

object InputValidation {
    class NumberValidator : Validator {
        override fun validate(line: String, pos: Int): Validation {
            return when {
                line.toIntOrNull() == null -> Validation.Invalid(" Not a number!")
                else -> Validation.Valid()
            }
        }
    }

    fun main() {
        val editor = SimpleLineEditor().setValidator(NumberValidator())
        println("Welcome to the LineEditor InputValidation example!")
        println("Press Ctrl+C to exit")
        val line = editor.readLine("Number only> ").getOrElse { return }
        println(line)
    }
}