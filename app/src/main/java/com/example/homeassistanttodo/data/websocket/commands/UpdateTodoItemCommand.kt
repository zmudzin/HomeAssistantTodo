package com.example.homeassistanttodo.data.websocket.commands

class UpdateTodoItemCommand(
    override val id: Int,
    val entityId: String,
    val uid: String,
    val rename: String? = null,
    val status: String? = null,
    val description: String? = null,
    val due: String? = null
) : Command {
    override val type: String = "call_service"

    override fun toJson() = buildMap {
        put("id", id)
        put("type", "call_service")
        put("domain", "todo")
        put("service", "update_item")
        put("service_data", mapOf(
            "entity_id" to entityId,
            "item" to uid
        ).let { baseData ->
            val mutableData = baseData.toMutableMap()
            rename?.let { mutableData["rename"] = it }
            status?.let { mutableData["status"] = it }
            description?.let { mutableData["description"] = it }
            due?.let { mutableData["due"] = it }
            mutableData
        })
    }
}