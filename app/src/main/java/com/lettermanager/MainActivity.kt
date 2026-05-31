package com.lettermanager

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.lettermanager.presentation.navigation.AppNavigation
import com.lettermanager.presentation.theme.LetterManagerTheme
import com.lettermanager.presentation.ui.auth.AuthScreen
import com.lettermanager.presentation.ui.auth.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LetterManagerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainContent(authViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Could check inactivity lock here
    }
}

@Composable
fun MainContent(authViewModel: AuthViewModel) {
    var isAuthenticated by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        AuthScreen(
            onAuthSuccess = { isAuthenticated = true },
            viewModel = authViewModel
        )
    } else {
        AppNavigation()
    }
}
