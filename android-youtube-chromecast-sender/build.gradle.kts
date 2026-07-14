plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}
apply(from = "publish.gradle")
android {
    namespace = "com.pierfrancescosoffritti.androidyoutubeplayer.chromecast.chromecastsender"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildTypes {
        debug {
            manifestPlaceholders["IS_DEBUG"] = true
            manifestPlaceholders["IS_DEBUG_UDP"] = false
            manifestPlaceholders["IS_DEBUG_LINK_SOURCE"] = true
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
}

dependencies {
    implementation(project(":android-youtube-player"))
    implementation(libs.so.smartlab.sdk.common.utils.android)
    implementation(libs.google.android.gms.play.service.cast.framework)
    implementation(libs.androidx.mediarouter)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
