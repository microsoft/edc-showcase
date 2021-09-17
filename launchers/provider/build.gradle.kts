/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

val edcversion: String by project

dependencies {
    implementation("org.eclipse.dataspaceconnector:core:${edcversion}")
    implementation("org.eclipse.dataspaceconnector:in-mem.process-store:${edcversion}")
    implementation("org.eclipse.dataspaceconnector:azure.vault:${edcversion}")

//    implementation(project(":extensions:in-memory:policy-registry-memory"))
//    implementation(project(":extensions:in-memory:metadata-memory"))
//    implementation(project(":extensions:filesystem:configuration-fs"))
//
    implementation("org.eclipse.dataspaceconnector:data-protocols.ids:${edcversion}")
//    implementation(project(":data-protocols:ids:ids-policy-mock"))
//
//    implementation(project(":samples:other:copy-between-azure-and-s3"))
//
//    implementation(project(":samples:other:public-rest-api"))
//    implementation(project(":extensions:iam:distributed-identity:identity-did-service"))
//    implementation(project(":extensions:iam:distributed-identity:identity-did-spi"))
//    implementation(project(":extensions:iam:distributed-identity:identity-did-core"))
//    implementation(project(":extensions:in-memory:identity-hub-memory"))
//    implementation(project(":samples:other:identity-gaiax-verifier"))


}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.ion-demo.ProviderRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}
