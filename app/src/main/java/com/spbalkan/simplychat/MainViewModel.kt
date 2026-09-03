package com.spbalkan.simplychat

import androidx.lifecycle.ViewModel
import com.spbalkan.simplychat.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<Screen>(Screen.Welcome)
    val startDestination: StateFlow<Screen> = _startDestination.asStateFlow()

    init {
        _startDestination.value = if (authRepository.isUserSignedIn()) Screen.ConversationsList else Screen.Welcome
    }

}