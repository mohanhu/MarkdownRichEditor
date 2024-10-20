package com.example.markdownapp.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RIchTextViewModel:ViewModel() {

    private val _uiState = MutableStateFlow<RichData>(RichData())
    val uiState = _uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RichData())


    fun updateCurrent (index:Int) = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update { it.copy(currentIndex = index) }
    }

}

data class RichData(
    val currentIndex : Int = -3
)