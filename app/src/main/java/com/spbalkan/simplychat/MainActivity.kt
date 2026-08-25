package com.spbalkan.simplychat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.spbalkan.simplychat.auth.ui.LoginScreen
import com.spbalkan.simplychat.auth.ui.RegisterScreen
import com.spbalkan.simplychat.auth.ui.WelcomeScreen
import com.spbalkan.simplychat.chat.ui.ConversationsListScreen
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var startDestination: Screen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = Firebase.auth
        if (savedInstanceState == null) checkAuthState()

        setContent {
            SimplyChatTheme {
                AppNavHost(startDestination = startDestination)
            }
        }
    }

    private fun checkAuthState() {
        val currentUser = auth.currentUser
        startDestination = if (currentUser != null) {
            Screen.ConversationsList
        } else {
            Screen.Welcome
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Welcome
) {
    NavHost(
        modifier = Modifier.systemBarsPadding(),
        navController = navController,
        startDestination = startDestination,

    ) {
        composable<Screen.Welcome> {
            WelcomeScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register) },
                onNavigateToLogin = { navController.navigate(Screen.Login) }
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.ConversationsList) {
                        // Clear the entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Register> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ConversationsList) {
                        // Clear the entire back stack
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register) {
                        popUpTo<Screen.Login> { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.ConversationsList> {
            ConversationsListScreen(
                onChatSelected = { chatId ->
                    navController.navigate(Screen.Chat(chatId = chatId))
                },
                onLogout = {
                    navController.navigate(Screen.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        /*
            composable<Screen.Chat> { backStackEntry ->
                val chatRoute: Screen.Chat = backStackEntry.toRoute()

                ChatScreen(
                    chatId = chatRoute.chatId,
                    onNavigateBack = { navController.popBackStack() }
                )
        }*/
    }


}

sealed interface Screen {
    @Serializable
    data object Welcome : Screen

    @Serializable
    data object Login : Screen

    @Serializable
    data object Register : Screen

    @Serializable
    data object ConversationsList : Screen

    @Serializable
    data class Chat(val chatId: String) : Screen
}
