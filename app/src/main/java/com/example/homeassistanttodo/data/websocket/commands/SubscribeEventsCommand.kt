package com.example.homeassistanttodo.data.websocket.commands

class SubscribeEventsCommand(
    override val id: Int,
    private val eventType: String?
) : Command {
    override val type: String = "subscribe_events"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "event_type" to eventType
    )
}