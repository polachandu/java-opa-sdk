plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":opa-evaluator"))

    implementation("com.github.java-json-tools:json-patch:1.13")
    implementation("com.networknt:json-schema-validator:1.5.5")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
