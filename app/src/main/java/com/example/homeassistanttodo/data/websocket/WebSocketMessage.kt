package com.example.homeassistanttodo.data.websocket

import com.google.gson.JsonElement
import com.google.gson.JsonObject

sealed class WebSocketMessage {
    data class AuthRequired(val haVersion: String) : WebSocketMessage()
    data class AuthOk(val haVersion: String) : WebSocketMessage()
    data class AuthInvalid(val message: String) : WebSocketMessage()
    data class Result(
        val id: Int,
        val type: String,
        val success: Boolean,
        val result: JsonElement? = null,
        val error: JsonObject? = null
    ) : WebSocketMessage()
    data class Event(val id: Int, val event: JsonObject) : WebSocketMessage()
    data class Pong(val id: Int) : WebSocketMessage()
}