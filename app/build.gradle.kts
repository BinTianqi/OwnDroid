import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    alias(libs.plugins.serialization)
}

android {
    signingConfigs {
        create("defaultSignature") {
            storeFile = file(project.findProperty("StoreFile") ?: "testkey.jks")
            storePassword = (project.findProperty("StorePassword") as String?) ?: "testkey"
            keyPassword = (project.findProperty("KeyPassword") as String?) ?: "testkey"
            keyAlias = (project.findProperty("KeyAlias") as String?) ?: "testkey"
        }
    }
    namespace = "com.bintianqi.owndroid"
    compileSdk = 36

    lint.checkReleaseBuilds = false
    lint.disable += "All"

    defaultConfig {
        applicationId = "com.bintianqi.owndroid"
        minSdk = 21
        targetSdk = 36
        versionCode = 41
        versionName = "7.2"
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("defaultSignature")
        }
        debug {
            signingConfig = signingConfigs.getByName("defaultSignature")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    androidResources {
        generateLocaleConfig = true
    }
    dependenciesInfo {
        includeInApk = false
    }
    composeCompiler {
        includeSourceInformation = false
        includeTraceMarkers = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
    sourceSets {
        all {
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}

gradle.taskGraph.whenReady {
    project.tasks.findByPath(":app:test")?.enabled = false
    project.tasks.findByPath(":app:lint")?.enabled = false
    project.tasks.findByPath(":app:lintAnalyzeDebug")?.enabled = false
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.permissions)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.api)
    implementation(libs.dhizuku.api)
    implementation(libs.dhizuku.server.api)
    implementation(libs.androidx.fragment)
    implementation(libs.hiddenApiBypass)
    implementation(libs.libsu)
    implementation(libs.serialization)
    implementation(kotlin("reflect"))
}