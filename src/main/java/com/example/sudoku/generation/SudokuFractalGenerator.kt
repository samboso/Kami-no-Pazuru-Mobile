package com.example.sudoku.generation

import com.example.sudoku.model.SudokuBoard
import kotlin.random.Random

/**
 * Generator for fractal Sudoku puzzles.  A fractal Sudoku is built
 * recursively by laying out nine classic Sudoku boards in a 3×3
 * arrangement.  Each sub‑board uses the same difficulty bucket.  At
 * depth 1 the generator simply returns a classic Sudoku.  Depth 2
 * produces an 18×18 grid, depth 3 a 27×27 grid, and so on.
 */
object SudokuFractalGenerator {
    /**
     * Generate a fractal Sudoku board with the specified [difficulty],
     * [depth] and optional [seed].  Difficulties follow the same
     * interpretation as [SudokuGenerator.generate].
     *
     * @param difficulty difficulty bucket 0–2, see [SudokuGenerator].
     * @param depth recursion depth; values ≤ 1 return a classic board.
     * @param seed optional random seed for reproducibility.
     * @return a [SudokuBoard] containing the puzzle and solution.
     */
    fun generate(difficulty: Int, depth: Int = 2, seed: Long? = null): SudokuBoard {
        val rand = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())
        // Depth 1 → classic
        if (depth <= 1) {
            val (puzzle, solution, diff) = SudokuGenerator.generate(difficulty, seed)
            return SudokuBoard(puzzle, solution)
        }
        // Depth ≥ 2 → generate 9 sub‑boards
        val subboards = mutableListOf<SudokuBoard>()
        repeat(9) {
            val (p, s, _) = SudokuGenerator.generate(difficulty, rand.nextLong())
            subboards.add(SudokuBoard(p, s))
        }
        val size = 9 * depth
        val puzzleBig = Array(size) { IntArray(size) }
        val solutionBig = Array(size) { IntArray(size) }
        var idx = 0
        for (br in 0 until size step 9) {
            for (bc in 0 until size step 9) {
                val sb = subboards[idx++]
                copyGrid(sb.puzzle, puzzleBig, br, bc)
                copyGrid(sb.solution, solutionBig, br, bc)
            }
        }
        return SudokuBoard(puzzleBig, solutionBig)
    }

    /**
     * Copy a 9×9 grid from [src] into [dst] starting at the
     * coordinates ([top], [left]).
     */
    private fun copyGrid(src: Array<IntArray>, dst: Array<IntArray>, top: Int, left: Int) {
        for (r in src.indices) {
            for (c in src[r].indices) {
                dst[top + r][left + c] = src[r][c]
            }
        }
    }
}