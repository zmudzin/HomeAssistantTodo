package com.example.homeassistanttodo.di

import com.example.homeassistanttodo.data.websocket.core.managers.connection.ConnectionManager
import com.example.homeassistanttodo.data.websocket.core.managers.connection.DefaultConnectionManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.MessageManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.DefaultMessageManager
import com.example.homeassistanttodo.data.websocket.core.managers.ping.PingPongManager
import com.example.homeassistanttodo.data.websocket.core.managers.ping.DefaultPingPongManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.CommandIdManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebSocketModule {

    @Binds
    @Singleton
    abstract fun bindConnectionManager(
        defaultConnectionManager: DefaultConnectionManager
    ): ConnectionManager

    @Binds
    @Singleton
    abstract fun bindMessageManager(
        defaultMessageManager: DefaultMessageManager
    ): MessageManager

    @Binds
    @Singleton
    abstract fun bindPingPongManager(
        defaultPingPongManager: DefaultPingPongManager
    ): PingPongManager

    companion object {
        @Provides
        @Singleton
        fun provideCommandIdManager(): CommandIdManager {
            return CommandIdManager()
        }
    }
}