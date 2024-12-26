package com.example.homeassistanttodo.data.model

data class TodoItem(
    val uid: String,
    val summary: String,
    val status: String,
    val listId: String, // ID listy do której należy zadanie
    val lastChanged: String? = null,
    val lastUpdated: String? = null,
    val due: String? = null,           // Opcjonalny termin
    val description: String? = null     // Opcjonalny opis
)

enum class TodoStatus {
    NEEDS_ACTION,
    COMPLETED;

    companion object {
        fun fromString(status: String): TodoStatus = when (status.uppercase()) {
            "NEEDS_ACTION" -> NEEDS_ACTION
            "COMPLETED" -> COMPLETED
            else -> NEEDS_ACTION
        }
    }
}

data class TodoList(
    val entityId: String,  // np. "todo.lista_zakupow"
    val name: String,      // przyjazna nazwa listy
    val domain: String = "todo"  // domena w HA, domyślnie "todo"
)