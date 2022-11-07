import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.jetbrains.kotlin.jvm") version "1.6.21"
	id("org.jetbrains.kotlin.plugin.spring") version "1.6.21"
	id("com.adarshr.test-logger") version "3.2.0"
}

group = "name.hennr"

// update gradle (always the ALL distribution) via
// ./gradlew wrapper --gradle-version 7.x.x
tasks.named<Wrapper>("wrapper") {
	distributionType = Wrapper.DistributionType.ALL
}

kotlin {
	jvmToolchain {
		(this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("ch.qos.logback:logback-classic")
	implementation("io.github.microutils:kotlin-logging:3.0.4")

	// caching
	implementation("com.github.ben-manes.caffeine:caffeine")

	testImplementation("de.mkammerer.wiremock-junit5:wiremock-junit5:1.1.0")
	testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")

	developmentOnly("org.springframework.boot:spring-boot-devtools")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

testlogger {
	theme = com.adarshr.gradle.testlogger.theme.ThemeType.MOCHA_PARALLEL
}
