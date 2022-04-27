/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
    application
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {
    // EDC core dependencies
    implementation("${group}:core:${edcversion}")

    implementation("${group}:common-util:${edcversion}")
    implementation("${group}:azure-eventgrid-config:${edcversion}")

    implementation("${group}:registration-service:${edcversion}")
    implementation("${group}:registration-service-api:${edcversion}")
}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.ion.demo.RegistrationServiceRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("regsvc.jar")
}

