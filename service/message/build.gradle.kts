plugins {
    id("java")
}

group = "com.hagoapp.datacova.message"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("org.reflections", "reflections", "0.10.2")
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
