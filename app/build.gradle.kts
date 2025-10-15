plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose")
    // Keeping 'kotlin-kapt' as per your original configuration for Room.
    id("kotlin-kapt")
}

android {
    namespace = "com.kiprono.mamambogaqrapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kiprono.mamambogaqrapp"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        // Set JVM target to match compileOptions sourceCompatibility
        jvmTarget = "17"
        // Keeping languageVersion at 1.9 due to the Kapt warning, though usually it can be omitted.
        languageVersion = "1.9"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Desugaring for modern Java APIs
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // Core + Lifecycle (Using stable, existing versions)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")

    // Jetpack Compose BOM (Using stable, existing version)
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Extended Icons (The correct artifact name)
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation for Compose (Using stable, existing version)
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // CameraX (Using stable, existing version)
    val cameraXVersion = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraXVersion")
    implementation("androidx.camera:camera-camera2:$cameraXVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraXVersion")
    implementation("androidx.camera:camera-view:$cameraXVersion")

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // Accompanist Permissions (Using stable, existing version)
    val accompanistVersion = "0.34.0"
    implementation("com.google.accompanist:accompanist-permissions:$accompanistVersion")

    // Room Database (Using latest stable alpha version and consistent versions)
    val roomVersion = "2.7.0-alpha01"
    implementation("androidx.room:room-runtime:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    // Lifecycle ViewModel + LiveData (Using consistent version 2.8.2)
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")

    // Animation & Effects (Konfetti)
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")

    // Date/Time (ThreeTenABP)
    implementation("com.jakewharton.threetenabp:threetenabp:1.4.6")

    // Accompanist Swipe Refresh (Using consistent version 0.34.0)
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanistVersion")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
