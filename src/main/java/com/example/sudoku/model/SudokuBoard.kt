package com.example.sudoku.model

import android.graphics.PointF

/**
 * Data structure representing a Sudoku board and its solution.
 *
 * The puzzle array contains the starting clues of the puzzle.  Zero
 * values represent empty cells that the player must fill in.  The
 * solution array holds the fully solved grid.  For variants that
 * support non‑rectangular layouts (such as the 4D cross), the
 * optional [active] matrix describes which positions are part of the
 * playable area.  Cells marked as `false` in [active] are
 * considered inactive and should not accept input.
 */
data class SudokuBoard(
    val puzzle: Array<IntArray>,
    val solution: Array<IntArray>,
    val active: Array<BooleanArray>? = null
) {
    /**
     * Total number of rows in the board.
     */
    val rows: Int get() = puzzle.size

    /**
     * Total number of columns in the board.
     */
    val cols: Int get() = if (puzzle.isNotEmpty()) puzzle[0].size else 0

    /**
     * Returns `true` if the cell at [row], [col] is part of the
     * playable area.  When no [active] mask is provided, all cells are
     * considered active.
     */
    fun isActiveCell(row: Int, col: Int): Boolean {
        return active?.get(row)?.get(col) ?: true
    }

    /**
     * Returns `true` if the cell at [row], [col] is a fixed clue from
     * the original puzzle and therefore cannot be changed by the
     * player.  Inactive cells are considered fixed so the UI knows not
     * to allow interaction with them.
     */
    fun isFixedCell(row: Int, col: Int): Boolean {
        return !isActiveCell(row, col) || puzzle[row][col] != 0
    }

    /** Devuelve símbolo para mostrar (0-F en tableros 16×16). */
    fun symbol(v:Int): String =
        if (rows == 16) "0123456789ABCDEF"[v].toString()
        else v.toString()

    /** Posición dentro de la celda para dibujar una nota (1-16). */
    fun notePos(n:Int, cellW:Float): PointF =
        if (rows == 16) {            // cuadrícula 4×4 de notas
            val col = (n) % 4
            val row = (n) / 4
            PointF(col*cellW/4f + cellW/8f, row*cellW/4f + cellW/3f)
        } else {
            val col = (n-1) % 3
            val row = (n-1) / 3
            PointF(col*cellW/3f + cellW/6f, row*cellW/3f + cellW/2.8f)
        }
    fun SudokuBoard.encode(): String =
        puzzle.joinToString("|") { row -> row.joinToString(",") }

    /** Construye un SudokuBoard desde la cadena generada por [encode]. */
    fun SudokuBoard.Companion.decode(csv: String): SudokuBoard {
        val p = csv.split('|')
            .map { row -> row.split(',').map(String::toInt).toIntArray() }
            .toTypedArray()
        val solution = SudokuSolver.solveCopy(p)   // usa tu solver clásico
        return SudokuBoard(p, solution)
    }
}