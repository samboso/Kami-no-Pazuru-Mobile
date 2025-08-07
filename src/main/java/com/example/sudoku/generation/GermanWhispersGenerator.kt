package com.example.sudoku.generation
import com.example.sudoku.model.SudokuBoard

object GermanWhispersGenerator {
    data class Whisper(val cells: List<Pair<Int,Int>>)
    data class Result(val board: SudokuBoard, val whispers: List<Whisper>)

    fun generate(diff:Int): Result {
        val (p,s,_) = SudokuGenerator.generate(diff)
        val whispers = listOf(
            Whisper(listOf(1 to 0,1 to 1,1 to 2,1 to 3,1 to 4)),
            Whisper(listOf(7 to 8,7 to 7,7 to 6,7 to 5))
        )
        return Result(SudokuBoard(p,s), whispers)
    }
}
