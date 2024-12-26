package com.example.homeassistanttodo.data.websocket.mapper

import android.util.Log
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
            // Wydrukuj całą odpowiedź, aby zobaczyć jej strukturę
            Log.d("TodoResponseMapper", "Pełna odpowiedź: $response")

            val jsonObject = response.asJsonObject

            // Wydrukuj klucze obiektu
            jsonObject.keySet().forEach { key ->
                Log.d("TodoResponseMapper", "Klucz: $key, Wartość: ${jsonObject.get(key)}")
            }

            // Spróbuj znaleźć obiekt zadania w różnych miejscach
            val item = if (jsonObject.has("response")) {
                val responseObj = jsonObject.getAsJsonObject("response")
                if (responseObj.has(listId))
                    responseObj.getAsJsonObject(listId).getAsJsonObject("items")
                        .asJsonArray.firstOrNull()?.asJsonObject
                else null
            } else null

            item?.let { mapTodoItemFromJson(it, listId) }
        } catch (e: Exception) {
            Log.e("TodoResponseMapper", "Błąd mapowania", e)
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
            lastUpdated = getStateTimestamp(json, "last_updated"),
            due = json.get("due")?.asString,
            description = json.get("description")?.asString
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