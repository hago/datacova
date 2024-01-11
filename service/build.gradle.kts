import org.jetbrains.kotlin.config.JvmTarget

plugins {
    java
    kotlin("jvm") version "1.8.22"
    id("org.jetbrains.dokka") version ("1.8.20")
    id("jacoco")
    id("org.sonarqube") version "4.3.1.3277"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test)
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.22")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.22")

    testImplementation(kotlin("test", "1.8.22"))
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.jetbrains.dokka").apply { version = "1.8.20" }
    apply(plugin = "kotlin").apply { version = "1.8.22" }
    dependencies {
        implementation("com.google.code.gson:gson:2.10.1")
        testImplementation(kotlin("test", "1.8.22"))
    }
    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        target {
            JvmTarget.JVM_11
        }
    }
}

group = "org.hagoapp.datacova"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()

}
