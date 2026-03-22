val javaLanguageVersion = project.properties["javaLanguageVersion"] as String? ?: JavaVersion.VERSION_25.majorVersion
val javaVersion = project.properties["javaVersion"] ?: libs.versions.javaVersion.get()

val enablePreview = if (project.properties["enablePreview"] == false) null else "--enable-preview"
val imagePath = project.properties["imagePath"] ?: "omnixys"
val imageTag = project.properties["imageTag"] ?: project.version.toString()

val tracePinnedThreads = project.properties["tracePinnedThreads"] == "true" || project.properties["tracePinnedThreads"] == "TRUE"
val alternativeBuildpack = project.properties["buildpack"]

val mapStructVerbose = project.properties["mapStructVerbose"] == "true" || project.properties["mapStructVerbose"] == "TRUE"
val useTracing = project.properties["tracing"] != "false" && project.properties["tracing"] != "FALSE"
val useDevTools = project.properties["devTools"] != "false" && project.properties["devTools"] != "FALSE"
val activeProfilesProtocol = if (project.properties["https"] != "false" && project.properties["https"] != "FALSE") "dev" else "dev,http"
val activeProfiles = if (System.getenv("ACTIVE_PROFILE") == "test") {
    "test"
} else {
    activeProfilesProtocol
}

plugins {
    java
    jacoco
    idea
    `project-report`
    id("org.springframework.boot") version libs.versions.springBootPlugin.get()
    id("io.spring.dependency-management") version libs.versions.dependencyManagement.get()
    id("org.cyclonedx.bom") version "1.8.1"
}

group = project.findProperty("group") as String
version = project.findProperty("version") as String
extra["snippetsDir"] = file("build/generated-snippets")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(libs.versions.javaVersion.get())
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()

    maven {
        name = "github-starter"
        url = uri("https://maven.pkg.github.com/omnixys/omnixys-starter-java")

        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }

    maven {
        name = "github-bom"
        url = uri("https://maven.pkg.github.com/omnixys/omnixys-bom")

        credentials {
            username = project.findProperty("gpr.user") as String?
            password = project.findProperty("gpr.key") as String?
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("com.omnixys:omnixys-bom:1.0.0")
    }
}

extra["springCloudVersion"] = "2024.0.1"


dependencies {
    implementation("com.omnixys:omnixys-starter")

    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    /**--------------------------------------------------------------------------------------------------------------------
     * SECURITY
     * --------------------------------------------------------------------------------------------------------------------*/
    runtimeOnly("org.bouncycastle:bcpkix-jdk18on:${libs.versions.bouncycastle.get()}") // Argon2
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")

    /**--------------------------------------------------------------------------------------------------------------------
     * für MAPPER
     * --------------------------------------------------------------------------------------------------------------------*/
    annotationProcessor("org.mapstruct:mapstruct-processor:${libs.versions.mapstruct.get()}")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:${libs.versions.lombokMapstructBinding.get()}")
    implementation("org.mapstruct:mapstruct:${libs.versions.mapstruct.get()}")

    /**-------------------------------------------------------------------------------------------------------------------
     * GATEWAY
     ***********************************************************************************************************************************/
    implementation("com.apollographql.federation:federation-graphql-java-support:5.3.0")

    /**------------------------------------------------------------------------------------------------------------------------
     * TEST
     * --------------------------------------------------------------------------------------------------------------------*/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework:spring-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator-test")
    testImplementation("org.springframework.boot:spring-boot-starter-hateoas-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-resource-server-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    /**----------------------------------------------------------------
     * SPRING BOOT STARTER
     **-------------------------------------------------------------*/
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.springframework.boot:spring-boot-starter-json")

    /**------------------------------------------------------------------------------------------------------------------------
     * WICHTIGE EXTRAS
     * --------------------------------------------------------------------------------------------------------------------*/
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok:${libs.versions.lombok.get()}")
    annotationProcessor("org.hibernate:hibernate-jpamodelgen:${libs.versions.hibernateJpamodelgen.get()}")
    implementation("io.github.cdimascio:dotenv-java:${libs.versions.dotenv.get()}") // Bibliothek für .env-Datei

    /**------------------------------------------------------------------------------------------------------------------------
     * MESSANGER
     * --------------------------------------------------------------------------------------------------------------------*/
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names")

// cache
    implementation("org.springframework.boot:spring-boot-starter-data-redis")


    }

tasks.withType<Test> {
    useJUnitPlatform()
    failOnNoDiscoveredTests = false
    finalizedBy(tasks.jacocoTestReport) // Nach Tests Coverage Report erstellen
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // Coverage-Report erst nach den Tests generieren
    reports {
        xml.required.set(true) // Codecov benötigt das XML-Format
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.named("bootRun", org.springframework.boot.gradle.tasks.run.BootRun::class.java) {
    if (enablePreview != null) {
        jvmArgs(enablePreview)
    }
    // systemProperty("spring.profiles.active", activeProfiles)
    systemProperty("logging.file.name", "./build/log/application.log")
    systemProperty("server.tomcat.basedir", "build/tomcat")
//    systemProperty("keycloak.client-secret", project.properties["keycloak.client-secret"]!!)
//    systemProperty("keycloak.issuer", project.properties["keycloak.issuer"]!!)
    //systemProperty("app.keycloak.host", project.properties["keycloak.host"]!!)

    if (tracePinnedThreads) {
        systemProperty("tracePinnedThreads", "full")
    }
}

tasks.named<JavaCompile>("compileJava") {
    with(options) {
        isDeprecation = true
        with(compilerArgs) {
            if (enablePreview != null) {
                add(enablePreview)
            }
            // javac --help-lint
            add("-Xlint:all,-serial,-processing,-preview")
            add("--add-opens")
            add("--add-exports")
        }
    }
}

/**
 * 🔥 Ensure version is visible in build logs
 */
tasks.register("printVersion") {
    doLast {
        println("🚀 Building version: $version")
    }
}

tasks.register("setVersion") {
    doLast {
        val newVersion = project.findProperty("newVersion") as String
        val file = file("gradle.properties")

        val updated = file.readLines().map {
            if (it.startsWith("version=")) {
                "version=$newVersion"
            } else it
        }

        file.writeText(updated.joinToString("\n"))

        println("✅ Version updated to $newVersion")
    }
}
