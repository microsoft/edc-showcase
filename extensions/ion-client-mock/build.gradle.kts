plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {


    implementation("org.bouncycastle:bcpkix-jdk15on:1.56")


    implementation("${group}:spi:${edcversion}")
    implementation("${group}:data-protocols.ion.core:${edcversion}")
    implementation("${group}:iam.identity-did-spi:${edcversion}")
}
