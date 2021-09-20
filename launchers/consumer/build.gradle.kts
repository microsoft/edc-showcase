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
    implementation(project(":extensions:identity-hub-verifier"))
    implementation(project(":extensions:ion-client-mock"))
    implementation(project(":extensions:verifiable-credentials"))
    implementation(project(":extensions:distributed-identity-service"))

    implementation("${group}:core:${edcversion}")
    implementation("${group}:in-mem.process-store:${edcversion}")

    implementation("${group}:azure.vault:${edcversion}")
    implementation("${group}:in-mem.policy-registry:${edcversion}")
    implementation("${group}:in-mem.metadata:${edcversion}")
    implementation("${group}:in-mem.identity-hub:${edcversion}")
    implementation("${group}:in-mem.did-document-store:${edcversion}")

    implementation("${group}:filesystem.configuration:${edcversion}")
    implementation("${group}:aws.s3.provision:${edcversion}")
    implementation("${group}:aws.s3.provision:${edcversion}")

    implementation("${group}:data-protocols.ids:${edcversion}")
    implementation("${group}:data-protocols..ids-policy-mock:${edcversion}")
    implementation("${group}:data-protocols.ion:${edcversion}")

    implementation("${group}:iam.identity-did-spi:${edcversion}")
    implementation("${group}:iam.identity-did-core:${edcversion}")


    //    implementation(":extensions:iam:distributed-identity:identity-did-service")
    //    implementation(":extensions:iam:distributed-identity:identity-did-spi")
    //    implementation(":extensions:iam:distributed-identity:identity-did-core")
    //    implementation(":samples:other:identity-gaiax-verifier")
    //    implementation(":samples:other:public-rest-api")

}

application {
    @Suppress("DEPRECATION")
    mainClassName = "com.microsoft.ion.consumer.ConsumerRuntime"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("consumer.jar")
}

