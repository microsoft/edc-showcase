plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {

    api("${group}:spi:${edcversion}")
    api("${group}:catalog-cache:${edcversion}")
    implementation("${group}:spi:${edcversion}")
    implementation("${group}:common-util:${edcversion}")
    implementation("${group}:dataloading:${edcversion}")

}
