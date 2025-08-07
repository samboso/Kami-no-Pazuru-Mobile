package com.example.sudoku.data
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName="puzzles")
data class PuzzleEntity(
    @PrimaryKey val id:Long,
    val variant:String,
    val difficulty:Int,
    val puzzle:String,
    val solution:String,
    val started:Long=0L,
    val finished:Long=0L
)