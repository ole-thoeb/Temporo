package com.eloem.temporo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import com.eloem.temporo.R
import com.eloem.temporo.util.AnimatedIconFab
import com.eloem.temporo.util.createNotificationChannel
import com.eloem.temporo.util.lazyView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_host.*
import org.jetbrains.anko.defaultSharedPreferences

class HostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        setSupportActionBar(bottomAppBar)
        createNotificationChannel()

        mainFab.icon = AnimatedIconFab.Icon.ADD
        
        val themeMode = when(val theme = defaultSharedPreferences.getString("settingsTheme", "2")) {
            "0" -> AppCompatDelegate.MODE_NIGHT_NO
            "1" -> AppCompatDelegate.MODE_NIGHT_YES
            "2" -> AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            else -> throw Error("Unknown theme option: $theme")
        }
        AppCompatDelegate.setDefaultNightMode(themeMode)
    }

    val mainFab: AnimatedIconFab by lazyView(R.id.mainFab)

    override fun onSupportNavigateUp() =
        findNavController(R.id.navHostFragment).navigateUp()
}
