package com.example.homeassistanttodo.data.websocket.commands

class AuthCommand(
    private val accessToken: String
) : Command {
    override val id: Int = 0 // Auth command doesn't need an ID
    override val type: String = "auth"

    override fun toJson() = mapOf(
        "type" to type,
        "access_token" to accessToken
    )
}