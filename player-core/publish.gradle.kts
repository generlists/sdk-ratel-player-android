
extensions.configure<PublishingExtension>("publishing") {
    publications {
        create<MavenPublication>("release") {
            groupId = "ai.shortform-play.sdk.ratel.player.android"
            artifactId = "player-core"
            version = "0.0.0.2"

            afterEvaluate {
                from(components["release"])
            }

        }
    }
    val localProperties = java.util.Properties()
    val localFile = rootProject.file("local.properties")

    if (localFile.exists()) {
        localFile.inputStream().use { localProperties.load(it) }
    }


    repositories {
        maven {
            name = "AndroidPlayerSDKPackage"
            url = uri("https://maven.pkg.github.com/generlists/sdk-ratel-player-android")

            credentials {
                username = localProperties.getProperty("gpr.user") ?: System.getenv("GPR_USER")
                password = localProperties.getProperty("gpr.key")  ?: System.getenv("GPR_TOKEN")
            }
        }
    }
}
