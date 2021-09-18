/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

plugins {
    `java-library`
    id("application")
}

val edcversion: String by project

dependencies {
    implementation("org.eclipse.dataspaceconnector:core:${edcversion}")
    implementation("org.eclipse.dataspaceconnector:in-mem.process-store:${edcversion}")

//    // TODO HACKATHON-1 TASK 6A Commented out until private keys placed in Azure Vault
//    implementation(":extensions:azure:vault")
//    implementation(":extensions:in-memory:policy-registry-memory")
//    implementation(":extensions:in-memory:metadata-memory")
//    implementation(":extensions:filesystem:configuration-fs")
//    implementation(":extensions:aws:s3:provision")
//
//    implementation(":data-protocols:ids")
//    implementation(":data-protocols:ids:ids-policy-mock")
//
//
//    implementation(":extensions:iam:distributed-identity:identity-did-service")
//    implementation(":extensions:iam:distributed-identity:identity-did-spi")
//    implementation(":extensions:iam:distributed-identity:identity-did-core")
//    implementation(":extensions:in-memory:identity-hub-memory")
//    implementation(":samples:other:identity-gaiax-verifier")
//
//    implementation(":samples:other:public-rest-api")

}
