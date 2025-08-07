package com.example.sudoku.generation

import kotlin.random.Random

/**
 * Classic Sudoku generator ported from the provided Python
 * implementation.  The generator produces a valid puzzle with a
 * guaranteed unique solution by filling a board using backtracking and
 * then removing numbers until the desired clue count is reached.  A
 * simple solver is used to ensure uniqueness during clue removal.
 */
object SudokuGenerator {
    /**
     * Difficulty buckets define the target clue ranges for easy,
     * medium and hard puzzles.  These values mirror those used in
     * the original Python code (inclusive ranges).  Values outside
     * this map will default to the medium bucket.
     */
    /**
     * Map of difficulty bucket to clue count ranges.  The values are
     * inclusive ranges that determine how many clues remain in the
     * generated puzzle.  Lower ranges correspond to harder puzzles.
     *
     * 0 – Fácil: 40–81 clues
     * 1 – Media: 32–39 clues
     * 2 – Difícil: 24–31 clues
     * 3 – Maestro: 17–23 clues
     * 4 – Infernal/Inhumano: 17–20 clues (tighter range for extra difficulty)
     */
    private val CLUE_RANGE: Map<Int, IntRange> = mapOf(
        0 to (40..81),
        1 to (32..39),
        2 to (24..31),
        3 to (22..28),
        4 to (17..21)
    )

    /**
     * Generate a Sudoku puzzle along with its solution.  The returned
     * difficulty bucket may differ from the requested one if the final
     * clue count falls into a different range.
     *
     * @param difficulty target difficulty bucket: 0 easy, 1 medium, 2 hard.
     * @param seed optional random seed for reproducibility.
     * @return Triple of puzzle grid, solution grid and actual difficulty bucket.
     */
    fun generate(difficulty: Int = 1, seed: Long? = null): Triple<Array<IntArray>, Array<IntArray>, Int> {
        val rand: Random = seed?.let { Random(it) } ?: Random(System.currentTimeMillis())

        // Step 1: produce a completely filled valid board
        val full = Array(9) { IntArray(9) }
        fillBoard(full, rand)
        val solution = copyBoard(full)

        // Determine target clue count
        val range = CLUE_RANGE[difficulty] ?: CLUE_RANGE[1]!!
        val cluesTarget = rand.nextInt(range.first, range.last + 1)

        // Create a working puzzle copy
        val puzzle = copyBoard(full)
        // Shuffle cell positions for random removal order
        val positions = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                positions.add(r to c)
            }
        }
        positions.shuffle(rand)

        for ((r, c) in positions) {
            // Stop when we've reached the desired number of clues
            val remaining = puzzle.sumOf { row -> row.count { it != 0 } }
            if (remaining <= cluesTarget) {
                break
            }
            val backup = puzzle[r][c]
            puzzle[r][c] = 0
            // Check uniqueness by counting solutions up to 2
            val count = solveCount(copyBoard(puzzle), limit = 2)
            if (count != 1) {
                // revert removal if solution not unique
                puzzle[r][c] = backup
            }
        }

        // Compute actual difficulty based on final clue count
        val actualClues = puzzle.sumOf { row -> row.count { it != 0 } }
        var actualDifficulty = difficulty
        for ((bucket, clueRange) in CLUE_RANGE) {
            if (actualClues in clueRange) {
                actualDifficulty = bucket
                break
            }
        }

        return Triple(puzzle, solution, actualDifficulty)
    }

    /**
     * Recursively fill the [board] using backtracking.  Returns true
     * when a complete solution has been found.  Empty cells are
     * represented by zeros.  The [rand] parameter controls the order
     * in which numbers are tried.
     */
    private fun fillBoard(board: Array<IntArray>, rand: Random): Boolean {
        for (r in 0 until 9) {
            for (c in 0 until 9) {
                if (board[r][c] == 0) {
                    val nums = (1..9).toMutableList()
                    nums.shuffle(rand)
                    for (n in nums) {
                        if (isSafe(board, r, c, n)) {
                            board[r][c] = n
                            if (fillBoard(board, rand)) {
                                return true
                            }
                            board[r][c] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    /**
     * Determine whether placing [n] at position ([row], [col]) in
     * [board] violates any Sudoku rules.
     */
    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, n: Int): Boolean {
        // Check row and column
        for (i in 0 until 9) {
            if (board[row][i] == n || board[i][col] == n) return false
        }
        // Check 3×3 subgrid
        val boxRow = 3 * (row / 3)
        val boxCol = 3 * (col / 3)
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[boxRow + i][boxCol + j] == n) return false
            }
        }
        return true
    }

    /**
     * Count the number of solutions for a given board using
     * backtracking.  The solver searches for up to [limit] solutions
     * and stops early once that limit has been reached.  Returning a
     * number greater than one indicates the puzzle is not uniquely
     * solvable.
     */
    private fun solveCount(board: Array<IntArray>, limit: Int = 2): Int {
        var count = 0
        fun solve(cell: Int = 0) {
            if (count >= limit) return
            if (cell == 81) {
                count++
                return
            }
            val r = cell / 9
            val c = cell % 9
            if (board[r][c] != 0) {
                solve(cell + 1)
                return
            }
            for (n in 1..9) {
                if (isSafe(board, r, c, n)) {
                    board[r][c] = n
                    solve(cell + 1)
                    board[r][c] = 0
                    if (count >= limit) return
                }
            }
        }
        solve()
        return count
    }

    /**
     * Create a deep copy of a 9×9 Sudoku grid.
     */
    private fun copyBoard(src: Array<IntArray>): Array<IntArray> {
        return Array(src.size) { r -> src[r].clone() }
    }
}