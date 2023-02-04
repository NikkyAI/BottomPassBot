plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
    id("com.github.johnrengelman.shadow")
}

group = "org.example"
version = "1.0-SNAPSHOT"


repositories {
    mavenCentral()
    google()

    maven {
        name = "Sonatype Snapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        name = "Kotlin Discord"
        url = uri("https://maven.kotlindiscord.com/repository/maven-public/")
    }
}


dependencies {
    implementation("com.kotlindiscord.kord.extensions:kord-extensions:_")
    implementation("com.kotlindiscord.kord.extensions:annotations:_")

    implementation("com.github.twitch4j:twitch4j:_")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:_")
    implementation(KotlinX.Coroutines.core)

//    implementation("org.jetbrains.kotlinx:kotlin-deeplearning-tensorflow:0.5.1")
//    implementation("org.jetbrains.kotlinx:kotlin-onnx-tensorflow:0.5.1")
//    implementation("org.tensorflow:tensorflow-core-platform:_")

    implementation(Square.okio)
//    implementation("com.sangupta:bloomfilter:_")

    implementation("io.klogging:klogging-jvm:_")
    implementation("io.klogging:slf4j-klogging:_")
//    implementation("org.slf4j:slf4j-api:1.7.36")
//    implementation("io.github.microutils:kotlin-logging:_")
//    implementation("ch.qos.logback:logback-classic:_")

    testImplementation(kotlin("test", "_"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
        this.implementation
    }
    jvmToolchain(17)
}

application {
    mainClass.set("moe.nikky.MainKt")
}

tasks {
    shadowJar {
        archiveBaseName.set("application")
        archiveClassifier.set("")
        archiveVersion.set("")
    }
}