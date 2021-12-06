/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
}

val group = "org.eclipse.dataspaceconnector"
val edcversion: String by project
val jupiterVersion: String by project

dependencies {
    implementation(project(":extensions:dataseeding:hub"))

    testImplementation("${group}:spi:${edcversion}")
    testImplementation("${group}:core.bootstrap:${edcversion}")
    testImplementation("${group}:core.transfer:${edcversion}")
    testImplementation("${group}:core.protocol-web:${edcversion}")
    testImplementation("${group}:transfer-process-store-memory:${edcversion}")
    testImplementation("${group}:policy-registry-memory:${edcversion}")
    testImplementation("${group}:in-memory.metadata:${edcversion}")
    testImplementation("${group}:in-memory.identity-hub:${edcversion}")
    testImplementation("${group}:ion.ion-core:${edcversion}")
    testImplementation("${group}:ion.ion-client:${edcversion}")

    testImplementation("${group}:ids-policy-mock:${edcversion}")
    testImplementation("${group}:ids:${edcversion}")
    testImplementation("${group}:iam.identity-did-core:${edcversion}")
    testImplementation("${group}:iam.identity-did-service:${edcversion}")
    testImplementation("${group}:iam.verifiable-credentials:${edcversion}")
    testImplementation(project(":extensions:identity-hub-verifier"))


    testImplementation("${group}:dataspaceconnector.junit.launcher:${edcversion}")
    testImplementation("${group}:common-util:${edcversion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}

