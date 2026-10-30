package com.example.challenge

import com.example.data.model.ChallengeDifficulty
import kotlin.random.Random

data class MathProblem(
    val expression: String,
    val answer: Int
)

object MathEngine {

    fun getRequiredSolves(difficulty: ChallengeDifficulty): Int {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> 2
            ChallengeDifficulty.MEDIUM -> 3
            ChallengeDifficulty.HARD -> 4
        }
    }

    fun generateProblem(difficulty: ChallengeDifficulty): MathProblem {
        return when (difficulty) {
            ChallengeDifficulty.EASY -> generateEasyProblem()
            ChallengeDifficulty.MEDIUM -> generateMediumProblem()
            ChallengeDifficulty.HARD -> generateHardProblem()
        }
    }

    private fun generateEasyProblem(): MathProblem {
        val op = Random.nextInt(3)
        return when (op) {
            0 -> {
                // Addition
                val a = Random.nextInt(8, 45)
                val b = Random.nextInt(5, 45)
                MathProblem("$a + $b", a + b)
            }
            1 -> {
                // Subtraction
                val b = Random.nextInt(5, 30)
                val a = b + Random.nextInt(10, 40)
                MathProblem("$a - $b", a - b)
            }
            else -> {
                // Multiplication
                val a = Random.nextInt(3, 10)
                val b = Random.nextInt(3, 10)
                MathProblem("$a × $b", a * b)
            }
        }
    }

    private fun generateMediumProblem(): MathProblem {
        val op = Random.nextInt(3)
        return when (op) {
            0 -> {
                // (a * b) + c
                val a = Random.nextInt(4, 12)
                val b = Random.nextInt(4, 9)
                val c = Random.nextInt(10, 50)
                MathProblem("($a × $b) + $c", (a * b) + c)
            }
            1 -> {
                // (a * b) - c
                val a = Random.nextInt(5, 12)
                val b = Random.nextInt(4, 9)
                val c = Random.nextInt(5, a * b - 5)
                MathProblem("($a × $b) - $c", (a * b) - c)
            }
            else -> {
                // Clean division + addition: (a * b) / b + c
                val b = Random.nextInt(3, 9)
                val quotient = Random.nextInt(6, 18)
                val dividend = b * quotient
                val c = Random.nextInt(10, 40)
                MathProblem("($dividend ÷ $b) + $c", quotient + c)
            }
        }
    }

    private fun generateHardProblem(): MathProblem {
        val type = Random.nextInt(3)
        return when (type) {
            0 -> {
                // (a * b) + (c * d)
                val a = Random.nextInt(6, 15)
                val b = Random.nextInt(4, 12)
                val c = Random.nextInt(4, 10)
                val d = Random.nextInt(3, 9)
                MathProblem("($a × $b) + ($c × $d)", (a * b) + (c * d))
            }
            1 -> {
                // (a * b) - (c * d)
                val a = Random.nextInt(8, 16)
                val b = Random.nextInt(6, 12)
                val c = Random.nextInt(3, 8)
                val d = Random.nextInt(2, 6)
                val result = (a * b) - (c * d)
                MathProblem("($a × $b) - ($c × $d)", result)
            }
            else -> {
                // Clean division with multiplier: ((dividend ÷ b) × c) - d
                val b = Random.nextInt(4, 8)
                val quotient = Random.nextInt(7, 16)
                val dividend = b * quotient
                val c = Random.nextInt(3, 7)
                val mult = quotient * c
                val d = Random.nextInt(4, maxOf(5, mult - 6))
                val result = mult - d
                MathProblem("(($dividend ÷ $b) × $c) - $d", result)
            }
        }
    }
}
