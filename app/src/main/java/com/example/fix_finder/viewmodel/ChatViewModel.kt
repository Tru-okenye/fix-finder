package com.example.fix_finder.viewmodel


import androidx.lifecycle.ViewModel
import com.example.fix_finder.data.model.Message
import com.example.fix_finder.data.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class ChatViewModel(
    private val currentUserId: String,
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages = _messages.asStateFlow()

    var currentChatId: String? = null
        private set

    fun initChat(clientId: String, providerId: String) {
        repository.getOrCreateChat(clientId, providerId) { chatId ->
            currentChatId = chatId
            observeMessages(chatId)
        }
    }
    private var isChatVisible = false

    fun resetUnseenCount() {
        currentChatId?.let { id ->
            firestore.collection("chats").document(id)
                .update("${currentUserId}_unseenCount", 0)
        }
    }

    fun setChatVisibility(visible: Boolean) {
        isChatVisible = visible
        if (visible && currentChatId != null) {
            // Re-check and mark messages as seen when chat becomes visible
            markUnseenMessagesAsSeen(currentChatId!!, _messages.value)
        }
    }

    private fun observeMessages(chatId: String) {
        repository.listenForMessages(chatId, currentUserId) { newMessages ->
            _messages.value = newMessages
            if (isChatVisible) {
                markUnseenMessagesAsSeen(chatId, newMessages)
            }
        }
    }


//    private fun markUnseenMessagesAsSeen(chatId: String, messages: List<Message>) {
//        messages.filter { !it.seen && it.senderId != currentUserId }.forEach { msg ->
//        repository.markMessageAsSeen(chatId, msg.id)
//        }
//
//    }

    private fun markUnseenMessagesAsSeen(chatId: String, messages: List<Message>) {
        // 1) Mark individual messages as seen
        messages.filter { !it.seen && it.senderId != currentUserId }
            .forEach { msg -> repository.markMessageAsSeen(chatId, msg.id) }

        // 2) Always reset THIS user’s unseen count to 0 on chat open
        firestore.collection("chats").document(chatId)
            .update("${currentUserId}_unseenCount", 0)
    }



    fun markMessageAsSeenIfNeeded(message: Message) {
        if (!message.seen && message.senderId != currentUserId) {
            currentChatId?.let { chatId ->
                repository.markMessageAsSeen(chatId, message.id)
            }
        }
    }

    fun sendMessage(senderId: String, recipientId: String, text: String) {
        currentChatId?.let {
            repository.sendMessage(it, senderId, recipientId, text)
        }
    }
}
