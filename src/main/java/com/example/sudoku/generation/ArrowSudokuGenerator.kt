package com.example.sudoku.generation
import com.example.sudoku.model.SudokuBoard

object ArrowSudokuGenerator {
    data class Arrow(val stem: Pair<Int,Int>, val cells: List<Pair<Int,Int>>)
    data class Result(val board: SudokuBoard, val arrows: List<Arrow>)

    fun generate(diff:Int): Result {
        val (p,s,_) = SudokuGenerator.generate(diff)
        val arrows = listOf(
            Arrow(2 to 2, listOf(2 to 3, 2 to 4, 3 to 4)),
            Arrow(6 to 6, listOf(5 to 6, 5 to 7, 4 to 7))
        )
        return Result(SudokuBoard(p,s), arrows)
    }
}
