package com.itlab.notes

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.itlab.ai.AiServiceProvider
import com.itlab.notes.ui.notesApp
import com.itlab.notes.ui.theme.notesTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Анализируем фото через YOLO
            lifecycleScope.launch(Dispatchers.IO) {
                val aiService = AiServiceProvider.getInstance(this@MainActivity)
                val tags = aiService.tagIMGs(listOf(it.toString()))

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Найдено тегов: ${tags.size}\n${tags.joinToString(", ")}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            notesTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    notesApp()

                    // КНОПКА ДЛЯ ТЕСТА
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text("📷 Тест YOLO")
                    }
                }
            }
        }
    }
}
