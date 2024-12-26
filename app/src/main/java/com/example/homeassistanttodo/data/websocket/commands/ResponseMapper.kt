package com.example.homeassistanttodo.data.websocket.commands

import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.google.gson.JsonObject

object ResponseMapper {
    fun mapResponse(jsonMessage: JsonObject): WebSocketMessage? {
        return when (jsonMessage.get("type").asString) {
            "result" -> WebSocketMessage.Result(
                id = jsonMessage.get("id").asInt,
                success = jsonMessage.get("success").asBoolean,
                result = jsonMessage.get("result"),
                error = if (jsonMessage.has("error")) jsonMessage.getAsJsonObject("error") else null
            )
            "pong" -> WebSocketMessage.Pong(
                id = jsonMessage.get("id").asInt
            )
            "event" -> WebSocketMessage.Event(
                id = jsonMessage.get("id").asInt,
                event = jsonMessage.getAsJsonObject("event")
            )
            else -> null
        }
    }
}