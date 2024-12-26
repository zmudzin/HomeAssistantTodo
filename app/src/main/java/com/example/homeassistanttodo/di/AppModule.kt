package com.example.homeassistanttodo.di

import com.example.homeassistanttodo.data.websocket.WebSocketService
import com.example.homeassistanttodo.data.websocket.core.HomeAssistantWebSocket
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindWebSocketService(
        webSocket: HomeAssistantWebSocket
    ): WebSocketService
}