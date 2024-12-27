package com.example.homeassistanttodo.ui.todo

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val items by viewModel.todoItems.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingItem by viewModel.editingItem.collectAsState()
    val activeItems = items.filter { it.status != "completed" }
    val completedItems = items.filter { it.status == "completed" }

    // Dialogi
    if (showAddDialog) {
        TodoDialog(
            title = "Nowe zadanie",
            onDismiss = { viewModel.hideAddDialog() },
            onConfirm = { summary ->
                viewModel.createTodoItem(summary)
                viewModel.hideAddDialog()
            }
        )
    }

    editingItem?.let { item ->
        TodoDialog(
            title = "Edytuj zadanie",
            initialValue = item.summary,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { newSummary ->
                viewModel.updateTodoItemSummary(item.uid, newSummary)
                viewModel.hideEditDialog()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp, 
                top = 8.dp, 
                end = 8.dp, 
                bottom = 80.dp // Dodajemy padding na dole dla FABów
            ),
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
                    onEdit = { viewModel.showEditDialog(item) },
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
                        onEdit = { viewModel.showEditDialog(item) },
                        onDelete = { viewModel.deleteItem(item.uid) },
                        modifier = Modifier.alpha(0.6f)
                    )
                }
            }
        }

        // FABy
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FAB do usuwania ukończonych (widoczny tylko gdy są ukończone zadania)
            AnimatedVisibility(
                visible = completedItems.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.deleteCompletedItems() },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Usuń ukończone zadania",
                    )
                }
            }

            // Główny FAB
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Dodaj zadanie",
                )
            }
        }
    }
}