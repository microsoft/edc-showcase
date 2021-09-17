plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

subprojects {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri("https://maven.iais.fraunhofer.de/artifactory/eis-ids-public/")
        }
    }
}

val jetBrainsAnnotationsVersion: String by project
val jacksonVersion: String by project
val jupiterVersion: String by project

allprojects {
    pluginManager.withPlugin("java-library") {
        group = "com.microsoft"
        version = "1.0-SNAPSHOT"
        dependencies {
            api("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")
            api("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
            api("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
            api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
            api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

            testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
            testImplementation("org.easymock:easymock:4.2")
            testImplementation("org.assertj:assertj-core:3.19.0")

        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform()
}