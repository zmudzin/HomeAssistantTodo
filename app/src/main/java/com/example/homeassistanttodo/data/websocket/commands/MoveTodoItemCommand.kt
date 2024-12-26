package com.example.homeassistanttodo.data.websocket.commands

class MoveTodoItemCommand(
    override val id: Int,
    private val serviceData: Map<String, Any>?
) : Command {
    override val type: String = "call_service"
    private val domain: String = "todo"
    private val service: String = "move_item"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to domain,
        "service" to service,
        "service_data" to serviceData
    )
}