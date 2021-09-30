plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"
dependencies {
    api("${group}:iam.identity-did-spi:${edcversion}")
    api("${group}:iam.identity-did-core:${edcversion}")
}
