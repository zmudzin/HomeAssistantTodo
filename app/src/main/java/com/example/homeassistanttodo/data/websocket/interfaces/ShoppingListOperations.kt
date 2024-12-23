package com.example.homeassistanttodo.data.websocket.interfaces

interface ShoppingListOperations {
    suspend fun getShoppingListItems(): Result<List<String>>
}