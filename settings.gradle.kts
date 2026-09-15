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

// neoforge + forge deferred: no upstream 26.3 build yet (their subprojects stay in-tree).
listOf("fabric").forEach { loader ->
    val path = ":$loader"
    include(path)
    project(path).projectDir = file(loader)
}
