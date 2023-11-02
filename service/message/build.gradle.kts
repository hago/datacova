plugins {
    id("java")
}

group = "com.hagoapp.datacova.message"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":utility"))
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
