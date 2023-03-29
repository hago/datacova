group = "com.hagoapp"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.36")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.9")
    testRuntimeOnly("ch.qos.logback:logback-core:1.2.9")
}

tasks.withType(JavaCompile::class).configureEach {
    options.encoding = "UTF-8"
}
tasks.withType(Javadoc::class.java).configureEach {
    options.encoding = "UTF-8"
}

kotlin {
    jvmToolchain(11)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}