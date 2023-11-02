import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("java")
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

group = "com.lenovo.led.worker"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":libcova"))
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.reflections", "reflections", "0.10.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("ch.qos.logback", "logback-classic", "1.2.9")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.hagoapp.datacova.worker.Application")
}

tasks.withType<Jar>() {
    isZip64 = true
    manifest {
        attributes(mapOf("Main-Class" to "com.hagoapp.datacova.worker.Application"))
    }
}


tasks.withType<ShadowJar>() {
    isZip64 = true
}
