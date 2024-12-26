package com.example.homeassistanttodo.data.websocket.commands

class UpdateTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val uid: String,
    val status: String,
    val description: String? = null,
    val due: String? = null
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to "call_service",
        "domain" to "todo",
        "service" to "update_item",
        "target" to mapOf("entity_id" to entityId),
        "service_data" to buildMap {
            put("item", uid)
            put("status", status)
            description?.let { put("description", it) }
            due?.let { put("due", it) }
        }
    )
}