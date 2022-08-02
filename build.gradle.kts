buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlinx:binary-compatibility-validator:0.9.0")
    }
}

apply(plugin = "binary-compatibility-validator")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
    signing
}

version = "2.0.5"
group = "com.myunidays.multiplatform-swiftpackage"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("gradle-plugin", "1.6.21"))
    testImplementation("io.kotest:kotest-runner-junit5:5.2.3")
    testImplementation("io.kotest:kotest-assertions-core:5.2.3")
    testImplementation("io.kotest:kotest-property:5.2.3")
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation(kotlin("gradle-plugin", "1.6.21"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

project.tasks.named("processResources", Copy::class.java) {
    // https://github.com/gradle/gradle/issues/17236
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

extensions.findByName("buildScan")?.withGroovyBuilder {
    setProperty("termsOfServiceUrl", "https://gradle.com/terms-of-service")
    setProperty("termsOfServiceAgree", "yes")
}

gradlePlugin {
    plugins {
        create("pluginMaven") {
            id = "com.myunidays.multiplatform-swiftpackage"
            implementationClass = "com.myunidays.multiplatformswiftpackage.MultiplatformSwiftPackagePlugin"
        }
    }
}

fun SigningExtension.whenRequired(block: () -> Boolean) {
    setRequired(block)
}

//tasks.javadoc {
//    if (JavaVersion.current().isJava9Compatible) {
//        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
//    }
//}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.value("javadoc")
}

publishing {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

            credentials {
                username = System.getenv("sonatypeUsernameEnv")
                password = System.getenv("sonatypePasswordEnv")
            }
        }
    }

    publications.all {
        this as MavenPublication

        artifact(javadocJar)

        pom {
            name.set("Multiplatform Swift Package")
            description.set("Gradle plugin to generate a Swift.package file and XCFramework to distribute a Kotlin Multiplatform iOS library")
            url.set("https://github.com/myunidays/multiplatform-swiftpackage")

            licenses {
                license {
                    name.set("Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("Reedyuk")
                    name.set("Andrew Reed")
                    email.set("andrew.reed@myunidays.com")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/myunidays/multiplatform-swiftpackage.git")
                developerConnection.set("scm:git:ssh://git@github.com/myunidays/multiplatform-swiftpackage.git")
                url.set("https://github.com/myunidays/multiplatform-swiftpackage")
            }
        }
    }
}

signing {
    whenRequired { gradle.taskGraph.hasTask("publish") }
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
