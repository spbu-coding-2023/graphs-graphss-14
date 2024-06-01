plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.2"
    id("jacoco")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport{
    dependsOn(tasks.test)
    reports{
        xml.required.set(true)
        html.required.set(true)
    }
}

jacoco {
    toolVersion = "0.8.12"
}

kotlin {
    jvmToolchain(21)
}