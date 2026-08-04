package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.EbookViewModel
import com.example.ui.EbookViewModelFactory
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReadScreen
import com.example.ui.SettingsViewModel
import com.example.ui.SettingsViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as MyApplication
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(app.settingsManager)
            )
            val themeIndex by settingsViewModel.themeIndex.collectAsState()

            MyApplicationTheme(themeIndex = themeIndex) {
                val ebookViewModel: EbookViewModel = viewModel(
                    factory = EbookViewModelFactory(app.repository)
                )
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                navController = navController,
                                ebookViewModel = ebookViewModel,
                                settingsViewModel = settingsViewModel
                            )
                        }
                        composable("read/{bookId}?scrollTo={scrollTo}") { backStackEntry ->
                            val bookId = backStackEntry.arguments?.getString("bookId")?.toIntOrNull() ?: 0
                            val scrollTo = backStackEntry.arguments?.getString("scrollTo")?.toIntOrNull()
                            ReadScreen(
                                bookId = bookId,
                                ebookViewModel = ebookViewModel,
                                settingsViewModel = settingsViewModel,
                                scrollTo = scrollTo,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
