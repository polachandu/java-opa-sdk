plugins {
    `java-library`
}

dependencies {
    api(project(":opa-builtins-time"))
    api(project(":opa-builtins-token"))
    api(project(":opa-builtins-regex"))
    api(project(":opa-builtins-semver"))
    api(project(":opa-builtins-net"))
    api(project(":opa-builtins-crypto"))
    api(project(":opa-builtins-json"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}
