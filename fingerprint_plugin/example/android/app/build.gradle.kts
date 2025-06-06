plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.fingerprint_plugin_example"
    compileSdk = 34
    ndkVersion = "27.0.12077973"
    
    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    sourceSets["main"].java.srcDirs("src/main/kotlin")

    defaultConfig {
        applicationId = "com.example.fingerprint_plugin_example"
        minSdk = 23  // Minimum pour la biométrie
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        
        // Spécifier que nous utilisons la biométrie forte
        manifestPlaceholders["authMethod"] = "fingerprint"
        buildConfigField("boolean", "USE_BIOMETRIC", "true")
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

flutter {
    source = "../.."
}
