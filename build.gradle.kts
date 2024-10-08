/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    `java-library`
    `maven-publish`
    application
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

dependencies {
    api(libs.org.openpnp.opencv)
    api(libs.commons.cli.commons.cli)
    api(libs.com.google.code.gson.gson)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

group = "me.seyfu-t"
version = "1.0-Pre-1"
description = "JBarcode_1D"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/seyfu-t/JBarcode_1D")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

// Task to create a fat JAR that bundles all dependencies
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all") // This will create a file like `jbarcode_1d-1.0-Pre-1-all.jar`
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE // Exclude duplicate files

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

application {
    mainClass.set("me.seyfu_t.JBarcode_1D.JBarcode_1D")
}
