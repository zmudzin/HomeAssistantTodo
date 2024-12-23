
package com.example.homeassistanttodo.data.websocket.commands.responses

import com.google.gson.JsonElement

sealed interface CommandResponse {
    val success: Boolean
}

data class ShoppingListResponse(
    override val success: Boolean,
    val items: List<String>
) : CommandResponse

data class SubscriptionResponse(
    override val success: Boolean,
    val subscriptionId: Int
) : CommandResponse

data class ServiceCallResponse(
    override val success: Boolean,
    val result: JsonElement?
) : CommandResponse