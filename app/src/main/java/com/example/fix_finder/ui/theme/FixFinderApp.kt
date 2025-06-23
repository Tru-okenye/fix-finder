// FixFinderApp.kt
package com.example.fix_finder.ui.theme

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.fix_finder.ui.navigation.AppNavGraph
import com.example.fix_finder.ui.screens.auth.AuthViewModel

@Composable
fun FixFinderApp(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()
    ) {
        val navController = rememberNavController()
        val authViewModel: AuthViewModel = viewModel()
        AppNavGraph(
            navController = navController,
            isDarkTheme = isDarkTheme,
            onToggleTheme = onToggleTheme,
            authViewModel = authViewModel
        )
    }
}
