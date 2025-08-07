package com.example.sudoku

import android.app.Application

/**
 * Application singleton usado por Room y DataStore para obtener
 * un contexto seguro.  Referenciado como `SudokuApp.INSTANCE`.
 */
class SudokuApp : Application() {
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
    }
    companion object {
        lateinit var INSTANCE: SudokuApp
            private set
    }
}
