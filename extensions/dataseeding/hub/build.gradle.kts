plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {

    implementation("${group}:spi:${edcversion}")
    implementation("${group}:identity-did-spi:${edcversion}")

}
