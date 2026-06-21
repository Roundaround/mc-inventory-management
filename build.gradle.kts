plugins {
  id("me.roundaround.allay")
}

allay {
  displayName.set("Inventory Management")
  description.set("Sort and transfer items with the click of a button.")
  authors.set(listOf("Roundaround"))
  license.set("MIT")
  homepage.set("https://modrinth.com/mod/inventory-management")
  repository.set("https://github.com/Roundaround/mc-inventory-management")
  issues.set("https://github.com/Roundaround/mc-inventory-management/issues")
  logoFile.set("assets/inventorymanagement/banner.png")

  gametest {
    timeoutSeconds.set(10)
  }

  modrinth {
    projectId.set("inventory-management")
  }

  curseforge {
    projectId.set(1293402)
  }

  release {
    versionType.set("release")
    minecraftVersions("26.2")
    changelogDir.set(file("changelogs"))
  }
}
