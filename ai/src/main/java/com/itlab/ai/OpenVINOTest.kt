package com.itlab.ai

import android.util.Log
import org.openvino.java.OpenVINO
import org.openvino.java.core.Core
import org.openvino.java.core.Model
import org.openvino.java.core.CompiledModel
import org.openvino.java.core.InferRequest
import org.openvino.java.core.Tensor
import java.io.File
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

// TODO
// 1. Сделать сэмпл моделей в этом тесте
// 2. Добавить всё новое в лок депенденсис и раскомментировать
// 3. Понять почему 17 сдк и сделать в зависимости от того зачем оно
// 4. Начать интегрировать методы для интерфейса Златы Л
// Не забыть убрать из аппа вызов теста!

object OpenVINOTest {

    private const val TAG = "OpenVINOTest"

    fun testVersion() {
        try {
            // 1. Загружаем библиотеку OpenVINO
            // Если ваша .so библиотека лежит в стандартном пути (src/main/jniLibs/arm64-v8a/),
            // то метод load() без аргументов должен её найти.
            val vino = OpenVINO.load()
            Log.d(TAG, "OpenVINO library loaded successfully!")

            // 2. Получаем и выводим версию
            val version = vino.version
            Log.i(TAG, "---- OpenVINO INFO ----")
            Log.i(TAG, "Description: ${version.description}")
            Log.i(TAG, "Build number: ${version.buildNumber}")

        } catch (e: Exception) {
            Log.e(TAG, "OpenVINO test failed!", e)
        }
    }

    // ========== RESNET50 ПОЛНЫЙ ТЕСТ (из ResNetDetection.java) ==========
    
    fun testResNet50(context: Context) {
        try {
            Log.i(TAG, "=== RESNET50 ПОЛНЫЙ ТЕСТ ===")
            
            // 1. Загружаем OpenVINO
            val core = Core()
            
            // 2. Копируем модель из assets во временную папку
            val modelFile = copyAssetToFile(context, "models/resnet50-v2-7/resnet50-v2-7.xml")
            val modelBin = copyAssetToFile(context, "models/resnet50-v2-7/resnet50-v2-7.bin")
            
            Log.i(TAG, "Модель скопирована: ${modelFile.absolutePath}")
            
            // 3. Загружаем модель
            val ovModel = core.readModel(modelFile.absolutePath)
            Log.i(TAG, "✅ Модель загружена")
            
            // 4. Получаем имена входов и выходов
            val inputName = (ovModel.inputs().get(0) as org.openvino.java.core.Input).anyName
            val outputName = (ovModel.outputs().get(0) as org.openvino.java.core.Output).anyName
            Log.i(TAG, "Вход: $inputName, Выход: $outputName")
            
            // 5. Компилируем для CPU
            val compiledModel = core.compileModel(ovModel, "CPU")
            Log.i(TAG, "✅ Модель скомпилирована для CPU")
            
            // 6. Создаем тестовое изображение (224x224 белое с черным квадратом)
            val size = 224
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (y in 0 until size) {
                for (x in 0 until size) {
                    val color = if (x in 50..174 && y in 50..174) {
                        android.graphics.Color.BLACK
                    } else {
                        android.graphics.Color.WHITE
                    }
                    bitmap.setPixel(x, y, color)
                }
            }
            
            // 7. Преобразуем изображение в float массив (BGR формат)
            val inputData = preprocessBitmap(bitmap, size, size)
            Log.i(TAG, "Подготовлены данные: ${inputData.size} элементов")
            
            // 8. Создаем запрос и заполняем тензор
            val request = compiledModel.createInferRequest()
            val inputTensor = request.getTensor(inputName)
            inputTensor.setData(inputData)
            Log.i(TAG, "✅ Тензор заполнен")
            
            // 9. Инференс
            Log.i(TAG, "🔄 Запуск инференса...")
            val start = System.currentTimeMillis()
            request.infer()
            val time = System.currentTimeMillis() - start
            Log.i(TAG, "✅ Инференс выполнен за ${time} мс")
            
            // 10. Получаем результат
            val outputTensor = request.getTensor(outputName)
            val outputData = getTensorData(outputTensor)
            
            if (outputData != null && outputData.isNotEmpty()) {
                Log.i(TAG, "Размер выхода: ${outputData.size}")
                
                // Топ-5 предсказаний
                val indices = outputData.indices.sortedByDescending { outputData[it] }.take(5)
                Log.i(TAG, "Топ-5 предсказаний:")
                indices.forEachIndexed { i, idx ->
                    Log.i(TAG, "  ${i+1}. класс $idx: ${String.format("%.4f", outputData[idx])}")
                }
            }
            
            core.free()
            Log.i(TAG, "=== RESNET50 ТЕСТ ЗАВЕРШЕН ===")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ ResNet50 тест failed!", e)
        }
    }
    
    private fun preprocessBitmap(bitmap: Bitmap, w: Int, h: Int): FloatArray {
        // Ресайзим до 224x224
        val resized = Bitmap.createScaledBitmap(bitmap, w, h, true)
        
        val data = FloatArray(3 * w * h)
        val pixels = IntArray(w * h)
        resized.getPixels(pixels, 0, w, 0, 0, w, h)
        
        for (y in 0 until h) {
            for (x in 0 until w) {
                val pixel = pixels[y * w + x]
                val r = ((pixel shr 16) and 0xFF) / 255.0f
                val g = ((pixel shr 8) and 0xFF) / 255.0f
                val b = (pixel and 0xFF) / 255.0f
                
                // ResNet50 ожидает BGR порядок
                data[y * w + x] = b
                data[h * w + y * w + x] = g
                data[2 * h * w + y * w + x] = r
            }
        }
        return data
    }
    
    private fun getTensorData(tensor: Tensor): FloatArray? {
        return try {
            val method = Tensor::class.java.getDeclaredMethod("getDataAsFloatArray")
            method.isAccessible = true
            method.invoke(tensor) as FloatArray
        } catch (e: Exception) {
            try {
                val method = Tensor::class.java.getDeclaredMethod("getData", Class::class.java)
                method.isAccessible = true
                method.invoke(tensor, FloatArray::class.java) as FloatArray
            } catch (e2: Exception) {
                Log.e(TAG, "Не удалось получить данные тензора", e2)
                null
            }
        }
    }
    
    private fun copyAssetToFile(context: Context, assetPath: String): File {
        val destFile = File(context.cacheDir, assetPath.substringAfterLast("/"))
        if (!destFile.exists()) {
            context.assets.open(assetPath).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        return destFile
    }
}
