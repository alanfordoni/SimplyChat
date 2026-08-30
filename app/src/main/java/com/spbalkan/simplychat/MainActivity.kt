package com.spbalkan.simplychat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.spbalkan.simplychat.auth.ui.LoginScreen
import com.spbalkan.simplychat.auth.ui.RegisterScreen
import com.spbalkan.simplychat.auth.ui.WelcomeScreen
import com.spbalkan.simplychat.chat.ui.ChatScreen
import com.spbalkan.simplychat.chat.ui.NewChatScreen
import com.spbalkan.simplychat.chat.ui.ConversationsListScreen
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme
import kotlinx.serialization.Serializable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SimplyChatTheme {
                AppNavHost()
            }
        }
    }
}

@Composable
fun AppNavHost() {

    val navController: NavHostController = rememberNavController()
    val viewmodel: MainViewModel = viewModel()
    val startDestination by viewmodel.startDestination.collectAsStateWithLifecycle()

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
                onChatSelected = { chatId, username, uid ->
                    navController.navigate(Screen.Chat(
                        chatId = chatId,
                        username = username,
                        uid = uid
                    ))
                },
                onStartNewChat = { username, uid ->
                    navController.navigate(Screen.NewChat(
                        username = username,
                        uid = uid
                    ))
                },
                onLogout = {
                    navController.navigate(Screen.Welcome) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<Screen.NewChat> { backStackEntry ->
            NewChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Chat> {
            ChatScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
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
    data class NewChat(val username: String, val uid: String) : Screen

    @Serializable
    data class Chat(val chatId: String, val username: String, val uid: String) : Screen
}

