import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.devtools.ksp)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ktlint)
}
ktlint {
    android.set(true)
    outputToConsole.set(true)
    enableExperimentalRules.set(true)
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    debug.set(true)
}

android {
    namespace = "com.sean.ratel.player.demo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sean.ratel.player.demo"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    val localProperties = Properties()
    val localFile = rootProject.file("local.properties")

    if (localFile.exists()) {
        localFile.inputStream().use { localProperties.load(it) }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "TEST_DEVICE_HASHED_ID",
                "\"${localProperties.getProperty("TEST_DEVICE_HASHED_ID")}\"",
            )
            buildConfigField(
                "String",
                "BANNER_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "NATIVE_AD_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_NATIVE_AD_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "ADAPTIVE_BANNER_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_ADAPTIVE_BANNER_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "INTERSTITIALAd_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_INTERSTITIALAd_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "Ad_OPEN_UNIT_ID",
                "\"${localProperties.getProperty("DEBUG_Ad_OPEN_UNIT_ID")}\"",
            )
            buildConfigField(
                "String",
                "admobAppId",
                "\"${localProperties.getProperty("debug_admobAppId")}\"",
            )
            manifestPlaceholders["validator"] = "false"

            manifestPlaceholders["admobAppId"] =
                localProperties.getProperty(
                    "debug_admobAppId",
                    "ca-app-pub-3940256099942544~3347511713",
                )

            buildConfigField(
                "String",
                "PEXABAY_API_KEY",
                "\"${localProperties.getProperty("PEXABAY_API_KEY")}\"",
            )
        }

        release {
            isMinifyEnabled = false
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
        compose = true
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(project(":player-core"))
    implementation(project(":player-ui"))
    implementation(project(":player-utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.google.code.gson)
    implementation(libs.androidx.multidex.multidex)
    implementation(libs.androidx.lifecycle.process)

    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.com.google.accompanist.webview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
}
