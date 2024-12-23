package com.example.homeassistanttodo.data.websocket.commands

class CallServiceCommand(
    override val id: Int,
    private val domain: String,
    private val service: String,
    private val serviceData: Map<String, Any>? = null
) : Command {
    override val type: String = "call_service"

    override fun toJson() = mapOf(
        "id" to id,
        "type" to type,
        "domain" to domain,
        "service" to service,
        "service_data" to serviceData
    )
}