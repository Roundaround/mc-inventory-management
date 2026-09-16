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

// forge deferred: no upstream 26.3 build yet (its subproject stays in-tree).
listOf("fabric", "neoforge").forEach { loader ->
    val path = ":$loader"
    include(path)
    project(path).projectDir = file(loader)
}
