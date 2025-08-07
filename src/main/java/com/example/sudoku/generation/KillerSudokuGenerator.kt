package com.example.sudoku.generation

import com.example.sudoku.model.SudokuBoard
import kotlin.random.Random

/**
 * Killer Sudoku generator.  A Killer Sudoku is built by grouping
 * cells into cages of size 2–5 and summing their solution values.  The
 * puzzle presented to the user has all digits removed from the
 * cages.  Currently this implementation does not expose cage sums or
 * positions to the UI; it simply returns the puzzle and solution.
 */
object KillerSudokuGenerator {
    data class Cage(val cells: List<Pair<Int, Int>>, val sum: Int)
    data class Result(val board: SudokuBoard, val cages: List<Cage>)

    /**
     * Generate a Killer Sudoku puzzle.  Each cage is formed by a
     * connected cluster of 2–5 cells.  Digits are removed from all
     * cells belonging to cages so the player must infer them based on
     * sums (not yet displayed).
     *
     * @param difficulty difficulty bucket 0–2 for the underlying
     * classic Sudoku.
     * @param seed optional random seed.
     * @return [Result] containing the puzzle and cages.
     */
    fun generate(difficulty: Int, seed: Long? = null): Result {
        val rand = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())
        val (puzzle, solution, _) = SudokuGenerator.generate(difficulty, rand.nextLong())
        val board = SudokuBoard(puzzle, solution)
        val remaining = mutableSetOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                remaining.add(r to c)
            }
        }
        val cages = mutableListOf<Cage>()
        while (remaining.isNotEmpty()) {
            val start = remaining.random(rand)
            remaining.remove(start)
            val cage = mutableListOf(start)
            val cageSize = rand.nextInt(2, 6) // 2–5 inclusive
            val frontier = mutableListOf(start)
            while (cage.size < cageSize && frontier.isNotEmpty()) {
                val (r, c) = frontier.removeAt(frontier.lastIndex)
                val neighbours = listOf(r + 1 to c, r - 1 to c, r to c + 1, r to c - 1)
                neighbours.shuffled(rand).forEach { (nr, nc) ->
                    if (nr in 0 until 9 && nc in 0 until 9 && remaining.contains(nr to nc)) {
                        remaining.remove(nr to nc)
                        cage.add(nr to nc)
                        frontier.add(nr to nc)
                        if (cage.size == cageSize) return@forEach
                    }
                }
            }
            // Compute cage sum from solution and remove digits from puzzle
            var sum = 0
            cage.forEach { (r, c) -> sum += solution[r][c] }
            cages.add(Cage(cage.toList(), sum))
            cage.forEach { (r, c) -> board.puzzle[r][c] = 0 }
        }
        return Result(board, cages)
    }
}