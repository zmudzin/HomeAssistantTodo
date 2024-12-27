package com.example.homeassistanttodo.ui.todo

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.homeassistanttodo.data.model.TodoItem

@Composable
fun TodoItemView(
    item: TodoItem,
    onDragHandle: () -> Unit,
    onToggleStatus: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isCompleted = item.status == "completed"
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        color = colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDragHandle) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Drag handle",
                    tint = colorScheme.outline
                )
            }
            
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggleStatus(if (it) "completed" else "needs_action") },
                colors = CheckboxDefaults.colors(
                    checkedColor = colorScheme.primary
                )
            )
            
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
                    .padding(horizontal = 8.dp),
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                color = if (isCompleted) 
                    colorScheme.onSurface.copy(alpha = 0.6f)
                    else colorScheme.onSurface
            )
            
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }
}