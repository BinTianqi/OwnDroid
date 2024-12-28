plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose)
    kotlin("plugin.serialization") version "2.0.21"
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
    compileSdk = 34

    lint.checkReleaseBuilds = false
    lint.disable += "All"

    defaultConfig {
        applicationId = "com.bintianqi.owndroid"
        minSdk = 21
        targetSdk = 34
        versionCode = 34
        versionName = "6.2"
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
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    kotlinOptions {
        jvmTarget = "20"
    }
    buildFeatures {
        compose = true
        aidl = true
    }
    androidResources {
        generateLocaleConfig = true
    }
}

kotlin {
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
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.shizuku.provider)
    implementation(libs.shizuku.api)
    implementation(libs.dhizuku.api)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.fragment)
    implementation(libs.hiddenApiBypass)
    implementation(libs.serialization)
    implementation(kotlin("reflect"))
}