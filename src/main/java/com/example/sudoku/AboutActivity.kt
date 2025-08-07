package com.example.sudoku

import android.os.Bundle

class AboutActivity : BaseDrawerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        initDrawer(R.id.toolbar_about)
    }
}
