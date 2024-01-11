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
    implementation(project(":utility"))
    implementation(project(":libcova"))
    implementation(project(":message"))
    implementation(project(":filestore"))
    implementation(project(":surveyor"))
    implementation("com.hagoapp:f2t:0.8.3")
    implementation("info.picocli:picocli:4.7.5")
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("ch.qos.logback", "logback-classic", "1.4.12")
    implementation("org.antlr", "ST4", "4.3.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("ch.qos.logback", "logback-classic", "1.4.12")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
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
