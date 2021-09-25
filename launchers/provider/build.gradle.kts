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
    // dependencies from this project
    implementation(project(":extensions:identity-hub-verifier"))
    implementation(project(":extensions:ion-client-mock"))
    implementation(project(":extensions:verifiable-credentials"))
    implementation(project(":extensions:distributed-identity-service"))

    implementation(project(":extensions:dataseeding:catalog"))
    implementation(project(":extensions:dataseeding:hub"))

    // EDC core dependencies
    implementation("${group}:core:${edcversion}")
    implementation("${group}:in-mem.process-store:${edcversion}")

    // ids
    implementation("${group}:data-protocols.ids-policy-mock:${edcversion}")
    implementation("${group}:data-protocols.ids:${edcversion}")

    // simple in-memory and filesystem implementations
    implementation("${group}:in-mem.policy-registry:${edcversion}")
    implementation("${group}:in-mem.metadata:${edcversion}")
    implementation("${group}:in-mem.identity-hub:${edcversion}")
    implementation("${group}:in-mem.did-document-store:${edcversion}")
    implementation("${group}:filesystem.configuration:${edcversion}")

    //cloud stuff
    implementation("${group}:azure.vault:${edcversion}")
    implementation("${group}:aws.s3.provision:${edcversion}")
    implementation("${group}:aws.s3.provision:${edcversion}")

    // distributed identity stuff
    implementation("${group}:data-protocols.ion:${edcversion}")
    implementation("${group}:iam.identity-did-spi:${edcversion}")
    implementation("${group}:iam.identity-did-core:${edcversion}")


}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.ion.provider.ProviderRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("provider.jar")
}

