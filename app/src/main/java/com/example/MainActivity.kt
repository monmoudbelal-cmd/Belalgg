package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ImageViewerScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PdfViewerScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.QuizTakerScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.EduVaultTheme
import com.example.ui.viewmodel.EduViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduVaultTheme {
                // Arabic RTL Layout Provider
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val viewModel: EduViewModel = viewModel()
                    val currentScreen by viewModel.screen.collectAsStateWithLifecycle()
                    val snackbarHostState = remember { SnackbarHostState() }
                    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

                    LaunchedEffect(snackbarMsg) {
                        snackbarMsg?.let {
                            snackbarHostState.showSnackbar(it)
                            viewModel.clearSnackbar()
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                    ) { innerPadding ->
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition",
                            modifier = Modifier.padding(innerPadding)
                        ) { screen ->
                            when (screen) {
                                is Screen.Login -> LoginScreen(viewModel = viewModel)
                                is Screen.StudentDashboard -> StudentDashboardScreen(viewModel = viewModel)
                                is Screen.AdminDashboard -> AdminDashboardScreen(viewModel = viewModel)
                                is Screen.PdfViewer -> PdfViewerScreen(fileItem = screen.fileItem, viewModel = viewModel)
                                is Screen.VideoPlayer -> VideoPlayerScreen(fileItem = screen.fileItem, viewModel = viewModel)
                                is Screen.ImageViewer -> ImageViewerScreen(fileItem = screen.fileItem, viewModel = viewModel)
                                is Screen.QuizTaker -> QuizTakerScreen(quiz = screen.quiz, viewModel = viewModel)
                                is Screen.QuizResult -> QuizResultScreen(
                                    result = screen.result,
                                    questions = screen.questions,
                                    userAnswers = screen.userAnswers,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
