package com.spbalkan.simplychat.chat.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.CheckMessagesWorker
import com.spbalkan.simplychat.chat.viewmodels.ConversationsListViewModel
import com.spbalkan.simplychat.utils.Constants
import java.util.concurrent.TimeUnit

@Composable
fun ConversationsListScreen(
    viewModel: ConversationsListViewModel = hiltViewModel(),
    onChatSelected: (chatId: String, newChatUsername: String) -> Unit,
    onStartNewChat: (chatId: String, newChatUsername: String) -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val hasRequested by viewModel.hasRequestedPermission.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) setupPeriodicMessageCheck(context)
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getUserConversations()
    }

    LaunchedEffect(hasRequested) {
        val isLoadedAndNotRequested = hasRequested == false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !isLoadedAndNotRequested
        ) {
            viewModel.markPermissionAsRequested()
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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
                    event.chatId,
                    event.newChatUsername
                )
            }
        }
    }
}

private fun setupPeriodicMessageCheck(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val messageCheckRequest = PeriodicWorkRequestBuilder<CheckMessagesWorker>(
        repeatInterval = 15,
        repeatIntervalTimeUnit = TimeUnit.MINUTES
    )
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        Constants.WM_CHECK_MESSAGES,
        ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, // Keeps the active schedule across app restarts
        messageCheckRequest
    )
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
                    lastMessage = conversation.lastMessageText,
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

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp),
                text = "v. ${LocalContext.current.packageManager.getPackageInfo(LocalContext.current.packageName, 0).versionName ?: ""}",
                fontSize = TextUnit(12f, TextUnitType.Sp)
            )
        }
    }
}

@Composable
fun ConversationItem(
    username: String,
    lastMessage: String?,
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
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)

    ) {
        Text(
            text = username,
            fontSize = TextUnit(24f, TextUnitType.Sp)
        )
        lastMessage?.let { msg ->
            Text(modifier = Modifier
                .padding(start = 16.dp),
                text = msg,
                maxLines = 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )
        }
    }
}