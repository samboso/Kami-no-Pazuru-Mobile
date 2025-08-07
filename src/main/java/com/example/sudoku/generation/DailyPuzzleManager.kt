package com.example.sudoku.generation
import com.example.sudoku.data.AppDatabase
import com.example.sudoku.data.PuzzleEntity
import java.time.LocalDate

object DailyPuzzleManager {
    private fun todayId() = LocalDate.now().run { year*10000L+monthValue*100+dayOfMonth }

    suspend fun getToday(): PuzzleEntity {
        val dao = AppDatabase.instance.dao()
        val id  = todayId()
        dao.get(id)?.let { return it }

        val (p,s,_) = SudokuGenerator.generate(1)
        val csvP = p.joinToString("\n"){it.joinToString(",")}
        val csvS = s.joinToString("\n"){it.joinToString(",")}
        val entity = PuzzleEntity(id,"classic",1,csvP,csvS)
        dao.save(entity)
        return entity
    }
}
