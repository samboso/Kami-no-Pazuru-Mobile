package com.example.sudoku.data
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.sudoku.SudokuApp
@Database(entities=[PuzzleEntity::class], version=1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun dao(): PuzzleDao
    companion object{
        val instance by lazy {
            Room.databaseBuilder(
                SudokuApp.INSTANCE, AppDatabase::class.java, "sudoku.db"
            ).build()
        }
    }
}
