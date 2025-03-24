import com.vanniktech.maven.publish.SonatypeHost


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "io.github.kotlin"
version = "1.0.0"

kotlin {
    jvm()
    linuxX64()
    mingwX64 {
        binaries {
            executable {
                entryPoint = "io.github.kotlin.fibonacci.main"
            }
        }
        compilations.getByName("main") {
            cinterops.create("dut") {
                defFile("src/mingwX64Main/cinterop/dut.def")
            }
//            cinterops.register("dut") {
//                definitionFile = layout.projectDirectory.file("src/mingwX64Main/cinterop/dut.def")
//                extraOpts("-libraryPath", layout.projectDirectory.dir("src/mingwX64Main/c/").asFile.absolutePath)
//            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //put your multiplatform dependencies here
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
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
