import java.io.FileInputStream
import java.util.Properties

apply(from = "publish.gradle.kts")
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.signing)
}

var versionPropsFile = file("version.properties")
val versionProps: Properties = Properties()

if (versionPropsFile.canRead()) {
    versionProps.load(FileInputStream(versionPropsFile))
}

android {
    namespace = "com.sean.ratel.player.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "VERSION_NAME", "\""+versionProps["VERSION_NAME"]+ "\"")
        buildConfigField("String", "LIB_NAME",  "\""+versionProps["LIB_NAME"]+ "\"")
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
        compose =true
    }
}

dependencies {

    implementation(project(":player-core"))
    implementation(project(":player-utils"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.material3)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.material3.icons.core)
    implementation(libs.google.media3.ui )
    implementation(libs.google.media3.exoplayer)
    implementation(libs.androidx.foundation)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.coil)
    implementation(libs.coil.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}