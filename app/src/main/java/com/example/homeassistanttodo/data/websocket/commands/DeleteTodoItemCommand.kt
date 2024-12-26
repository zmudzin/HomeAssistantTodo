package com.example.homeassistanttodo.data.websocket.commands

class DeleteTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val uid: String
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to "todo",
        "service" to "remove_item",
        "target" to mapOf("entity_id" to entityId),
        "service_data" to mapOf("item" to uid)
    )
}