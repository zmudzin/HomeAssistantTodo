package com.example.homeassistanttodo.data.websocket.core.managers.message

import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.commands.Command
import com.google.gson.JsonElement
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharedFlow

/**
 * Interfejs odpowiedzialny za zarządzanie komunikacją WebSocket.
 * Obsługuje przychodzące wiadomości, kolejkowanie komend i zarządzanie eventami.
 */
@ExperimentalCoroutinesApi
interface MessageManager {
    /**
     * Strumień zdarzeń WebSocket
     */
    val events: SharedFlow<WebSocketMessage.Event>

    /**
     * Obsługuje przychodzącą wiadomość WebSocket
     * @param message Treść wiadomości
     * @param apiToken Token autoryzacyjny
     */
    suspend fun handleMessage(message: String, apiToken: String)

    /**
     * Wykonuje komendę WebSocket
     * @param command Komenda do wykonania
     * @return Wynik wykonania komendy
     */
    suspend fun executeCommand(command: Command): Result<JsonElement?>

    /**
     * Rozpoczyna przetwarzanie kolejkowanych wiadomości
     */
    fun startMessageProcessing()

    /**
     * Zatrzymuje przetwarzanie kolejkowanych wiadomości
     */
    fun stopMessageProcessing()

    /**
     * Rejestruje callback dla eventów
     * @param key Unikalny klucz callbacka
     * @param callback Funkcja wywoływana przy otrzymaniu eventu
     */
    fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit)

    /**
     * Usuwa callback dla eventów
     * @param key Klucz callbacka do usunięcia
     */
    fun unregisterEventCallback(key: String)
}