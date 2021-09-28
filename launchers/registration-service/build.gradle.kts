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
    implementation("${group}:in-mem.process-store:${edcversion}")

    implementation("${group}:core.bootstrap:${edcversion}")
    implementation("${group}:dataspaceconnector.common.util:${edcversion}")
    implementation("${group}:core.protocol-web:${edcversion}")
    implementation("${group}:azure.events-config:${edcversion}")
    implementation("${group}:data-protocols.ion-client:${edcversion}")
    implementation("${group}:iam.registration-service:${edcversion}")
    implementation("${group}:iam.registration-service-api:${edcversion}")
    implementation("${group}:in-mem.identity-hub:${edcversion}")
    implementation("${group}:in-mem.did-document-store:${edcversion}")

    // todo: replace this with a finer-grained dependency structure. we don't need VCs here!!
    implementation("${group}:iam.verifiable-credentials:${edcversion}")

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

