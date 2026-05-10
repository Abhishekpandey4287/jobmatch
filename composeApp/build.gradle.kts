import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {

        commonMain.dependencies {
            implementation("androidx.activity:activity-compose:1.9.0")
            implementation("org.slf4j:slf4j-android:1.7.36")
            // Compose Multiplatform
            implementation(compose.components.resources)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)

            // Lifecycle + Navigation
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.navigation.compose)

            // Coroutines
            implementation(libs.coroutines.core)

            // Ktor (HTTP + WebSocket)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.websockets)

            // Serialization + DateTime
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)

            // DataStore
            implementation(libs.androidx.datastore.preferences)

            // Krossbow STOMP (real-time)
            implementation(libs.krossbow.stomp.core)
            implementation(libs.krossbow.websocket.ktor)
            implementation(libs.krossbow.stomp.kxserialization)
        }

        androidMain.dependencies {
            implementation("org.slf4j:slf4j-android:1.7.36")
            implementation("androidx.activity:activity-compose:1.9.0")
            implementation(compose.preview)
            implementation(compose.components.resources)
            implementation(libs.ktor.client.android)
            implementation(libs.coroutines.android)
            implementation(libs.activity.compose)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "jobmatch.composeapp.generated.resources"
}

android {
    namespace   = "com.jobmatch"
    compileSdk  = 35

    defaultConfig {
        applicationId = "com.jobmatch"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}