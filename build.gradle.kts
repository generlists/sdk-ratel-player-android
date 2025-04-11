// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.devtools.ksp)  apply  false
}

tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes player-utils first, then player-core to GitHub Packages"

    dependsOn(
         ":android-youtube-player:assembleRelease",
        ":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-utils:assembleRelease",
        ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-core:assembleRelease",
        ":player-core:publishReleasePublicationToAndroidPlayerSDKPackageRepository"
    )

    project(":player-core").tasks.named("assembleRelease") {
        mustRunAfter(":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }
    project(":player-core").tasks.named("assembleRelease") {
        mustRunAfter(":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }

    project(":player-core").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }
}