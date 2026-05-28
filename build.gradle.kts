import java.util.Properties

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.devtools.ksp) apply false
    alias(libs.plugins.ktlint)
}

val versionProps =
    Properties().apply {
        load(rootProject.file("version.properties").inputStream())
    }

val snapshot = versionProps["SNAPSHOT"].toString().toBoolean()
val suffix = if (snapshot) "-SNAPSHOT" else ""

subprojects {

    group = versionProps["GROUP"].toString()

    version =
        when (name) {

            // SNAPSHOT 사용
            "player-core" -> {
                "${versionProps["CORE_VERSION"]}$suffix"
            }

            "player-utils" -> {
                "${versionProps["UTIL_VERSION"]}$suffix"
            }

            "player-ui" -> {
                "${versionProps["UI_VERSION"]}$suffix"
            }

            // RELEASE 고정 (SNAPSHOT 제거)
            "android-youtube-player" -> {
                versionProps["YOUTUBE_PLAYER_VERSION"].toString()
            }

            else -> {
                "1.0.0"
            }
        }

    println(">>> module=$name version=$version")
}

tasks.register("publishAll") {

    group = "publishing"
    description = "수정된(UPDATED=true) 모듈만 순서대로 배포"

    val vProps =
        Properties().apply {
            load(project.rootProject.file("version.properties").inputStream())
        }

    val modules =
        mapOf(
            ":android-youtube-player" to "YOUTUBE_PLAYER",
            ":player-utils" to "UTIL",
            ":player-core" to "CORE",
            ":player-ui" to "UI",
        )

    modules.forEach { (path, prefix) ->

        val updatedKey = "${prefix}_UPDATED"
        val isUpdated =
            vProps
                .getProperty(updatedKey)
                ?.trim()
                ?.toBoolean()
                ?: false

        println(">>> $path updated=$isUpdated")

        if (isUpdated) {
            dependsOn(
                "$path:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            )
        }
    }

    project(":player-utils")
        .tasks
        .named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
            mustRunAfter(
                ":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            )
        }

    project(":player-core")
        .tasks
        .named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
            mustRunAfter(
                ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            )
        }

    project(":player-ui")
        .tasks
        .named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
            mustRunAfter(
                ":player-core:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            )
        }
}
