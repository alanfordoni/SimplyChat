package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.viewmodels.ChatViewModel
import com.spbalkan.simplychat.ui.CustomInputTextView
import com.spbalkan.simplychat.ui.theme.Blue40
import com.spbalkan.simplychat.ui.theme.BlueGrey40
import com.spbalkan.simplychat.ui.theme.BlueGrey80
import com.spbalkan.simplychat.ui.theme.IceBlue80
import com.spbalkan.simplychat.ui.theme.SimplyChatTheme

// TODO: to add:
// - dynamic vertical separation between messages of different senders

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
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
    val listState = rememberLazyListState()

    LaunchedEffect(messagesState.messagesList.size) {
        listState.animateScrollToItem(0)
    }

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
        Image(
            painterResource(R.drawable.bg_chat),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            alpha = 0.2f,
            modifier = Modifier
                .fillMaxSize()
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            reverseLayout = true
        ) {
            itemsIndexed(
                items = messagesState.messagesList,
                key = { _, message -> message.id }
            ) { index, message ->

                if (message is ChatViewModel.ChatWrapperItem.DateHeader) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 12.dp),
                        textAlign = TextAlign.Center,
                        text = message.dateText)
                } else {
                    val message = message as ChatViewModel.ChatWrapperItem.MessageBubble
                    var addSpace = false

                    if (index - 1 >= 0) {
                        val nextMessage = messagesState.messagesList[index - 1] as? ChatViewModel.ChatWrapperItem.MessageBubble
                        if (nextMessage != null && message.isReceivedMessage != nextMessage.isReceivedMessage) {
                            addSpace = true
                        }
                    }

                    MessageItem(message, addSpace)
                }
            }
        }
    }
}

@Composable
fun MessageItem(
    message: ChatViewModel.ChatWrapperItem.MessageBubble,
    addSpace: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.align(if (message.isReceivedMessage) Alignment.CenterStart else Alignment.CenterEnd)
        ) {
            if (message.isReceivedMessage) {
                ReceivedMessage(message)
            } else {
                MyMessage(message)
            }

            if (addSpace) Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MyMessage(
    message: ChatViewModel.ChatWrapperItem.MessageBubble
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(
                start = 48.dp
            )
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = BlueGrey80,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                modifier = Modifier
                    .weight(1f, fill = false),
                text = message.text,
                color = Color.Black,
                textAlign = TextAlign.End,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier
                    .align(Alignment.Bottom),
                maxLines = 1,
                text = message.time,
                color = BlueGrey40,
                fontSize = TextUnit(12f, TextUnitType.Sp)
            )
        }
    }
}

@Composable
fun ReceivedMessage(
    message: ChatViewModel.ChatWrapperItem.MessageBubble
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .padding(
                end = 48.dp
            )
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = Blue40,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                modifier = Modifier
                    .weight(1f, fill = false),
                text = message.text,
                color = Color.White,
                fontSize = TextUnit(16f, TextUnitType.Sp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier
                    .align(Alignment.Bottom),
                text = message.time,
                color = IceBlue80,
                fontSize = TextUnit(12f, TextUnitType.Sp)
            )
        }



    }

}

@Preview
@Composable
fun ChatScreenContentPreview() {
    SimplyChatTheme {
        val messagesState = ChatViewModel.MessagesState(
            messagesList = listOf(
                ChatViewModel.ChatWrapperItem.MessageBubble(
                    id = "sdsd",
                    text = "Ich habe viel Sport gemacht, denn ich hatte stressig Woche",
                    isReceivedMessage = false,
                    time = "09:32"
                ),
                ChatViewModel.ChatWrapperItem.DateHeader(
                    id = "1",
                    dateText = "22 Feb 2026"
                )
            )
        )

        ChatScreenContent(
            inputState = "",
            messagesState = messagesState,
            onAction = {},
            username = "Chatty 55",
            onNavigateBack = {}
        )
    }
}