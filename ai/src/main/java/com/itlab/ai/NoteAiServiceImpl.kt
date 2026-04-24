package com.itlab.ai

import android.content.Context
import com.itlab.domain.ai.NoteAiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class NoteAiServiceImpl(private val context: Context) : NoteAiService {

    private val modelPath: String by lazy {
        copyModelToCache()
    }

    init {
        CotypeTag.init(modelPath)
        CotypeSummarize.init(modelPath)
    }

    override suspend fun summarize(text: String): String = withContext(Dispatchers.IO) {
        if (text.length < 50) return@withContext text
        val token = CotypeSummarize.summarize(text).toIntOrNull() ?: 0
        decodeToken(token)
    }

    override suspend fun tagTXT(text: String): Set<String> = withContext(Dispatchers.IO) {
        val token = CotypeTag.tag(text).toIntOrNull() ?: 0
        val decoded = decodeToken(token)
        if (decoded.isNotEmpty()) setOf(decoded) else emptySet()
    }

    override suspend fun tagIMGs(img: List<String>): Set<String> = withContext(Dispatchers.IO) {
        emptySet()
    }

    private fun decodeToken(token: Int): String {
        return if (token in 1001..1999) {
            (token - 1000).toChar().toString()
        } else {
            ""
        }
    }

    private fun copyModelToCache(): String {
        val modelFile = File(context.cacheDir, "openvino_model.xml")
        val binFile = File(context.cacheDir, "openvino_model.bin")

        if (!modelFile.exists()) {
            context.assets.open("ai/models/cotype-cpu/openvino_model.xml").use { input ->
                FileOutputStream(modelFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        if (!binFile.exists()) {
            context.assets.open("ai/models/cotype-cpu/openvino_model.bin").use { input ->
                FileOutputStream(binFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return modelFile.absolutePath
    }
}
