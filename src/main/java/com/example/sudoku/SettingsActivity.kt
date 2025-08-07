package com.example.sudoku

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : BaseDrawerActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        initDrawer(R.id.toolbar_settings)

        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, SettingsFragment())
            .commit()
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(s: Bundle?, root: String?) {
            setPreferencesFromResource(R.xml.prefs_root, root)

            findPreference<androidx.preference.SwitchPreferenceCompat>("pref_left_handed")
                ?.setOnPreferenceChangeListener { _, newVal ->
                    (activity as? SudokuActivity)?.applyLeftHanded(newVal as Boolean); true
                }
            findPreference<androidx.preference.SwitchPreferenceCompat>("pref_hc")
                ?.setOnPreferenceChangeListener { _, _ ->
                    activity?.recreate(); true
                }
        }
    }
}
