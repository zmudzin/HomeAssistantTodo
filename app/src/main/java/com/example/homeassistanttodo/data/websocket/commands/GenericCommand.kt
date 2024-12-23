package com.example.homeassistanttodo.data.websocket.commands

class GenericCommand(
    override val id: Int,
    private val commandName: String,
    private val parameters: Map<String, Any>?
) : Command {
    override val type: String = "command"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "command" to commandName,
        "parameters" to parameters
    )
}