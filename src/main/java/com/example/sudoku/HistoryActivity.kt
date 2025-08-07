package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sudoku.data.HistoryRepository
import kotlinx.coroutines.launch

class HistoryActivity : BaseDrawerActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContentView(R.layout.activity_history)
        initDrawer(R.id.toolbar_history)

        val rv = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rv_history)
        rv.layoutManager = LinearLayoutManager(this)
        val adapter = HistoryAdapter { g ->
            startActivity(
                Intent(this, SudokuActivity::class.java).apply {
                    putExtra("resume_puzzle", g.puzzle)
                    putExtra("variant", g.variant)
                    putExtra("difficulty", g.difficulty)
                }
            )
        }
        rv.adapter = adapter

        lifecycleScope.launch {
            HistoryRepository.history(this@HistoryActivity).collect { adapter.submitList(it) }
        }
    }

    /* menú opcional para borrar */
    override fun onCreateOptionsMenu(m: android.view.Menu): Boolean {
        m.add("Limpiar historial"); return true
    }
    override fun onOptionsItemSelected(i: android.view.MenuItem): Boolean {
        if (i.title == "Limpiar historial") {
            lifecycleScope.launch { HistoryRepository.clear(this@HistoryActivity) }
            return true
        }
        return super.onOptionsItemSelected(i)
    }
}
