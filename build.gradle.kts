// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.devtools.ksp)  apply  false
}


tasks.register("publishAll") {
    group = "publishing"
    description = "Publishes player-utils first, then player-core to GitHub Packages"

    dependsOn(
        ":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-core:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-ui:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
        ":player-ad:publishReleasePublicationToAndroidPlayerSDKPackageRepository"
    )

    project(":player-utils").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }

    project(":player-core").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(
            ":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository"
        )
    }

    project(":player-ui").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(
            ":player-core:publishReleasePublicationToAndroidPlayerSDKPackageRepository",
            ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository"
        )
    }

    project(":player-ad").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(
            ":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository"
        )
    }
}