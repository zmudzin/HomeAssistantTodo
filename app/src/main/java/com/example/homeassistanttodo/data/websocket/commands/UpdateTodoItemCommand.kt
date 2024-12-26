package com.example.homeassistanttodo.data.websocket.commands

class UpdateTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val uid: String,
    val status: String
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to "todo",
        "service" to if (status == "completed") "complete_item" else "uncomplete_item",
        "target" to mapOf("entity_id" to entityId),
        "service_data" to mapOf("item" to uid)
    )
}