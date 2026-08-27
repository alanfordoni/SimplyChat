package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.viewmodels.ConversationsListViewModel

@Composable
fun ConversationsListScreen(
    viewModel: ConversationsListViewModel = viewModel(),
    onChatSelected: (chatId: String) -> Unit,
    onStartNewChat: (username: String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ConversationsListScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onLogout = {
            viewModel.onAction(ConversationsListViewModel.ActionIntent.OnLogout)
            onLogout()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is ConversationsListViewModel.ConversationsEvents.OpenNewChat) {
                onStartNewChat(event.username)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreenContent(
    state: ConversationsListViewModel.ConversationsState,
    onAction:(ConversationsListViewModel.ActionIntent) -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAction(ConversationsListViewModel.ActionIntent.OnSearchNewChat)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_new_chat_24),
                    contentDescription = null
                )
            }
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                actions = {
                    IconButton(
                        onClick = {
                            onLogout()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_logout_24),
                            contentDescription = null
                        )
                    }
                }
            )

        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues)
        ) {
            // todo
        }

        FloatingActionButton(
            onClick = {
                onAction(ConversationsListViewModel.ActionIntent.OnSearchNewChat)
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_new_chat_24),
                contentDescription = null
            )
        }

        if (state.showNewChatDialog) {
            NewChatDialog(
                onOpenNewChat = {

                },
                onDismiss = {
                    onAction(ConversationsListViewModel.ActionIntent.OnNewChatDismiss)
                }
            )
        }
    }
}