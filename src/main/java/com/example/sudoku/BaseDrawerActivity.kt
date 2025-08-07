package com.example.sudoku

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

abstract class BaseDrawerActivity :
    AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    protected lateinit var drawer: DrawerLayout
    private   lateinit var nav   : NavigationView

    /** Llamar DESPUÉS de setContentView(layout) */
    protected fun initDrawer(toolbarId: Int) {
        val tb: Toolbar = findViewById(toolbarId)
        setSupportActionBar(tb)

        drawer = findViewById(R.id.drawer_layout)
        nav    = findViewById(R.id.nav_view)
        nav.setNavigationItemSelectedListener(this)

        androidx.appcompat.app.ActionBarDrawerToggle(
            this, drawer, tb,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        ).apply {
            drawer.addDrawerListener(this)
            syncState()
        }
    }

    /* -------- navegación global -------- */
    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        drawer.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_home     -> goto(MainActivity   ::class.java)
            R.id.nav_new      -> goto(SudokuActivity::class.java)
            R.id.nav_profile  -> goto(ProfileActivity::class.java)
            R.id.nav_stats    -> goto(StatsActivity  ::class.java)
            R.id.nav_settings -> goto(SettingsActivity::class.java)
            R.id.nav_about    -> goto(AboutActivity ::class.java)
        }
        return true
    }
    private fun goto(cls: Class<*>) {
        if (this::class.java == cls) return
        startActivity(Intent(this, cls))
        overridePendingTransition(R.anim.fade_through_enter, R.anim.fade_through_exit)
    }

    override fun onBackPressed() {
        if (::drawer.isInitialized && drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else {
            super.onBackPressed()
            overridePendingTransition(R.anim.fade_through_enter, R.anim.fade_through_exit)
        }
    }
}
