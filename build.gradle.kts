import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("net.mamoe.mirai-console") version "2.14.0"
}

allprojects {

    apply(plugin = "kotlin")
    apply(plugin = "kotlinx-serialization")
    apply(plugin = "net.mamoe.mirai-console")

    group = "io.github.argonariod"
    version = "1.0.0"

    repositories {
//        maven("https://maven.aliyun.com/repository/public")
        mavenCentral()
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    mirai {
        jvmTarget = JavaVersion.VERSION_11
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }

    tasks.test {
        useJUnitPlatform()
    }
}