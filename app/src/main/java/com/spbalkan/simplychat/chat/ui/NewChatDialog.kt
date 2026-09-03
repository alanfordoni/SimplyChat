package com.spbalkan.simplychat.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbalkan.simplychat.R
import com.spbalkan.simplychat.chat.viewmodels.NewChatDialogViewModel
import com.spbalkan.simplychat.chat.viewmodels.NewChatDialogViewModel.ActionIntent
import com.spbalkan.simplychat.ui.CustomFilledButton
import com.spbalkan.simplychat.ui.CustomInputTextView

@Composable
fun NewChatDialog(
    viewModel: NewChatDialogViewModel = hiltViewModel(),
    onOpenNewChat: (newChatUsername: String, newChatUid: String) -> Unit,
    onDismiss: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NewChatDialogContent(
        state = state,
        onAction = viewModel::onAction,
        onDismiss = onDismiss
    )

    LaunchedEffect(Unit) {
        viewModel.eventDoesUserExist.collect { event ->
            if (event is NewChatDialogViewModel.NewChatEvents.EventUserFound) {
                onOpenNewChat(
                    event.username,
                    event.uid
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialogContent(
    state: NewChatDialogViewModel.NewChatState,
    onAction: (ActionIntent) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSearchTextDirty by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(5.dp)
                        .background(
                            color = Color.DarkGray,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CustomInputTextView(
                labelText = stringResource(R.string.label_search_user),
                inputText = state.searchText,
                errorText = stringResource(R.string.error_username_search_too_short),
                isError = state.isSearchTextError,
                isDirty = isSearchTextDirty,
                onInputChanged = {
                    onAction(ActionIntent.OnSearchTextChange(it))
                    if (!isSearchTextDirty) isSearchTextDirty = true
                }
            )

            CustomFilledButton(
                textRes = R.string.button_open_chat,
                onClick = {
                    onAction(ActionIntent.OnCheckIfUserExist)
                }
            )
        }

    }
}
