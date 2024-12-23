package com.example.homeassistanttodo.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseInspectorTest {
    private lateinit var db: AppDatabase
    
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @Test
    fun inspectDatabase() = runBlocking {
        // Dodaj przykładowe dane
        val testItems = listOf(
            TodoItem("1", "Zadanie 1", TodoItemStatus.NEEDS_ACTION, 1),
            TodoItem("2", "Zadanie 2", TodoItemStatus.COMPLETED, 2)
        )
        
        db.todoItemDao().insertItems(testItems)
        
        // Pobierz i wyświetl wszystkie dane
        val items = db.todoItemDao().getAllItems().first()
        println("=== Zawartość bazy danych ===")
        items.forEach { item ->
            println("""
                |ID: ${item.uid}
                |Opis: ${item.summary}
                |Status: ${item.status}
                |Pozycja: ${item.orderPosition}
                |------------------------
            """.trimMargin())
        }
    }

    @After
    fun closeDb() {
        db.close()
    }
}
