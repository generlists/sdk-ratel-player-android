plugins {
    id("com.android.library")
    id("kotlin-android")
}

apply(from = "publish.gradle")

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 17
        targetSdk = 34
    }

    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res")
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

    namespace = "com.pierfrancescosoffritti.androidyoutubeplayer.core"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    testImplementation(libs.junit)
//    androidTestImplementation(libs.test.runner)
//    androidTestImplementation(libs.androidx.espresso.core)

    api(libs.androidx.lifecycle.runtime.ktx)
}
