plugins {
    `java-library`
}

val edcversion: String by project
val group = "org.eclipse.dataspaceconnector"

dependencies {

    implementation("${group}:spi:${edcversion}")

}
