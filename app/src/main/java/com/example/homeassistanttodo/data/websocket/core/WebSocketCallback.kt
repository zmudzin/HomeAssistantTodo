// core/WebSocketCallback.kt
package com.example.homeassistanttodo.data.websocket.core

/**
 * Klasa obsługująca callback'i dla wiadomości WebSocket.
 * Pozwala na rejestrację i wyrejestrowanie wielu callback'ów oraz ich wywoływanie.
 * @param T Typ danych przekazywanych przez callback
 */
class WebSocketCallback<T> {
    // Mapa przechowująca callback'i, gdzie kluczem jest identyfikator callback'a
    private val callbacks = mutableMapOf<String, (T) -> Unit>()

    /**
     * Rejestruje nowy callback.
     * @param key Unikalny identyfikator callback'a
     * @param callback Funkcja wywoływana gdy pojawią się nowe dane typu T
     */
    fun register(key: String, callback: (T) -> Unit) {
        callbacks[key] = callback
    }

    /**
     * Usuwa zarejestrowany callback.
     * @param key Identyfikator callback'a do usunięcia
     */
    fun unregister(key: String) {
        callbacks.remove(key)
    }

    /**
     * Powiadamia wszystkie zarejestrowane callback'i o nowych danych.
     * @param data Dane do przekazania callback'om
     */
    fun notify(data: T) {
        callbacks.values.forEach { callback ->
            callback(data)
        }
    }

    /**
     * Sprawdza, czy istnieje callback o podanym identyfikatorze.
     * @param key Identyfikator callback'a do sprawdzenia
     * @return true jeśli callback istnieje, false w przeciwnym razie
     */
    fun hasCallback(key: String): Boolean = callbacks.containsKey(key)

    /**
     * Zwraca liczbę zarejestrowanych callback'ów.
     */
    fun callbackCount(): Int = callbacks.size

    /**
     * Usuwa wszystkie zarejestrowane callback'i.
     */
    fun clearCallbacks() {
        callbacks.clear()
    }
}