package com.example.homeassistanttodo.data.websocket.commands

class DeleteTodoItemCommand(
    override val id: Int,
    val entityId: String,    // pełny identyfikator listy (np. "todo.lista_zakupow")
    val itemId: String       // unikalny identyfikator zadania
) : Command {
    override val type: String = "call_service"
    private val domain: String = "todo"
    private val service: String = "remove_item"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to domain,
        "service" to service,
        "service_data" to mapOf(
            "entity_id" to entityId,
            "item" to itemId  // Kluczowa zmiana: użyj 'item', nie 'id' czy 'item_id'
        )
    )
}