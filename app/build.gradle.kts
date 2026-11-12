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
        versionCode = 4
        versionName = "1.3"
    }

    // Fixed key so every build is signed the same way and updates install over each other
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
            isMinifyEnabled = false
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
