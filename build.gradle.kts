plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.2"
	id("io.spring.dependency-management") version "1.1.6"
	id("com.google.cloud.tools.jib") version "3.3.1"
}

group = "com.vemeet"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("me.paulschwarz:spring-dotenv:4.0.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.amazonaws:aws-java-sdk-cognitoidp:1.12.762")
	implementation("software.amazon.awssdk:s3:2.17.102")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.1.0")
	implementation("org.springframework.security:spring-security-oauth2-jose")
	implementation("org.springframework.security:spring-security-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.flywaydb:flyway-core:9.22.3")
	implementation("org.postgresql:postgresql:42.7.2")
	implementation("com.vladmihalcea:hibernate-types-60:2.21.1")

	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.springframework.security:spring-security-test")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jib {
	from {
		image = "eclipse-temurin:21-jre-alpine"
	}
	to {
		image = "vemeet/core"
		tags = setOf("latest")
	}
	container {
		jvmFlags = listOf("-Xms512m", "-Xmx512m")
		ports = listOf("8080")
		mainClass = "com.vemeet.backend.BackendApplicationKt"
	}
}