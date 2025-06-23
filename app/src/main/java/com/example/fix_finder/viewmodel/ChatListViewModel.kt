package com.example.fix_finder.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.fix_finder.data.model.ChatSummary
import com.example.fix_finder.data.repository.ChatRepository

class ChatListViewModel(private val currentUserId: String) : ViewModel() {
    private val repository = ChatRepository()
    private val _chatSummaries = mutableStateListOf<ChatSummary>()
    val chatSummaries: List<ChatSummary> = _chatSummaries

    init {
        loadChats()
    }

    private fun loadChats() {
        repository.getUserChatSummaries(currentUserId) { summaries ->
            _chatSummaries.clear()
            _chatSummaries.addAll(summaries)
        }
    }
}
