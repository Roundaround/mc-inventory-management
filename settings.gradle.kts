pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    plugins {
        id("me.roundaround.allay") version "0.1.0-SNAPSHOT"
    }
}

rootProject.name = "inventorymanagement"

listOf("fabric", "neoforge", "forge").forEach { loader ->
    val path = ":$loader"
    include(path)
    project(path).projectDir = file(loader)
}
