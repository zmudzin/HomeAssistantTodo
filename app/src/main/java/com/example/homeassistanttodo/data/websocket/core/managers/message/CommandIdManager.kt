package com.example.homeassistanttodo.data.websocket.core.managers.message

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandIdManager @Inject constructor() {
    private val idCounter = AtomicInteger(0)
    
    fun getNextId(): Int = idCounter.incrementAndGet()
    
    fun getCurrentId(): Int = idCounter.get()
    
    fun reset() {
        idCounter.set(0)
    }
}