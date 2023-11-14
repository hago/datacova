plugins {
    id("java-library")
    id("jacoco")
    id("org.sonarqube") version "4.3.1.3277"
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
}

group = "com.hagoapp.dataccova"
version = "0.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":filestore"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
    implementation("com.fasterxml.jackson.jr:jackson-jr-objects:2.15.3")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.slf4j", "slf4j-api", "2.0.9")
    implementation("org.apache.directory.api:api-all:2.1.5")
    api("org.apache.directory.api:api-all:2.1.5")
    implementation("redis.clients:jedis:5.0.2")
    api("redis.clients:jedis:5.0.2")
    implementation("com.github.albfernandez:juniversalchardet:2.4.0")
    implementation("com.sun.mail", "javax.mail", "1.6.2")
    implementation("org.freemarker", "freemarker", "2.3.32")
    api("org.freemarker", "freemarker", "2.3.32")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testCompileOnly("org.projectlombok:lombok:1.18.30")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.30")
    testImplementation("ch.qos.logback:logback-classic:1.4.11")
    testImplementation("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

kotlin {
    jvmToolchain(11)
}

@Suppress("unchecked_cast")
tasks.test {
    useJUnitPlatform()
    systemProperties(System.getProperties() as Map<String, Any>)
}
