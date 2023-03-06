import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    kotlin("kapt") version "1.7.21"
    kotlin("plugin.spring") version "1.7.21"
    id("org.jetbrains.dokka") version "1.7.20"
    id("io.spring.dependency-management") version "1.1.0"
    `maven-publish`
    signing
}

group = "dev.ocpd.spring"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withSourcesJar()
    withJavadocJar()
}

tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all")
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports { mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.9") }
}

extra["kotlin.version"] = getKotlinPluginVersion()

dependencies {
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-logging")
    api("net.logstash.logback:logstash-logback-encoder:7.3")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("reflect"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("spring-boot-logging-json")
                description.set("Spring Boot JSON logging support")
                url.set("https://github.com/ocpddev/spring-boot-json-logging")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    url.set("https://github.com/ocpddev/spring-boot-json-logging")
                }
                developers {
                    developer {
                        id.set("sola")
                        name.set("Sola")
                        email.set("sola@ocpd.dev")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            val releaseUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")

            url = if (version.toString().endsWith("-SNAPSHOT")) snapshotUrl else releaseUrl

            credentials {
                username = project.findSecret("ossrh.username", "OSSRH_USERNAME")
                password = project.findSecret("ossrh.password", "OSSRH_PASSWORD")
            }
        }
    }
}

signing {
    val key = findSecret("ocpd.sign.key", "OCPD_SIGN_KEY")
    if (key != null) {
        val keyId = findSecret("ocpd.sign.key.id", "OCPD_SIGN_KEY_ID")
        val passphrase = findSecret("ocpd.sign.passphrase", "OCPD_SIGN_PASSPHRASE") ?: ""
        useInMemoryPgpKeys(keyId, key, passphrase)
    }
    sign(publishing.publications["maven"])
}

fun Project.findSecret(key: String, env: String): String? =
    project.findProperty(key) as? String ?: System.getenv(env)
