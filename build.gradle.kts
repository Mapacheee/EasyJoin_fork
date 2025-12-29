plugins {
    id("java")
    id("java-library")
    id("com.gradleup.shadow") version "9.3.0"
}

group = "me.espryth.easyjoin"
version = "3.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")

    api("com.thewinterframework:paper:1.0.4") {
        exclude(group = "io.papermc.paper", module = "paper-api")
    }
    annotationProcessor("com.thewinterframework:paper:1.0.4")

    api("com.thewinterframework:configuration:1.0.2") {
        exclude(group = "io.papermc.paper", module = "paper-api")
    }
    annotationProcessor("com.thewinterframework:configuration:1.0.2")

    api("com.thewinterframework:command:1.0.1") {
        exclude(group = "io.papermc.paper", module = "paper-api")
        exclude(group = "org.incendo", module = "cloud-paper")
        exclude(group = "org.incendo", module = "cloud-annotations")
        exclude(group = "org.incendo", module = "cloud-bukkit")
        exclude(group = "org.incendo", module = "cloud-core")
    }
    annotationProcessor("com.thewinterframework:command:1.0.1")

    api("org.incendo:cloud-paper:2.0.0-beta.10")
    api("org.incendo:cloud-annotations:2.0.0")

    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("fr.xephi:authme:5.6.0-SNAPSHOT")
    compileOnly("com.nickuc.login:nlogin-api:10.0")

    implementation("net.kyori:adventure-text-serializer-legacy:4.26.1")
    implementation("net.kyori:adventure-text-minimessage:4.26.1")
    implementation("com.zaxxer:HikariCP:7.0.2")
}


tasks.shadowJar {
    relocate("com.zaxxer.hikari", "me.espryth.easyjoin.libs.hikari")
    archiveClassifier.set("")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
