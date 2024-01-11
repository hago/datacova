plugins {
    id("java-library")
}

group = "com.hagoapp.datacova"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.reflections", "reflections", "0.10.2")
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.4.12")
    testRuntimeOnly("ch.qos.logback:logback-core:1.4.12")
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}
tasks.withType(Javadoc::class.java).configureEach {
    options.encoding = "UTF-8"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}