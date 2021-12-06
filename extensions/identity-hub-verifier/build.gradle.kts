plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"
dependencies {
    api("${group}:identity-did-spi:${edcversion}")
    api("${group}:identity-did-core:${edcversion}")
    api("${group}:identity-did-crypto:${edcversion}")
}
