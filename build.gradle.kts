import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.gradle.kotlin.dsl.named


plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.hibernate.orm") version "7.2.12.Final"
	id("org.graalvm.buildtools.native") version "1.1.0"
}

group = "com.reyga-dev"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-kafka")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("com.fasterxml.jackson.core:jackson-databind")
//	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.kafka:spring-kafka-test")
	testImplementation("org.testcontainers:kafka")
	testImplementation("org.testcontainers:postgresql")
	testImplementation("org.testcontainers:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

hibernate {
	enhancement {
		enableAssociationManagement = true
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

graalvmNative {
	binaries {
		named("main") {
			imageName.set("application")
			mainClass.set("com.reyga_dev.notification_service.NotificationServiceApplication")
			debug.set(true)
			verbose.set(true)
			fallback.set(true)
			sharedLibrary.set(false)
			richOutput.set(false)
			quickBuild.set(false)

			systemProperties.putAll(mapOf("name1" to "value1", "name2" to "value2"))
			configurationFileDirectories.from(file("src/my-config"))
			excludeConfig.put("com.reyga_dev.notification_service:artifact:version", listOf("^/META-INF/native-image/.*", "^/config/.*"))
			excludeConfig.put(file("path/to/artifact.jar"), listOf("^/META-INF/native-image/.*", "^/config/.*"))

			// Advanced options
			buildArgs.add("--link-at-build-time")
			jvmArgs.add("flag")

			// Runtime options
			runtimeArgs.add("--help")

			useFatJar.set(true)
		}
	}

	agent {
		defaultMode.set("standard")
		enabled.set(true)

		modes {
			standard {
			}
			conditional {
				userCodeFilterPath.set("path-to-filter.json")
				extraFilterPath.set("path-to-another-filter.json") // Optional
			}
			direct {
				options.add("config-output-dir={output_dir}")
				options.add("experimental-configuration-with-origins")
			}
		}

		callerFilterFiles.from("filter.json")
		accessFilterFiles.from("filter.json")
		builtinCallerFilter.set(true)
		builtinHeuristicFilter.set(true)
		enableExperimentalPredefinedClasses.set(false)
		enableExperimentalUnsafeAllocationTracing.set(false)
		trackReflectionMetadata.set(true)

		metadataCopy {
			inputTaskNames.add("test")
			outputDirectories.add("/META-INF/native-image/<groupId>/<artifactId>/")
			mergeWithExisting.set(true)
		}

		tasksToInstrumentPredicate.set { true }
	}
}

/**
 * (Optional) Konfigurasi Tambahan untuk kompresi size binary
 */
tasks.named<BootBuildImage>("bootBuildImage") {
	builder.set("paketobuildpacks/builder-jammy-tiny")
	imageName.set("notification-service-native:latest")
	environment.put("BP_NATIVE_IMAGE", "true")
	environment.put("BP_JVM_VERSION", "25")

	environment.put("BP_BINARY_COMPRESSION_METHOD", "upx")

	cleanCache.set(true)
}