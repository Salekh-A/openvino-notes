package com.itlab.ai

import android.util.Log
import org.openvino.java.OpenVINO

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
}
