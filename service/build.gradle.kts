plugins {
    java
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version("1.7.20")
}

group = "org.hagoapp.datacova"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}