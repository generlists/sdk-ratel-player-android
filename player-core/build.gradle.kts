import java.io.FileInputStream
import java.util.Properties

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


var versionPropsFile = file("version.properties")
val versionProps: Properties = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
}


android {
    namespace = "com.sean.ratel.player.core"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "VERSION_NAME", "\"" + versionProps["VERSION_NAME"] + "\"")
        buildConfigField("String", "LIB_NAME", "\"" + versionProps["LIB_NAME"] + "\"")
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
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