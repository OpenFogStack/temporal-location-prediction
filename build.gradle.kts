import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.21"
}

group = "me.mbe.prp"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    jcenter()
}

dependencies {

    implementation("com.floern.castingcsv:casting-csv-kt:1.1")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}