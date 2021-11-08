plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {

    api("${group}:spi:${edcversion}")
    api("${group}:catalog.spi:${edcversion}")
    implementation("${group}:spi:${edcversion}")
    implementation("${group}:dataspaceconnector.common.util:${edcversion}")

}
