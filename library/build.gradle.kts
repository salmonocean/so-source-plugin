plugins {
  `kotlin-dsl`
  `java-gradle-plugin`
}

gradlePlugin {
  plugins {
    create("so-source-plugin") {
      id = "so-source"
      implementationClass = "com.evergreen.android.SoSourcePlugin"
    }

    // @see [GradlePluginDevelopmentExtension]
    isAutomatedPublishing = false
  }
}

repositories {
  google()
  mavenCentral()
  jcenter()
}

sourceSets {
  main {
    java.srcDir("src/main/kotlin")
  }
}

dependencies {
  compileOnly("com.android.tools.build:gradle:3.4.2")
}

apply(from = rootProject.file("gradle/mvn-publish.gradle"))