import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.konan.target.*

plugins {
    kotlin("multiplatform") version "1.4.32"
}

repositories {
    mavenCentral()
}

tasks.whenTaskAdded {
    if (name.endsWith("test", ignoreCase = true)) onlyIf { !rootProject.hasProperty("skipTests") }
}

kotlin {
    jvm("jvmOld")
    jvm("jvmIr") {
        compilations.all {
            kotlinOptions.useIR = true
        }
    }
    js("jsLegacy", LEGACY) {
        nodejs {
            binaries.executable()
            testTask {
                useMocha {
                    timeout = "600s"
                }
            }
        }
    }
    js("jsIr", IR) {
        nodejs {
            binaries.executable()
            testTask {
                useMocha {
                    timeout = "600s"
                }
            }
        }
    }
    fun native(
        name: String,
        configure: KotlinNativeTargetWithHostTests.() -> Unit
    ) = when {
        HostManager.hostIsLinux -> linuxX64(name, configure)
        HostManager.hostIsMingw -> mingwX64(name, configure)
        HostManager.hostIsMac   -> macosX64(name, configure)
        else                    -> error("")
    }
    native("native") {
        binaries.executable()
        binaries.test(listOf(RELEASE))
        testRuns.all { setExecutionSourceFrom(binaries.getTest(RELEASE)) }
    }
    native("nativeMimalloc") {
        binaries.executable()
        binaries.test(listOf(RELEASE))
        testRuns.all { setExecutionSourceFrom(binaries.getTest(RELEASE)) }
        compilations.all {
            kotlinOptions.freeCompilerArgs += "-Xallocator=mimalloc"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-io:1.5.3")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmOldMain by getting {
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val jvmIrMain by getting {
            dependencies {
                api(kotlin("test-junit"))
            }
        }
        val jsLegacyMain by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
        val jsIrMain by getting {
            dependencies {
                api(kotlin("test-js"))
            }
        }
    }
}
