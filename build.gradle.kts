import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.3.20"
    id("org.jetbrains.compose") version "1.10.3"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.20"
}

group = "com.excelsior.nothing"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.mozilla:rhino:1.7.15")
}

kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "com.excelsior.nothing.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "the-nothing-system"
            packageVersion = "1.0.0"
        }
    }
}
