plugins {
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Jackson is a compile-only dependency: the Engine JsonNode API, Bundle.manifest,
    // Capabilities, and IR class annotations require it at compile time.
    // At runtime, jackson-databind is provided transitively via the opa-jackson module.
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    testImplementation(project(":opa-jackson"))
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.json:json:20250517")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("org.skyscreamer:jsonassert:1.5.3")
    testImplementation("org.mockito:mockito-core:5.16.1")
    testImplementation(project(":opa-builtins"))
}

tasks.test {
    useJUnitPlatform()
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}