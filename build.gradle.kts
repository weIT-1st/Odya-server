import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.7"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "11.2.0"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.spring") version "1.8.21"
    kotlin("plugin.jpa") version "1.8.21"
}

group = "kr.weit"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

val asciidoctorExt: Configuration by configurations.creating
val snippetsDir by extra { "build/generated-snippets" }

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.sentry:sentry-spring-boot-starter-jakarta:6.19.0")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    runtimeOnly("com.oracle.database.jdbc:ojdbc10:19.8.0.0")
    implementation("com.oracle.database.security:osdt_cert:19.8.0.0")
    implementation("com.oracle.database.security:oraclepki:19.8.0.0")
    implementation("com.oracle.database.security:osdt_core:19.8.0.0")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    implementation("org.flywaydb:flyway-core")

    implementation("com.google.firebase:firebase-admin:9.1.1")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-assertions-core:5.6.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

    implementation("com.oracle.oci.sdk:oci-java-sdk-objectstorage:3.14.0")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3:3.14.0")

    implementation("org.opensearch.client:spring-data-opensearch-starter:1.1.0") {
        exclude("org.opensearch.client", "opensearch-rest-client-sniffer")
    }
    implementation("org.opensearch.client:spring-data-opensearch-test-autoconfigure:1.1.0") {
        exclude("org.opensearch.client", "opensearch-rest-client-sniffer")
    }

    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.0.1"))

    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.1")

    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    ktlint {
        verbose.set(true)
        disabledRules.addAll("annotation", "filename")
    }

    test {
        useJUnitPlatform()
        outputs.dir(snippetsDir)
    }

    asciidoctor {
        inputs.dir(snippetsDir)
        configurations("asciidoctorExt")
        sources {
            include("**/index.adoc")
        }
        dependsOn(test)
        baseDirFollowsSourceFile()
    }

    register<Copy>("copyDocs") {
        dependsOn(asciidoctor)
        from("${asciidoctor.get().outputDir}/index.html")
        into("src/main/resources/static/docs")
    }

    bootJar {
        dependsOn("copyDocs")
        from("${asciidoctor.get().outputDir}/index.html") {
            into("static/docs")
        }
    }
}
