package com.example.homeassistanttodo.data.websocket.commands

class CreateTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val summary: String
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to "todo",
        "service" to "add_item",
        "target" to mapOf("entity_id" to entityId),
        "service_data" to mapOf("item" to summary)
    )
}