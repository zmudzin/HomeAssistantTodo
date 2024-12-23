
package com.example.homeassistanttodo.data.websocket.commands

class GetShoppingListCommand(
    override val id: Int,
    private val entityId: String = "todo.lista_zakupow"
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to "todo",
        "service" to "get_items",
        "target" to mapOf("entity_id" to entityId),
        "return_response" to true
    )
}
