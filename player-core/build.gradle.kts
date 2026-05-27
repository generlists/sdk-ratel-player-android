apply(from = "publish.gradle.kts")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.signing)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sean.ratel.player.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        debug{
            manifestPlaceholders["IS_DEBUG"] = true
            manifestPlaceholders["IS_DEBUG_UDP"] = false
            manifestPlaceholders["IS_DEBUG_LINK_SOURCE"] = false
        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders["IS_DEBUG"] = false
            manifestPlaceholders["IS_DEBUG_UDP"] = false
            manifestPlaceholders["IS_DEBUG_LINK_SOURCE"] = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    api(project(":android-youtube-player"))
    implementation(project(":player-utils"))
    implementation(libs.so.smartlab.sdk.common.utils.android )

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.media3.transformer)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)

    implementation(libs.google.media3.exoplayer)

    implementation(libs.google.media3.exoplayer.dash)
    implementation(libs.google.media3.exoplayer.hls)
    implementation(libs.google.media3.datasource.okhttp)
    implementation(libs.google.media3.database)
    implementation(libs.google.media3.ui)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}
