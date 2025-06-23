package com.example.fix_finder.ui.components
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.fix_finder.viewmodel.ChatListViewModel

import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    currentUserId: String,
    isClient: Boolean,
    onBack: () -> Unit,
    navController: NavHostController
) {
    val viewModel = remember { ChatListViewModel(currentUserId) }
    val chatSummaries = viewModel.chatSummaries

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues -> // ✅ Fix here


            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )  {


        Text("Messages", style = MaterialTheme.typography.headlineMedium)

        LazyColumn {
            items(chatSummaries) { chat ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("recipientId", chat.otherUserId)
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("currentUserId", currentUserId)
                            navController.currentBackStackEntry
                                ?.savedStateHandle
                                ?.set("isClient", isClient)
                            navController.navigate("chat")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InitialCircle(initial = chat.otherUserName.take(1).uppercase())
                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text(chat.otherUserName, fontWeight = FontWeight.Bold)
                        Text(chat.lastMessage, maxLines = 1)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatTimestamp(chat.timestamp), fontSize = 11.sp)
                        if (chat.unseenCount > 0) {
                            Badge { Text("${chat.unseenCount}") }
                        }
                    }
                }
            }
        }
            }
        }
    )
}
