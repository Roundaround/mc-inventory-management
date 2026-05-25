plugins {
  id("me.roundaround.allay")
}

repositories {
  mavenLocal()
}

dependencies {
  libBundle(platform(libs.trove.bom))
  libBundle(libs.trove.forge.core)
  libBundle(libs.trove.config.gui)
}
