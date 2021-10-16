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
    implementation(project(":extensions:public-rest-api"))
    implementation(project(":extensions:dataseeding:catalog"))
    implementation(project(":extensions:dataseeding:hub"))
    implementation(project(":extensions:transfer-azure-s3"))
    implementation(project(":extensions:identity-hub-verifier"))

    // EDC core dependencies
    implementation("${group}:core:${edcversion}")
    implementation("${group}:core.schema:${edcversion}")
    implementation("${group}:in-memory.process-store:${edcversion}")

    // ids
    implementation("${group}:data-protocols.ids-policy-mock:${edcversion}")
    implementation("${group}:data-protocols.ids:${edcversion}")

    // simple in-memoryory and filesystem implementations
    implementation("${group}:in-memory.policy-registry:${edcversion}")
    implementation("${group}:in-memory.metadata:${edcversion}")
    implementation("${group}:in-memory.identity-hub:${edcversion}")
    implementation("${group}:in-memory.did-document-store:${edcversion}")
    implementation("${group}:filesystem.configuration:${edcversion}")

    //cloud stuff
    implementation("${group}:azure.vault:${edcversion}")
    implementation("${group}:aws.s3.provision:${edcversion}")

    // distributed identity stuff
    implementation("${group}:ion.ion-core:${edcversion}")
    implementation("${group}:ion.ion-client:${edcversion}")
    implementation("${group}:iam.identity-did-spi:${edcversion}")
    implementation("${group}:iam.identity-did-core:${edcversion}")
    implementation("${group}:iam.identity-did-service:${edcversion}")
    implementation("${group}:iam.verifiable-credentials:${edcversion}")

}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.ion.connector.Runtime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("connector.jar")
}

