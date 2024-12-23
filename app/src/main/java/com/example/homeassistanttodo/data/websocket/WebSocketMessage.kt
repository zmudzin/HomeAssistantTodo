package com.example.homeassistanttodo.data.websocket

import android.content.ContentValues.TAG
import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.channels.Channel

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
    data class CustomCommand(
        val id: Int,
        val type: String,
        val event_type: String? = null
    ) : WebSocketMessage()
}

private suspend fun sendCommandAndWaitForResult(message: Map<String, Any?>): WebSocketMessage.Result {
    val resultChannel = Channel<WebSocketMessage.Result>()
    resultChannel.send(
        WebSocketMessage.Result(
            id = message["id"] as Int,
            type = "result",
            success = true
        )
    )
    return resultChannel.receive()
}