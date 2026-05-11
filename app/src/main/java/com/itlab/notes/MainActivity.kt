package com.itlab.notes

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.itlab.ai.OpenVinoEngine
import com.itlab.notes.ui.notesApp
import com.itlab.notes.ui.theme.notesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val modelDir = File(filesDir, "models/yolo26n_openvino_model")
        val xmlFile = File(modelDir, "yolo26n.xml")
        val engine = OpenVinoEngine(modelXmlPath = xmlFile.absolutePath, context = this)

        engine.test()
        setContent {
            notesTheme {
                notesApp()
            }
        }

    }
}



