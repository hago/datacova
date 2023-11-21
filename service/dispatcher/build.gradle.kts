import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

group = "com.hagoapp.datacova"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation(project(":libcova"))
    implementation(project(":message"))
    implementation(project(":utility"))
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    runtimeOnly("ch.qos.logback", "logback-classic", "1.4.11")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("ch.qos.logback", "logback-classic", "1.4.11")
}

application {
    mainClass.set("com.hagoapp.datacova.dispatcher.Application")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

tasks.withType<Jar>() {
    isZip64 = true
    manifest {
        attributes(mapOf("Main-Class" to "com.hagoapp.datacova.dispatcher.Application"))
    }
}

tasks.withType<ShadowJar>() {
    isZip64 = true
}
