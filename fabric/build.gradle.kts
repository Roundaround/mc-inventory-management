plugins {
  id("me.roundaround.allay")
}

repositories {
  mavenLocal()
}

allay {
  modrinth {
    dependencies {
      required("fabric-api")
    }
  }
}

dependencies {
  libBundle(platform(libs.trove.bom))
  libBundle(libs.trove.fabric.core)
  libBundle(libs.trove.config.gui)

  testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations {
  testCompileOnly { extendsFrom(configurations.compileOnly.get()) }
  testRuntimeOnly { extendsFrom(configurations.runtimeOnly.get()) }
}

tasks.test {
  useJUnitPlatform()
  workingDir = file("run")
}
