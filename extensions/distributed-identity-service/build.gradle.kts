plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

val nimbusVersion: String by project
dependencies {

    api(project(":extensions:verifiable-credentials"))
    implementation("${group}:spi:${edcversion}")
    implementation("${group}:data-protocols.ion-core:${edcversion}")
    implementation("${group}:iam.identity-did-spi:${edcversion}")

    api("com.nimbusds:nimbus-jose-jwt:${nimbusVersion}")
    // this is required for the JcaPEMKeyConverter, which we use to restore keys from PEM files
    implementation("org.bouncycastle:bcpkix-jdk15on:1.56")

}
