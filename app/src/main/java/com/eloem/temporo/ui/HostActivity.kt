package com.eloem.temporo.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.eloem.temporo.R
import com.eloem.temporo.util.AnimatedIconFab
import com.eloem.temporo.util.createNotificationChannel
import com.eloem.temporo.util.lazyView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_host.*

class HostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        setSupportActionBar(bottomAppBar)
        createNotificationChannel()

        mainFab.icon = AnimatedIconFab.Icon.ADD
    }

    val mainFab: AnimatedIconFab by lazyView(R.id.mainFab)

    override fun onSupportNavigateUp() =
        findNavController(R.id.navHostFragment).navigateUp()
}
