package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.ChatUiState
import com.spbalkan.simplychat.chat.models.Message
import com.spbalkan.simplychat.chat.viewmodels.ChatViewModel
import com.spbalkan.simplychat.chat.viewmodels.NewChatViewModel
import com.spbalkan.simplychat.ui.CustomInputTextView

// New conversation
@Composable
fun NewChatScreen(
    viewModel: NewChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatScreenContent(
        state = state,
        username = viewModel.args.username
    )
}

// Already existing conversation
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ChatScreenContent(
        state = state,
        username = viewModel.args.username
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    state: ChatUiState,
    username: String
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = username)
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
                    inputText = state.inputText,
                    onInputChanged = {

                    }
                )

                IconButton(
                    onClick = {

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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(
                items = state.messagesList,
                key = { _, message -> message.id }
            ) { index, message ->
                MessageItem(message)
            }
        }
    }
}

@Composable
fun MessageItem(
    message: Message
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(all = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
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


}

@Composable
fun MyMessage(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier
            .padding(12.dp),
        text = text,
        fontSize = TextUnit(16f, TextUnitType.Sp)
    )
}

@Composable
fun ReceivedMessage(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier
            .background(
                color = Color.Blue,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        text = text,
        fontSize = TextUnit(16f, TextUnitType.Sp)
    )
}