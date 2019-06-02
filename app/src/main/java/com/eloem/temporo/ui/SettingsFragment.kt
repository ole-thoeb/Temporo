package com.eloem.temporo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.eloem.temporo.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey)

        findPreference<ListPreference>("settingsTheme")?.setOnPreferenceChangeListener { preference, newValue ->
            val themeMode = when(newValue) {
                "0" -> AppCompatDelegate.MODE_NIGHT_NO
                "1" -> AppCompatDelegate.MODE_NIGHT_YES
                "2" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                else -> throw Error("Unknown theme option: $newValue")
            }

            AppCompatDelegate.setDefaultNightMode(themeMode)

            true
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        (activity as HostActivity?)?.apply {
            mainFab.hide()

            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
            }
        }
    }
}