package com.example.homeassistanttodo.data.websocket.core.managers.ping

import android.util.Log
import com.example.homeassistanttodo.data.websocket.commands.PingCommand
import com.example.homeassistanttodo.data.websocket.core.managers.message.MessageManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.CommandIdManager
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class DefaultPingPongManager @Inject constructor(
    private val scope: CoroutineScope,
    private val messageManager: MessageManager,
    private val commandIdManager: CommandIdManager
) : PingPongManager {

    private var pingJob: Job? = null
    private val lastPongTime = AtomicLong(System.currentTimeMillis())
    private var lastPingResult: Result<*>? = null

    override fun startPingPong() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(PING_INTERVAL)
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastPongTime.get() > PONG_TIMEOUT) {
                    Log.w(TAG, "Pong timeout - reconnecting")
                    break
                }
                sendPing()
            }
        }
    }

    override fun stopPingPong() {
        pingJob?.cancel()
        pingJob = null
        lastPingResult = null
    }

    override fun handlePong() {
        lastPongTime.set(System.currentTimeMillis())
        Log.d(TAG, "Received pong")
    }

    override fun sendPing() {
        scope.launch {
            try {
                val commandId = commandIdManager.getNextId()
                lastPingResult = messageManager.executeCommand(PingCommand(commandId))
                if (lastPingResult?.isFailure == true) {
                    Log.e(TAG, "Ping failed", lastPingResult?.exceptionOrNull())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending ping", e)
            }
        }
    }

    companion object {
        private const val TAG = "DefaultPingPongManager"
        private const val PING_INTERVAL = 30000L
        private const val PONG_TIMEOUT = 45000L
    }
}