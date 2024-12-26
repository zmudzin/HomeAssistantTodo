package com.example.homeassistanttodo.data.websocket.mapper

import com.example.homeassistanttodo.data.model.TodoItem
import com.google.gson.JsonElement
import com.google.gson.JsonObject

object TodoResponseMapper {
    fun mapTodoItems(response: JsonElement?, listId: String): List<TodoItem> {
        if (response == null) return emptyList()
        
        return try {
            val itemsArray = response.asJsonObject
                .getAsJsonObject("response")
                .getAsJsonObject(listId)
                .getAsJsonArray("items")

            itemsArray.map { itemElement ->
                mapTodoItemFromJson(itemElement.asJsonObject, listId)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun mapTodoItem(response: JsonElement?, listId: String): TodoItem? {
        if (response == null) return null
        
        return try {
            val item = response.asJsonObject
                .getAsJsonObject("response")
                .getAsJsonObject(listId)
                .getAsJsonObject("item")

            mapTodoItemFromJson(item, listId)
        } catch (e: Exception) {
            null
        }
    }

    private fun mapTodoItemFromJson(json: JsonObject, listId: String): TodoItem {
        return TodoItem(
            uid = json.get("uid").asString,
            summary = json.get("summary").asString,
            status = json.get("status").asString,
            listId = listId,
            lastChanged = getStateTimestamp(json, "last_changed"),
            lastUpdated = getStateTimestamp(json, "last_updated")
        )
    }

    private fun getStateTimestamp(json: JsonObject, field: String): String? {
        return try {
            json.get(field)?.asString
        } catch (e: Exception) {
            null
        }
    }
}