plugins {
    id("java")
}

group = "com.hagoapp.datacova"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utility"))
    implementation(project(":surveyor"))
    implementation(project(":filestore"))
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.hagoapp:f2t:0.8.3")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.15.3")
    implementation("org.postgresql", "postgresql", "42.5.1")
    implementation("commons-net", "commons-net", "3.9.0")
    implementation("com.jcraft", "jsch", "0.1.55")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("ch.qos.logback", "logback-classic", "1.2.9")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

kotlin {
    jvmToolchain(11)
}
