import java.util.Properties
// Top-level build file where you can add configuration options common to all sub-projects/modules.
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

    // 모듈별로 다른 버전 할당
    version =
        when (name) {
            "player-core" -> "${versionProps["CORE_VERSION"]}$suffix"
            "player-utils" -> "${versionProps["UTIL_VERSION"]}$suffix"
            "player-ui" -> "${versionProps["UI_VERSION"]}$suffix"
            "android-youtube-player" -> "${versionProps["YOUTUBE_PLAYER_VERSION"] ?: "1.0.0"}$suffix"
            else -> versionProps["VERSION_NAME"].toString() // 기본값
        }
}

tasks.register("publishAll") {
    group = "publishing"
    description = "수정된(UPDATED=true) 모듈만 순서대로 깃허브에 배포합니다."

    // 1. 별도의 프로퍼티 파일 로드 로직
    val vProps = java.util.Properties()
    val vPropsFile = project.rootProject.file("version.properties") // 파일명 확인!

    if (vPropsFile.exists()) {
        vPropsFile.inputStream().use { vProps.load(it) }
        println(">>> [SUCCESS] version.properties 파일을 성공적으로 로드했습니다.")
    } else {
        println(">>> [ERROR] version.properties 파일을 찾을 수 없습니다! 경로: ${vPropsFile.absolutePath}")
    }
    // 1. 배포할 모듈 폴더명과 properties의 접두어 매핑
    val modules =
        mapOf(
            ":android-youtube-player" to "YOUTUBE_PLAYER",
            ":player-utils" to "UTIL",
            ":player-core" to "CORE",
            ":player-ui" to "UI",
        )

    // 2. UPDATED가 true인 모듈만 찾아서 배포 태스크를 dependsOn에 추가
    modules.forEach { (path, prefix) ->
        // 모든 모듈에 대해 일단 값을 다 찍어봅니다.
        println(">>> [DEBUG] 모듈: $path, 찾는 키: $prefix")

        val propName = "${prefix}_UPDATED"
        val version = vProps.getProperty(prefix)
        val rawValue = vProps.getProperty(propName) // 여기서 직접 꺼냄
        val isUpdated = rawValue?.trim()?.toBoolean() ?: false

        println(">>> [CHECK] Key: $propName | Value: $rawValue | Result: $isUpdated , version :$version")

        if (isUpdated) {
            // 이 구문이 태스크 등록 시점에 실행되도록 확실히 보장합니다.
            this.dependsOn("$path:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
        }
    }

    // 3. 순서 제어 로직 (그대로 유지)
    // 이 로직은 태스크가 '실행 목록'에 있을 때만 작동하므로,
    // UPDATED=false 인 놈들은 무시하고 넘어갑니다.
    project(":player-utils").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(":android-youtube-player:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }
    project(":player-core").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(":player-utils:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }
    project(":player-ui").tasks.named("publishReleasePublicationToAndroidPlayerSDKPackageRepository") {
        mustRunAfter(":player-core:publishReleasePublicationToAndroidPlayerSDKPackageRepository")
    }
}
