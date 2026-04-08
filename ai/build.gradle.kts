import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.gradle.api.file.RelativePath

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.itlab.ai"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            abiFilters.add("arm64-v8a")
        }
    }
    packaging {
        resources {
            excludes += "**/x86/**"
            excludes += "**/x86_64/**"
        }
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildToolsVersion = "37.0.0"
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

afterEvaluate {
    tasks.register<Copy>("extractNativeLibs") {
        // Укажите правильное имя вашего JAR
        val jarFile = file("libs/openvino-native-1.0.jar")

        if (jarFile.exists()) {
            println("Found JAR: ${jarFile.absolutePath}")

            from(zipTree(jarFile)) {
                // Включаем все .so файлы из любых lib/ папок
                include("**/*.so")

                // Логируем каждый найденный .so файл
                eachFile {
                    println("Found in JAR: ${relativePath}")
                }
            }

            // Распаковываем в jniLibs, сохраняя структуру папок
            into("src/main/jniLibs")

            doLast {
                println("Extraction complete. Files in jniLibs:")
                fileTree("src/main/jniLibs").forEach { file ->
                    println("  ${file.relativeTo(projectDir)}")
                }
            }
        } else {
            println("JAR not found: ${jarFile.absolutePath}")
        }
    }

    tasks.named("preBuild") {
        dependsOn("extractNativeLibs")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(project(":domain"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(files("libs/java-api-1.0-SNAPSHOT.jar"))
    implementation(libs.jna) {
        artifact {
            type = "aar"
        }
    }
    implementation(libs.opencv.android) {
        artifact {
            classifier = "android-arm64"
        }
    }
    runtimeOnly(libs.libcxx.provider)
}
