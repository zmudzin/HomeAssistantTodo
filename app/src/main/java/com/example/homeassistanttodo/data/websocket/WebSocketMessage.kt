package com.example.homeassistanttodo.data.websocket

import com.google.gson.JsonElement
import com.google.gson.JsonObject

sealed class WebSocketMessage {
    data class Result(
        val id: Int,
        val success: Boolean,
        val result: JsonElement?,
        val error: JsonObject?
    ) : WebSocketMessage()

    data class Event(
        val id: Int,
        val event: JsonObject
    ) : WebSocketMessage()

    data class Pong(
        val id: Int
    ) : WebSocketMessage()
}