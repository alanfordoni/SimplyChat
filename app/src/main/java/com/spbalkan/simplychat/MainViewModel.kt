package com.spbalkan.simplychat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen>(Screen.Welcome)
    val startDestination: StateFlow<Screen> = _startDestination.asStateFlow()

    init {
        _startDestination.value = if (ServiceLocator.authRepository.isUserSignedIn()) Screen.ConversationsList else Screen.Welcome
    }

}