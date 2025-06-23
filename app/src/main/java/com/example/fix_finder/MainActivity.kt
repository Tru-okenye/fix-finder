package com.example.fix_finder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.example.fix_finder.di.DataStoreManager
import com.example.fix_finder.ui.theme.FixFinderApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        dataStoreManager = DataStoreManager(applicationContext)

        setContent {
            val isDarkTheme by dataStoreManager.darkModeFlow.collectAsState(initial = false)

            FixFinderApp(
                isDarkTheme = isDarkTheme,
                onToggleTheme = {
                    lifecycleScope.launch {
                        dataStoreManager.setDarkMode(!isDarkTheme)
                    }
                }
            )
        }
    }
}
