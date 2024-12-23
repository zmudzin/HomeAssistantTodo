package com.example.homeassistanttodo.data.websocket.core.managers.ping

interface PingPongManager {
    fun startPingPong()
    fun stopPingPong()
    fun handlePong()
    fun sendPing()
}