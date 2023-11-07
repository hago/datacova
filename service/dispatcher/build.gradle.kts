plugins {
    application
    id("java")
}

group = "com.hagoapp.datacova"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":libcova"))
    implementation(project(":message"))
    implementation(project(":utility"))
    implementation("org.slf4j", "slf4j-api", "2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
