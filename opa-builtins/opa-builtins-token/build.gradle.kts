plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":opa-evaluator"))

    implementation("com.nimbusds:nimbus-jose-jwt:10.5")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.82")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
