import java.util.Properties

plugins {
    // No org.jetbrains.kotlin.android here: AGP 9 has built-in Kotlin support and
    // rejects the standalone plugin. The Compose compiler plugin is still separate.
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Signing credentials live in keystore.properties, which is gitignored and never
// committed. Without that file the project still builds — release just stays unsigned —
// so a fresh clone is not broken by its absence. See PLAY_STORE.md.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.plakaneresi.app"
    // 37 is what is installed locally; AGP 9.3 supports up to API 37.
    compileSdk = 37

    defaultConfig {
        applicationId = "com.plakaneresi.app"
        // 26 so the launcher icon can be a pure-XML adaptive icon (no bitmap assets).
        minSdk = 26
        // Google Play requires new submissions to target API 36 from 31 Aug 2026.
        targetSdk = 36
        // Play permanently rejects a reused versionCode, even for a bundle that was
        // uploaded and then discarded, and it never reports which numbers are taken.
        // 1-3 were spent during internal testing; jumping clear is free, since gaps in
        // the sequence mean nothing to Play. Only ever increase this.
        versionCode = 10
        versionName = "1.0.2"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        // Off by default since AGP 8; the details sheet reads BuildConfig.VERSION_NAME.
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)

    implementation(libs.play.services.ads)

    debugImplementation(libs.androidx.ui.tooling)

    testImplementation(libs.junit)
}
