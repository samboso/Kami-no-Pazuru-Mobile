package com.example.sudoku.generation

import com.example.sudoku.model.SudokuBoard
import kotlin.random.Random

/**
 * Simplified Thermo Sudoku generator.  A Thermo Sudoku is a classic
 * Sudoku augmented with thermometers (snakes of increasing digits).
 * In this implementation we generate a standard puzzle using
 * [SudokuGenerator] and disregard the thermometers for the purposes
 * of gameplay.  The meta information about thermometers is retained
 * should you wish to overlay them on the UI later.
 */
object ThermoSudokuGenerator {
    data class Thermo(val cells: List<Pair<Int, Int>>)
    data class Result(val board: SudokuBoard, val thermos: List<Thermo>)

    /**
     * Generate a Thermo Sudoku puzzle.  Currently thermometers are
     * generated randomly for visual purposes only and do not affect
     * puzzle generation.
     *
     * @param difficulty difficulty bucket 0–4.  Values beyond 2
     *        request harder puzzles with fewer clues.
     * @param seed optional random seed.
     * @return [Result] containing the puzzle and a list of thermos.
     */
    fun generate(difficulty: Int, seed: Long? = null): Result {
        val rand = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())
        val (puzzle, solution, _) = SudokuGenerator.generate(difficulty, rand.nextLong())
        val board = SudokuBoard(puzzle, solution)
        // Create 4–6 random thermometers
        val thermos = mutableListOf<Thermo>()
        val numThermos = rand.nextInt(4, 7)
        repeat(numThermos) {
            thermos.add(Thermo(buildRandomThermo(rand)))
        }
        return Result(board, thermos)
    }

    private fun buildRandomThermo(rand: Random): List<Pair<Int, Int>> {
        val length = rand.nextInt(3, 7)
        val start = rand.nextInt(0, 9) to rand.nextInt(0, 9)
        val thermo = mutableListOf(start)
        while (thermo.size < length) {
            val (r, c) = thermo.last()
            val candidates = mutableListOf<Pair<Int, Int>>()
            if (r + 1 < 9) candidates.add(r + 1 to c)
            if (r - 1 >= 0) candidates.add(r - 1 to c)
            if (c + 1 < 9) candidates.add(r to c + 1)
            if (c - 1 >= 0) candidates.add(r to c - 1)
            candidates.shuffle(rand)
            var added = false
            for (candidate in candidates) {
                if (!thermo.contains(candidate)) {
                    thermo.add(candidate)
                    added = true
                    break
                }
            }
            if (!added) break
        }
        return thermo
    }
}