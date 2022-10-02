import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation ("org.apache.jena:apache-jena-libs:3.16.0")
    implementation ("org.apache.jena:jena-core:3.16.0")
    implementation ("net.sourceforge.owlapi:org.semanticweb.hermit:1.4.5.519")
    implementation("com.github.owlcs:ontapi:2.1.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}