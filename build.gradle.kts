plugins {
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.70"
}

group = "com.lduncandroid"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    testImplementation("io.kotest:kotest-runner-junit5-jvm:4.1.2") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core-jvm:4.1.2") // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property-jvm:4.1.2") // for kotest property test
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.20.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}