// commands/ResponseMapper.kt
package com.example.homeassistanttodo.data.websocket.commands

import com.google.gson.JsonElement
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.commands.responses.*

object ResponseMapper {
    fun mapResponse(response: WebSocketMessage.Result, command: Command): CommandResponse {
        return when (command) {
            is GetShoppingListCommand -> mapShoppingListResponse(response)
            is SubscribeEventsCommand -> mapSubscriptionResponse(response)
            is CallServiceCommand, is GenericCommand -> mapServiceCallResponse(response)
            else -> throw IllegalArgumentException("Unsupported command type")
        }
    }

    private fun mapShoppingListResponse(response: WebSocketMessage.Result): ShoppingListResponse {
        val items = response.result?.asJsonObject
            ?.get("response")
            ?.asJsonObject
            ?.get("todo.lista_zakupow")
            ?.asJsonObject
            ?.get("items")
            ?.asJsonArray
            ?.mapNotNull { it.asJsonObject.get("summary")?.asString }
            ?: emptyList()

        return ShoppingListResponse(
            success = response.success,
            items = items
        )
    }

    private fun mapServiceCallResponse(response: WebSocketMessage.Result) = ServiceCallResponse(
        success = response.success,
        result = response.result
    )

    private fun mapSubscriptionResponse(response: WebSocketMessage.Result) = SubscriptionResponse(
        success = response.success,
        subscriptionId = response.id
    )
}