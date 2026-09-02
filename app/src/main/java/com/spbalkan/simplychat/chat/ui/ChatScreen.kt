package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.models.Message
import com.spbalkan.simplychat.chat.viewmodels.ChatViewModel
import com.spbalkan.simplychat.ui.CustomInputTextView
import com.spbalkan.simplychat.ui.theme.Blue40
import com.spbalkan.simplychat.ui.theme.BlueGrey80

// TODO: to add:
// - dynamic vertical separation between messages of different senders

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.inputState.collectAsStateWithLifecycle()
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()

    ChatScreenContent(
        inputState = state,
        messagesState = messagesState,
        onAction = viewModel::onAction,
        username = viewModel.args.otherUsername,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    inputState: String,
    messagesState: ChatViewModel.MessagesState,
    onAction: (ChatViewModel.ActionIntent) -> Unit,
    username: String,
    onNavigateBack: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = username)
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back_24),
                            contentDescription = null
                        )
                    }
                }

            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CustomInputTextView(
                    modifier = Modifier
                        .weight(1f),
                    labelText = null,
                    inputText = inputState,
                    onInputChanged = {
                        onAction(ChatViewModel.ActionIntent.OnInputChange(it))
                    }
                )

                IconButton(
                    onClick = {
                        onAction(ChatViewModel.ActionIntent.OnSendMessage)
                        keyboardController?.hide()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send_24),
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            reverseLayout = true
        ) {
            itemsIndexed(
                items = messagesState.messagesList,
                key = { _, message -> message.id }
            ) { _, message ->
                MessageItem(message)
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        if (message.isReceivedMessage) {
            ReceivedMessage(
                Modifier.align(Alignment.CenterStart),
                message.text
            )
        } else {
            MyMessage(
                Modifier.align(Alignment.CenterEnd),
                message.text
            )
        }
    }
}

@Composable
fun MyMessage(
    modifier: Modifier = Modifier,
    text: String
) {
    Box(
        modifier = modifier
            .wrapContentWidth()
            .padding(
                start = 48.dp,
                end = 12.dp
            )
    ) {
        Text(
            modifier = modifier
                .background(
                    color = BlueGrey80,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            text = text,
            color = Color.Black,
            textAlign = TextAlign.End,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )
    }
}

@Composable
fun ReceivedMessage(
    modifier: Modifier = Modifier,
    text: String
) {
    Box(
        modifier = modifier
            .wrapContentWidth()
            .padding(
                start = 12.dp,
                end = 48.dp
            )
    ) {
        Text(
            modifier = modifier
                .background(
                    color = Blue40,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            text = text,
            color = Color.White,
            fontSize = TextUnit(16f, TextUnitType.Sp)
        )
    }

}