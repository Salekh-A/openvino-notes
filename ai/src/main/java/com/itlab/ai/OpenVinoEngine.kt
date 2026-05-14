package com.itlab.ai

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import org.intel.openvino.CompiledModel
import org.intel.openvino.Core
import org.intel.openvino.InferRequest
import org.intel.openvino.Model
import org.intel.openvino.Output
import org.intel.openvino.Tensor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import androidx.core.net.toUri
import androidx.core.graphics.scale

@Suppress("TooGenericExceptionCaught", "TooManyFunctions")
class OpenVinoEngine(
    private val context: Context? = null,
    private val modelXmlPath: String = "",
    private val deviceName: String = "CPU",
) {

    private var core: Core? = null
    private var model: Model? = null
    private var compiledModel: CompiledModel? = null
    private var inferRequest: InferRequest? = null
    private var isInitialized = false
    private var activeModelXmlPath = modelXmlPath

    // YOLO параметры
    private val inputSize = 640
    private val confThreshold = 0.35f

    var inputName: String = ""
        private set
    var inputShape: LongArray = longArrayOf()
        private set
    var inputElementType: String = ""
        private set

    var outputName: String = ""
        private set
    var outputShape: LongArray = longArrayOf()
        private set
    var outputElementType: String = ""
        private set

    companion object {
        private const val TAG = "OpenVinoEngine"
        private const val DEFAULT_MODEL_ASSET_DIR = "yolo26n_openvino_model"
        private const val DEFAULT_MODEL_XML = "yolo26n.xml"
    }

    // ==================== LLM методы (заглушки) ====================

    fun runLlmSummary(text: String): String = text

    fun runLlmTagging(text: String): String = text

    // ==================== YOLO метод (реальная детекция) ====================

    fun runYoloTagging(imageSource: String): String {
        if (!isInitialized) {
            Log.e(TAG, "Engine not initialized")
            return ""
        }

        return try {
            val uri = imageSource.toUri()
            val bitmap = context?.contentResolver?.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }

            if (bitmap == null) {
                Log.e(TAG, "Failed to load image: $imageSource")
                return ""
            }

            // Препроцессинг
            val inputData = preprocess(bitmap)

            // Инференс
            val inputTensor = Tensor(intArrayOf(1, 3, inputSize, inputSize), inputData)
            inferRequest?.set_input_tensor(inputTensor)
            inferRequest?.infer()

            // Получение результатов
            val outputTensor = inferRequest?.get_output_tensor()
            val outputData = outputTensor?.data() as? FloatArray ?: return ""

            // Парсинг детекций
            val detections = parseDetections(outputData)

            bitmap.recycle()

            if (detections.isEmpty()) return ""

            // Возвращаем теги через запятую (ResultProcessor.normalizeTags разберёт)
            detections.joinToString(", ") { detection ->
                getClassName(detection.classId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "YOLO tagging failed: ${e.message}", e)
            ""
        }
    }

    private fun preprocess(bitmap: android.graphics.Bitmap): FloatArray {
        val resized = bitmap.scale(inputSize, inputSize)
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)

        val inputData = FloatArray(1 * 3 * inputSize * inputSize)
        val area = inputSize * inputSize

        for (i in pixels.indices) {
            val r = (pixels[i] shr 16 and 0xFF) / 255f
            val g = (pixels[i] shr 8 and 0xFF) / 255f
            val b = (pixels[i] and 0xFF) / 255f

            inputData[i] = b          // B
            inputData[area + i] = g   // G
            inputData[2 * area + i] = r // R
        }
        return inputData
    }

    private fun parseDetections(outputData: FloatArray): List<Detection> {
        val detections = mutableListOf<Detection>()
        val numDetections = 300

        for (i in 0 until numDetections) {
            val base = i * 6
            if (base + 5 >= outputData.size) break

            val x1 = outputData[base] * inputSize
            val y1 = outputData[base + 1] * inputSize
            val x2 = outputData[base + 2] * inputSize
            val y2 = outputData[base + 3] * inputSize
            val confidence = outputData[base + 4]
            val classId = outputData[base + 5].toInt()

            if (confidence > confThreshold) {
                detections.add(Detection(x1, y1, x2, y2, confidence, classId))
            }
        }
        return detections
    }

    private fun getClassName(classId: Int): String {
        return when (classId) {
            0 -> "person"
            1 -> "bicycle"
            2 -> "car"
            3 -> "motorcycle"
            4 -> "airplane"
            5 -> "bus"
            6 -> "train"
            7 -> "truck"
            8 -> "boat"
            9 -> "traffic light"
            10 -> "fire hydrant"
            11 -> "stop sign"
            12 -> "parking meter"
            13 -> "bench"
            14 -> "bird"
            15 -> "cat"
            16 -> "dog"
            17 -> "horse"
            18 -> "sheep"
            19 -> "cow"
            20 -> "elephant"
            21 -> "bear"
            22 -> "zebra"
            23 -> "giraffe"
            24 -> "backpack"
            25 -> "umbrella"
            26 -> "handbag"
            27 -> "tie"
            28 -> "suitcase"
            29 -> "frisbee"
            30 -> "skis"
            31 -> "snowboard"
            32 -> "sports ball"
            33 -> "kite"
            34 -> "baseball bat"
            35 -> "baseball glove"
            36 -> "skateboard"
            37 -> "surfboard"
            38 -> "tennis racket"
            39 -> "bottle"
            40 -> "wine glass"
            41 -> "cup"
            42 -> "fork"
            43 -> "knife"
            44 -> "spoon"
            45 -> "bowl"
            46 -> "banana"
            47 -> "apple"
            48 -> "sandwich"
            49 -> "orange"
            50 -> "broccoli"
            51 -> "carrot"
            52 -> "hot dog"
            53 -> "pizza"
            54 -> "donut"
            55 -> "cake"
            56 -> "chair"
            57 -> "couch"
            58 -> "potted plant"
            59 -> "bed"
            60 -> "dining table"
            61 -> "toilet"
            62 -> "tv"
            63 -> "laptop"
            64 -> "mouse"
            65 -> "remote"
            66 -> "keyboard"
            67 -> "cell phone"
            68 -> "microwave"
            69 -> "oven"
            70 -> "toaster"
            71 -> "sink"
            72 -> "refrigerator"
            73 -> "book"
            74 -> "clock"
            75 -> "vase"
            76 -> "scissors"
            77 -> "teddy bear"
            78 -> "hair drier"
            79 -> "toothbrush"
            else -> "object_$classId"
        }
    }

    // ==================== Инициализация OpenVINO ====================

    fun initialize(): Boolean {
        debugLog { "Initializing OpenVINO Engine..." }
        isInitialized = false

        val appContext = context
        return when {
            appContext == null -> {
                Log.e(TAG, "Android context is required to initialize OpenVINO")
                false
            }
            else -> initializeWithResolvedModel(appContext)
        }
    }

    private fun initializeWithResolvedModel(appContext: Context): Boolean {
        val resolvedModelXmlPath = resolveModelXmlPath(appContext) ?: return false
        activeModelXmlPath = resolvedModelXmlPath

        return if (!File(activeModelXmlPath).exists()) {
            Log.e(TAG, "❌ Model file not found: $activeModelXmlPath")
            false
        } else {
            initializeOpenVino(appContext)
        }
    }

    private fun resolveModelXmlPath(appContext: Context): String? {
        val modelDir = File(appContext.filesDir, DEFAULT_MODEL_ASSET_DIR)
        val modelXml = File(modelDir, DEFAULT_MODEL_XML)
        val resolvedPath =
            when {
                modelXmlPath.isNotBlank() -> modelXmlPath
                modelXml.exists() -> modelXml.absolutePath
                copyAssetDirectory(appContext, DEFAULT_MODEL_ASSET_DIR, modelDir) -> modelXml.absolutePath
                else -> {
                    Log.e(TAG, "Bundled model assets are missing: $DEFAULT_MODEL_ASSET_DIR")
                    null
                }
            }
        return resolvedPath
    }

    private fun copyAssetDirectory(
        appContext: Context,
        assetPath: String,
        targetDir: File,
    ): Boolean {
        val entries = appContext.assets.list(assetPath).orEmpty()
        if (entries.isEmpty()) {
            return false
        }

        targetDir.mkdirs()
        return entries.all { entry ->
            val childAssetPath = "$assetPath/$entry"
            val childTarget = File(targetDir, entry)
            val childEntries = appContext.assets.list(childAssetPath).orEmpty()
            if (childEntries.isEmpty()) {
                copyAssetFile(appContext, childAssetPath, childTarget)
            } else {
                copyAssetDirectory(appContext, childAssetPath, childTarget)
            }
        }
    }

    private fun copyAssetFile(
        appContext: Context,
        assetPath: String,
        targetFile: File,
    ): Boolean =
        try {
            targetFile.parentFile?.mkdirs()
            appContext.assets.open(assetPath).use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to copy asset: $assetPath", e)
            false
        }

    private fun initializeOpenVino(appContext: Context): Boolean =
        try {
            val pluginsFile = preparePluginsFile(appContext)
            val activeCore = createCore(pluginsFile)
            val devices = activeCore.get_available_devices()
            debugLog { "Available devices: $devices" }

            if (devices.isNullOrEmpty()) {
                Log.e(TAG, "No OpenVINO devices available")
                false
            } else {
                loadAndCompileModel(activeCore)
                isInitialized = true
                debugLog { "✅ Engine initialized successfully" }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize engine: ${e.message}", e)
            false
        }

    private fun preparePluginsFile(appContext: Context): File {
        val pluginsFile = File(appContext.filesDir, "plugins.xml")
        if (!pluginsFile.exists()) {
            copyPluginsFile(appContext, pluginsFile)
        }
        return pluginsFile
    }

    private fun copyPluginsFile(
        appContext: Context,
        pluginsFile: File,
    ) {
        try {
            appContext.assets.open("plugins.xml").use { input ->
                FileOutputStream(pluginsFile).use { output ->
                    input.copyTo(output)
                }
            }
            debugLog { "plugins.xml copied to: ${pluginsFile.absolutePath}" }
        } catch (e: IOException) {
            Log.w(TAG, "No plugins.xml in assets, trying without explicit config", e)
        }
    }

    private fun createCore(pluginsFile: File): Core =
        if (pluginsFile.exists()) {
            debugLog { "Creating Core with config: ${pluginsFile.absolutePath}" }
            Core(pluginsFile.absolutePath)
        } else {
            debugLog { "Creating Core without config" }
            Core()
        }

    private fun loadAndCompileModel(activeCore: Core) {
        core = activeCore
        model = activeCore.read_model(activeModelXmlPath) ?: error("Failed to read model")

        val activeModel = model ?: error("Model was not stored")
        configureInput(activeModel.input() ?: error("No input in model"))
        configureOutput(activeModel.output() ?: error("No output in model"))

        debugLog { "Compiling model for device: $deviceName" }
        compiledModel = activeCore.compile_model(activeModel, deviceName) ?: error("Failed to compile model")
        inferRequest = compiledModel?.create_infer_request() ?: error("Failed to create infer request")
    }

    private fun configureInput(input: Output) {
        inputName = safeGetAnyName(input) ?: "input"
        inputShape = input.get_shape().map { it.toLong() }.toLongArray()
        inputElementType = input.get_element_type().toString()
        debugLog { "Input - name: $inputName, shape: ${inputShape.contentToString()}, type: $inputElementType" }
    }

    private fun configureOutput(output: Output) {
        outputName = safeGetAnyName(output) ?: "output"
        outputShape = output.get_shape().map { it.toLong() }.toLongArray()
        outputElementType = output.get_element_type().toString()
        debugLog { "Output - name: $outputName, shape: ${outputShape.contentToString()}, type: $outputElementType" }
    }

    private fun safeGetAnyName(output: Output): String? =
        try {
            output.get_any_name()
        } catch (e: Exception) {
            Log.w(TAG, "Tensor has no OpenVINO name, using <unnamed>: ${e.message}")
            "<unnamed>"
        }

    fun getModelInfo(): String {
        if (!isInitialized) {
            return "Engine not initialized"
        }
        return buildString {
            appendLine("Model loaded successfully")
            appendLine("Device: $deviceName")
            appendLine("Input: $inputName $inputElementType ${inputShape.contentToString()}")
            appendLine("Output: $outputName $outputElementType ${outputShape.contentToString()}")
        }
    }

    fun isReady(): Boolean = isInitialized

    fun release() {
        try {
            inferRequest = null
            compiledModel = null
            model = null
            core = null
            isInitialized = false
            debugLog { "Engine resources released" }
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources: ${e.message}", e)
        }
    }

    fun test(): Boolean =
        try {
            debugLog { "=== Starting OpenVINO Engine Test ===" }
            initialize()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Test failed: ${e.message}", e)
            false
        }

    protected fun finalize() {
        release()
    }

    private fun debugLog(message: () -> String) {
        Log.d(TAG, message())
    }
}
