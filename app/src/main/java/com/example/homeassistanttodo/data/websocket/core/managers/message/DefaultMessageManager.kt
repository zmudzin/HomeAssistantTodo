package com.example.homeassistanttodo.data.websocket.core.managers.message

import android.util.Log
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.commands.*
import com.example.homeassistanttodo.data.websocket.core.WebSocketCallback
import com.example.homeassistanttodo.data.websocket.core.managers.connection.ConnectionManager
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultMessageManager @Inject constructor(
    private val gson: Gson,
    private val scope: CoroutineScope,
    private val connectionManager: ConnectionManager
) : MessageManager {

    companion object {
        private const val TAG = "DefaultMessageManager"
        private const val COMMAND_TIMEOUT = 10000L
        private const val MESSAGE_PROCESSING_INTERVAL = 1000L
    }

    private val messageQueue = Channel<Command>(Channel.BUFFERED)
    private val pendingCommands = mutableMapOf<Int, CompletableDeferred<WebSocketMessage.Result>>()
    private val eventCallbacks = WebSocketCallback<WebSocketMessage.Event>()
    
    private var messageProcessingJob: Job? = null
    private var isAuthenticated = false

    private val _events = MutableSharedFlow<WebSocketMessage.Event>()
    override val events: SharedFlow<WebSocketMessage.Event> = _events.asSharedFlow()

    override suspend fun handleMessage(message: String, apiToken: String) {
        try {
            Log.d(TAG, "Received message: $message")
            val jsonMessage = gson.fromJson(message, JsonObject::class.java)
            processMessage(jsonMessage, apiToken)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message", e)
        }
    }

    private suspend fun processMessage(jsonMessage: JsonObject, apiToken: String) {
        when (jsonMessage.get("type").asString) {
            "auth_required" -> handleAuthRequired(apiToken)
            "auth_ok" -> handleAuthOk()
            "auth_invalid" -> handleAuthInvalid()
            "result" -> handleResult(jsonMessage)
            "event" -> handleEvent(jsonMessage)
        }
    }

    private suspend fun handleAuthRequired(apiToken: String) {
        Log.d(TAG, "Auth required, sending auth command")
        isAuthenticated = false
        executeCommand(AuthCommand(apiToken))
    }

    private suspend fun handleAuthOk() {
        Log.d(TAG, "Auth OK")
        isAuthenticated = true
        processQueuedMessages()
    }

    private fun handleAuthInvalid() {
        Log.e(TAG, "Auth invalid")
        isAuthenticated = false
    }

    private suspend fun handleResult(jsonMessage: JsonObject) {
        val id = jsonMessage.get("id").asInt
        Log.d(TAG, "Received result for command $id: $jsonMessage")
        pendingCommands[id]?.complete(gson.fromJson(jsonMessage, WebSocketMessage.Result::class.java))
    }

    private suspend fun handleEvent(jsonMessage: JsonObject) {
        val event = gson.fromJson(jsonMessage, WebSocketMessage.Event::class.java)
        Log.d(TAG, "Received event: $event")
        _events.emit(event)
        eventCallbacks.notify(event)
    }

    private suspend fun processQueuedMessages() {
        try {
            while (isAuthenticated) {
                val command = messageQueue.tryReceive().getOrNull() ?: break
                Log.d(TAG, "Processing queued command: ${command::class.simpleName} with id: ${command.id}")
                executeCommand(command)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing queued messages", e)
        }
    }

    override suspend fun executeCommand(command: Command): Result<JsonElement?> {
        return try {
            Log.d(TAG, "Executing command: ${command::class.simpleName} with id: ${command.id}")
            
            if (shouldQueueCommand(command)) {
                queueCommand(command)
                return Result.success(null)
            }

            val result = sendCommandAndAwaitResponse(command)
            Log.d(TAG, "Command ${command.id} completed with result: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Command execution error (${command::class.simpleName})", e)
            Result.failure(e)
        }
    }

    private fun shouldQueueCommand(command: Command): Boolean =
        !isAuthenticated && command !is AuthCommand

    private suspend fun queueCommand(command: Command) {
        Log.d(TAG, "Not authenticated, queueing command ${command.id}")
        messageQueue.send(command)
    }

    private suspend fun sendCommandAndAwaitResponse(command: Command): Result<JsonElement?> {
        val deferred = setupCommandDeferred(command)
        sendCommand(command)
        
        if (command.id == 0) {
            return Result.success(null)
        }

        val response = awaitCommandResponse(command, deferred)
        return processCommandResponse(response)
    }

    private fun setupCommandDeferred(command: Command): CompletableDeferred<WebSocketMessage.Result>? {
        return if (command.id != 0) {
            CompletableDeferred<WebSocketMessage.Result>().also {
                pendingCommands[command.id] = it
            }
        } else null
    }

    private suspend fun sendCommand(command: Command) {
        try {
            val commandJson = command.toJson()
            Log.d(TAG, "Sending command ${command.id}: $commandJson")
            connectionManager.sendMessage(gson.toJson(commandJson))
        } catch (e: Exception) {
            Log.e(TAG, "Error sending command ${command.id}", e)
            pendingCommands.remove(command.id)
            throw e
        }
    }

    private suspend fun awaitCommandResponse(
        command: Command,
        deferred: CompletableDeferred<WebSocketMessage.Result>?
    ): WebSocketMessage.Result {
        return withTimeout(COMMAND_TIMEOUT) {
            Log.d(TAG, "Waiting for response to command ${command.id}")
            deferred?.await() ?: throw IllegalStateException("No deferred for command ${command.id}")
        }
    }

    private fun processCommandResponse(response: WebSocketMessage.Result): Result<JsonElement?> {
        return if (response.success) {
            Result.success(response.result)
        } else {
            Result.failure(Exception(response.error?.get("message")?.asString ?: "Unknown error"))
        }
    }

    override fun startMessageProcessing() {
        messageProcessingJob?.cancel()
        messageProcessingJob = scope.launch {
            Log.d(TAG, "Starting message processing")
            while (isActive) {
                delay(MESSAGE_PROCESSING_INTERVAL)
                if (isAuthenticated && !messageQueue.isEmpty) {
                    processQueuedMessages()
                }
            }
        }
    }

    override fun stopMessageProcessing() {
        messageProcessingJob?.cancel()
        messageProcessingJob = null
        isAuthenticated = false
    }

    override fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit) {
        eventCallbacks.register(key, callback)
    }

    override fun unregisterEventCallback(key: String) {
        eventCallbacks.unregister(key)
    }
}