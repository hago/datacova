plugins {
    id("java")
}

group = "com.lenovo.led.util"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.15.3")
    implementation("com.google.code.gson:gson:2.9.0")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("ch.qos.logback", "logback-classic","1.2.9")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}
