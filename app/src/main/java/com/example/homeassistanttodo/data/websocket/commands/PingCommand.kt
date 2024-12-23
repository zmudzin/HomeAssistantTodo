package com.example.homeassistanttodo.data.websocket.commands

class PingCommand(
    override val id: Int
) : Command {
    override val type: String = "ping"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type
    )
}