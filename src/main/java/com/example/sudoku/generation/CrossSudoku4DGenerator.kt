package com.example.sudoku.generation

import com.example.sudoku.model.SudokuBoard
import kotlin.random.Random

/**
 * Generator for 4D cross Sudoku puzzles.  A cross Sudoku consists
 * of five classic Sudoku boards: a central board and four arms
 * (north, east, south and west) arranged in a cross shape.  This
 * generator creates all five sub‑boards using the requested
 * [difficulty] bucket.  Only classic arms are supported in this
 * implementation; variants such as thermo or killer can be added
 * later if desired.
 */
object CrossSudoku4DGenerator {
    /**
     * Generate a cross Sudoku of size 21×21.  The central board is
     * placed at the centre and the four arms are attached above,
     * right, below and left.  Cells that fall outside these five
     * sub‑boards are marked inactive in the returned [SudokuBoard].
     *
     * @param difficulty difficulty bucket 0–2 for all five boards.
     * @param seed optional random seed for reproducibility.
     * @return a [SudokuBoard] with its [SudokuBoard.active] mask set.
     */
    fun generate(difficulty: Int, seed: Long? = null): SudokuBoard {
        val rand = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())
        // Centre board
        val (pc, sc, _) = SudokuGenerator.generate(difficulty, rand.nextLong())
        val centre = SudokuBoard(pc, sc)
        // Generate four arms (all classic for now)
        val arms = mutableListOf<SudokuBoard>()
        repeat(4) {
            val (p, s, _) = SudokuGenerator.generate(difficulty, rand.nextLong())
            arms.add(SudokuBoard(p, s))
        }
        // Create 21×21 big boards
        val size = 21
        val puzzleBig = Array(size) { IntArray(size) }
        val solutionBig = Array(size) { IntArray(size) }
        val active = Array(size) { BooleanArray(size) }
        // Helper to copy sub‑boards and mark active cells
        fun copyWithActive(src: SudokuBoard, top: Int, left: Int) {
            for (r in 0 until 9) {
                for (c in 0 until 9) {
                    puzzleBig[top + r][left + c] = src.puzzle[r][c]
                    solutionBig[top + r][left + c] = src.solution[r][c]
                    active[top + r][left + c] = true
                }
            }
        }
        // Centre at (6,6)
        copyWithActive(centre, 6, 6)
        // North arm at (0,6)
        copyWithActive(arms[0], 0, 6)
        // East arm at (6,12)
        copyWithActive(arms[1], 6, 12)
        // South arm at (12,6)
        copyWithActive(arms[2], 12, 6)
        // West arm at (6,0)
        copyWithActive(arms[3], 6, 0)
        return SudokuBoard(puzzleBig, solutionBig, active)
    }
}