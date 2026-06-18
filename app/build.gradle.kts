// 1. Add this import at the very top
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    kotlin("plugin.serialization")

    id("jacoco")
//    id("com.google.gms.google-services")
}

val localProperties = Properties()
val localPropertiesFile: File = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "it.polimi.rewatch"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "it.polimi.rewatch"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//        testInstrumentationRunner = "it.polimi.rewatch.CustomTestRunner"
        buildConfigField(
            "String",
            "TMDB_API_KEY",
            "\"${localProperties.getProperty("TMDB_API_KEY")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            // Enables coverage for local unitary tests (JVM)
            enableUnitTestCoverage = true

            //  Enables coverage for instrumented tests (emulators/real devices)
            enableAndroidTestCoverage = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
//    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.tmdb.api)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom)) {
        exclude(group = "com.google.android.gms")
        exclude(group = "com.google.android.play")
    } // Use the latest version
    // Add Auth and Firestore dependencies
    implementation(libs.firebase.auth) {
        exclude(group = "com.google.android.gms")
        exclude(group = "com.google.android.play")
    }
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.android.gms", module = "play-services-measurement")
        exclude(group = "com.google.android.gms", module = "play-services-ads-identifier")
        exclude(group = "com.google.android.play")
    }
    //implementation(libs.firebase.auth)
    //implementation(libs.firebase.firestore)

    /* TESTING */

    // basic JUnit 4
    androidTestImplementation("androidx.test.ext:junit:1.1.5")

    // integrate Compose and JUnit 4
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.0")

    // to load composables
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.0")

    // to unlock in case of dealing with animations
    // androidTestImplementation("androidx.compose.ui:ui-test:1.6.0")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.59.2")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.59.2")

}

tasks.register<JacocoReport>("jacocoCombinedTestReport") {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    // Filtri potenti per Hilt e Compose
    val fileFilter = arrayListOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/*_MembersInjector.class",
        "**/Dagger*.*",
        "**/*_Factory*.*",
        "**/*_HiltModules*",
        "**/hilt_aggregated_deps/**",
        "**/*_ComponentTree*.*",
        "**/*\$Result*.*",
        "**/*\$*.*",
    )

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))

    // LA SUPER RETE A STRASCICO (Include il tuo nuovo percorso)
    val buildDir = project.layout.buildDirectory.get().asFile
    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") { exclude(fileFilter) }
    val kotlinClassesOld = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    val kotlinClassesNew = fileTree("$buildDir/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") { exclude(fileFilter) }

    // Passiamo tutte e tre le cartelle a JaCoCo: dovunque siano le classi, le troverà.
    classDirectories.setFrom(javaClasses, kotlinClassesOld, kotlinClassesNew)

    // Unisce Unit Test (.exec) e Android Test (.ec)
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec",
            )
        },
    )
}