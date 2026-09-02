package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.viewmodels.ConversationsListViewModel

@Composable
fun ConversationsListScreen(
    viewModel: ConversationsListViewModel = viewModel(),
    onChatSelected: (chatId: String, newChatUsername: String) -> Unit,
    onStartNewChat: (chatId: String, newChatUsername: String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ConversationsListScreenContent(
        state = state,
        onAction = viewModel::onAction,
        onOpenSelectedChat = { chatId, newChatUsername ->
            onChatSelected(
                chatId,
                newChatUsername
            )
        },
        onLogout = {
            viewModel.onAction(ConversationsListViewModel.ActionIntent.OnLogout)
            onLogout()
        }
    )

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            if (event is ConversationsListViewModel.ConversationsEvents.EventOpenNewChat) {
                onStartNewChat(
                    event.newChatUsername,
                    event.newChatUsername
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreenContent(
    state: ConversationsListViewModel.ConversationsState,
    onAction:(ConversationsListViewModel.ActionIntent) -> Unit,
    onOpenSelectedChat: (String, String) -> Unit,
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
                title = {
                    Text(text = stringResource(R.string.label_chats))
                },
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
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
            ,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = state.conversations,
                key = { _, chat -> chat.id }
            ) { index, conversation ->
                ConversationItem(
                    username = conversation.otherUser.username,
                    onClick = { onOpenSelectedChat(conversation.id, conversation.otherUser.username) }
                )

                if (index < state.conversations.lastIndex) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
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
                onOpenNewChat = { newChatUsername, newChatUid ->
                    onAction(ConversationsListViewModel.ActionIntent.OnOpenNewChat(
                        newChatUsername = newChatUsername,
                        newChatUid = newChatUid
                    ))
                },
                onDismiss = {
                    onAction(ConversationsListViewModel.ActionIntent.OnNewChatDismiss)
                }
            )
        }
    }
}

@Composable
fun ConversationItem(
    username: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable(
                enabled = true,
                onClick = {
                    onClick()
                }
            )
            .padding(all = 16.dp)
    ) {
        Text(
            text = username,
            fontSize = TextUnit(24f, TextUnitType.Sp)
        )
    }
}