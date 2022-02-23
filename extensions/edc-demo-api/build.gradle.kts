/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *
 */

plugins {
    `java-library`
}

val edcversion: String by project
val rsApi: String by project
val group = "org.eclipse.dataspaceconnector"
dependencies {
    implementation("${group}:spi:${edcversion}")
    implementation("${group}:ids-spi:${edcversion}")
    implementation("${group}:common-util:${edcversion}")
    implementation("${group}:federated-catalog-spi:${edcversion}")
    implementation("${group}:blob-core:${edcversion}")
    implementation("${group}:dataloading:${edcversion}")

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:${rsApi}")

}
