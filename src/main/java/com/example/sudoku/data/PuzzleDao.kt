package com.example.sudoku.data
import androidx.room.*
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao interface PuzzleDao {
    @Query("SELECT * FROM puzzles WHERE id=:id") suspend fun get(id:Long):PuzzleEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun save(p:PuzzleEntity)
}