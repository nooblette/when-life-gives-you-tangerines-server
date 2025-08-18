plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    kotlin("plugin.jpa") version "1.9.25"
}

ktlint {
    // ktlint 리포트를 JSON 포맷으로 작성
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.JSON)
    }
}

group = "com.tangerine"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2024.0.0")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    // Kotest core
    testImplementation("io.kotest:kotest-runner-junit5:5.8.0") // Kotest + JUnit5 통합 실행기
    testImplementation("io.kotest:kotest-assertions-core:5.8.0") // 핵심 assertion
    testImplementation("io.kotest:kotest-property:5.8.0") // property-based testing (선택)
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0") // Mockito-Kotlin 확장
    // WireMock (FeignClient 테스트용, API 모킹을 위한 서버를 띄우고 실제 HTTP 요청/응답을 받는다)
    testImplementation("org.wiremock:wiremock-standalone:3.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    useJUnitPlatform()
}
