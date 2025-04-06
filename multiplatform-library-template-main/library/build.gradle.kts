import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlinxSerialization)
}

group = "io.github.e1turin"
version = "1.0.0"

kotlin {
    jvmToolchain(22)
    jvm {
        withJava() // TODO: setup jextract task
        compilerOptions {
            jvmTarget = JvmTarget.JVM_22
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass = "io.github.e1turin.cirkt.Main"
//            mainClass = "io.github.e1turin.cirkt.HelloKt"
        }
    }
    linuxX64 {
        binaries {
            executable {
                entryPoint = "io.github.e1turin.cirkt.main"
            }
        }
        compilations.getByName("main") {
            cinterops.create("dut") {
                defFile("src/linuxX64Main/cinterop/dut.def")
            }
        }
    }
    mingwX64 {
        binaries {
            executable {
                entryPoint = "io.github.e1turin.cirkt.main"
            }
        }
        compilations.getByName("main") {
            cinterops.create("dut") {
                defFile("src/mingwX64Main/cinterop/dut.def")
            }
        }
    }

//    macosArm64()
//    macosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.squareup:kotlinpoet:2.1.0")
            }

            resources.srcDirs("src/jvmMain/resources")
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates(group.toString(), "library", version.toString())

    pom {
        name = "My library"
        description = "A library."
        inceptionYear = "2024"
        url = "https://github.com/kotlin/multiplatform-library-template/"
        licenses {
            license {
                name = "XXX"
                url = "YYY"
                distribution = "ZZZ"
            }
        }
        developers {
            developer {
                id = "XXX"
                name = "YYY"
                url = "ZZZ"
            }
        }
        scm {
            url = "XXX"
            connection = "YYY"
            developerConnection = "ZZZ"
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-native-access=ALL-UNNAMED")

    val dynLibPath = "${projectDir}/src/jvmMain/lib/"

    when (val host = HostManager.host) {
        // by some reason setting up java.library.path variable do not give result
        KonanTarget.LINUX_X64 -> environment("LD_LIBRARY_PATH", dynLibPath)

        KonanTarget.MINGW_X64 -> jvmArgs("-Djava.library.path=${dynLibPath}")

        // TODO: check build on MacOS
        KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64 -> environment("DYLD_LIBRARY_PATH", dynLibPath)

        else -> error("Unknown host: $host")
    }
}

tasks.named<Jar>("jvmJar") {
    manifest {
        attributes["Main-Class"] = "io.github.e1turin.cirkt.Main"
    }
}
