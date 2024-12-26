package com.example.homeassistanttodo.data.websocket.commands

class CreateTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val summary: String,
    val description: String? = null,
    val due: String? = null
) : Command {
    override val type: String = "call_service"

    override fun toJson() = buildMap {
        put("id", id)
        put("type", type)
        put("domain", "todo")
        put("service", "add_item")
        put("target", mapOf("entity_id" to entityId))

        val serviceData = mutableMapOf("item" to summary)
        description?.let { serviceData["description"] = it }
        due?.let { serviceData["due"] = it }

        put("service_data", serviceData)
    }
}