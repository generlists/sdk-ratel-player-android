pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
val localProperties = java.util.Properties()
val localFile = File(rootDir, "local.properties")

if (localFile.exists()) {
    localFile.inputStream().buffered().use { stream ->
        localProperties.load(stream)
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/generlists/sdk-ratel-player-android")
            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = localProperties.getProperty("gpr.key") ?: System.getenv("GPR_TOKEN")
            }
        }
    }

}

rootProject.name = "sdk-ratel-android"
include(":android-youtube-player")
include(":app")
include(":player-core")
include(":player-utils")
include(":player-ui")
//include(":player-ad")
