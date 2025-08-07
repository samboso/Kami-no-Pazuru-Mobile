package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
// We avoid using ViewBinding here to reduce build complexity.  Instead we
// manually inflate the layout and find views by their IDs.

/**
 * Entry point of the application.  Presents the user with a menu of
 * available Sudoku variants and launches the corresponding puzzle
 * activity when selected.  Variants currently supported are
 * classic, fractal and cross.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout and find the buttons explicitly.  Using
        // viewBinding for such a simple screen is overkill and can lead
        // to build issues if the Gradle plugin is out of date.
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnClassic).setOnClickListener {
            startSudokuActivity("classic")
        }
        findViewById<Button>(R.id.btnFractal).setOnClickListener {
            startSudokuActivity("fractal")
        }
        findViewById<Button>(R.id.btnCross).setOnClickListener {
            startSudokuActivity("cross")
        }
        findViewById<Button>(R.id.btnThermo).setOnClickListener {
            startSudokuActivity("thermo")
        }
        findViewById<Button>(R.id.btnKiller).setOnClickListener {
            startSudokuActivity("killer")
        }
    }

    /**
     * Launch [SudokuActivity] with the given Sudoku [variant].  You
     * could also pass along a chosen difficulty here if desired.
     */
    private fun startSudokuActivity(variant: String) {
        val intent = Intent(this, SudokuActivity::class.java)
        intent.putExtra("variant", variant)
        // Always start with the easiest difficulty (0) for now.  This can
        // be exposed to the user via the UI later.
        intent.putExtra("difficulty", 0)
        startActivity(intent)
    }
}