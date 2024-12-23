// commands/Command.kt
package com.example.homeassistanttodo.data.websocket.commands

interface Command {
    val id: Int
    val type: String
    fun toJson(): Map<String, Any?>
}