import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("jacoco")
}

val localProperties = Properties()
val localPropertiesFile: File = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

/**
 * Reads a value from local.properties (see local.properties.example). A missing value becomes an
 * empty string with a warning, so the project still compiles after a fresh clone.
 */
fun localProperty(name: String): String {
    val value = localProperties.getProperty(name)
    if (value.isNullOrBlank()) {
        logger.warn("$name is missing from local.properties: the app will compile but will not work.")
    }
    return value.orEmpty()
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
        listOf("TMDB_API_KEY", "FIREBASE_API_KEY", "FIREBASE_PROJECT_ID", "FIREBASE_APP_ID").forEach { name ->
            buildConfigField("String", name, "\"${localProperty(name)}\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    implementation(platform(libs.firebase.bom)) {
        exclude(group = "com.google.android.gms")
        exclude(group = "com.google.android.play")
    }
    implementation(libs.firebase.auth) {
        exclude(group = "com.google.android.gms")
        exclude(group = "com.google.android.play")
    }
    implementation(libs.firebase.firestore) {
        exclude(group = "com.google.android.gms", module = "play-services-measurement")
        exclude(group = "com.google.android.gms", module = "play-services-ads-identifier")
        exclude(group = "com.google.android.play")
    }

    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)
}

tasks.register<JacocoReport>("jacocoCombinedTestReport") {
    dependsOn("testDebugUnitTest", "createDebugCoverageReport")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    // Exclude generated Hilt and Compose classes
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

    // AGP versions put compiled classes in different folders, so include all of them
    val buildDir = project.layout.buildDirectory.get().asFile
    val javaClasses = fileTree("$buildDir/intermediates/javac/debug/classes") { exclude(fileFilter) }
    val kotlinClassesOld = fileTree("$buildDir/tmp/kotlin-classes/debug") { exclude(fileFilter) }
    val kotlinClassesNew = fileTree("$buildDir/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") { exclude(fileFilter) }

    classDirectories.setFrom(javaClasses, kotlinClassesOld, kotlinClassesNew)

    // Merge unit test (.exec) and instrumented test (.ec) results
    executionData.setFrom(
        fileTree(buildDir) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec",
                "outputs/code_coverage/debugAndroidTest/connected/**/*.ec",
            )
        },
    )
}
