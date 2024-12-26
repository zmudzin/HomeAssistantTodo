package com.example.homeassistanttodo.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val items by viewModel.todoItems.collectAsState()
    val activeItems = items.filter { it.status != "completed" }
    val completedItems = items.filter { it.status == "completed" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = activeItems,
            key = { it.uid }
        ) { item ->
            TodoItemView(
                item = item,
                onDragHandle = { },
                onToggleStatus = { status -> viewModel.updateItemStatus(item.uid, status) },
                onEdit = { },
                onDelete = { viewModel.deleteItem(item.uid) }
            )
        }

        if (completedItems.isNotEmpty()) {
            item {
                Text(
                    "Ukończone",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                )
            }

            items(
                items = completedItems,
                key = { it.uid }
            ) { item ->
                TodoItemView(
                    item = item,
                    onDragHandle = { },
                    onToggleStatus = { status -> viewModel.updateItemStatus(item.uid, status) },
                    onEdit = { },
                    onDelete = { viewModel.deleteItem(item.uid) },
                    modifier = Modifier.alpha(0.6f)
                )
            }
        }
    }
}