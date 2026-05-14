package com.itlab.ai

import android.content.Context
import com.itlab.domain.ai.NoteAiService

object AiServiceProvider {

    @Volatile
    private var instance: NoteAiService? = null

    fun getInstance(context: Context): NoteAiService {
        return instance ?: synchronized(this) {
            instance ?: createInstance(context).also { instance = it }
        }
    }

    private fun createInstance(context: Context): NoteAiService {
        val engine = OpenVinoEngine(context)
        engine.initialize() // Инициализируем сразу

        val processor = ResultProcessor()

        return OpenVinoNoteAiService(engine, processor)
    }
}
