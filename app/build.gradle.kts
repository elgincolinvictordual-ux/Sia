plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.theunknown.siasgrades"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.theunknown.siasgrades"
        minSdk = 26
        targetSdk = 34
        versionCode = 9
        versionName = "1.8"
    }

    signingConfigs {
        create("main") {
            storeFile = file("sias.keystore")
            storePassword = "siasgrades"
            keyAlias = "sias"
            keyPassword = "siasgrades"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("main")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}
