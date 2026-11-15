plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

fun p(key: String): String = providers.gradleProperty(key).orElse("").get()
fun quoted(input: String): String = "\"" + input.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
val testApp = "ca-app-pub-3940256099942544~3347511713"
val testRewarded = "ca-app-pub-3940256099942544/5224354917"
val testBanner = "ca-app-pub-3940256099942544/9214589741"
val sampleUnits = mapOf(
    "ADMOB_INTERSTITIAL_ID" to "ca-app-pub-3940256099942544/1033173712",
    "ADMOB_REWARDED_INTERSTITIAL_ID" to "ca-app-pub-3940256099942544/5354046379",
    "ADMOB_NATIVE_ID" to "ca-app-pub-3940256099942544/2247696110",
    "ADMOB_APP_OPEN_ID" to "ca-app-pub-3940256099942544/9257395921"
)
val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
if (isReleaseBuild) {
    require(p("ADMOB_APP_ID").startsWith("ca-app-pub-") && p("ADMOB_APP_ID") != testApp) {
        "Release needs your real ADMOB_APP_ID. Configure it in ~/.gradle/gradle.properties"
    }
    require(p("ADMOB_REWARDED_ID").startsWith("ca-app-pub-") && p("ADMOB_REWARDED_ID") != testRewarded) {
        "Release needs your real ADMOB_REWARDED_ID."
    }
    require(p("ADMOB_BANNER_ID").startsWith("ca-app-pub-") && p("ADMOB_BANNER_ID") != testBanner) {
        "Release needs your real ADMOB_BANNER_ID."
    }
    sampleUnits.forEach { (name, sample) ->
        require(p(name).startsWith("ca-app-pub-") && p(name) != sample) { "Release needs your real $name." }
    }
    require(p("FIREBASE_API_KEY").isNotBlank() && p("FIREBASE_APP_ID").isNotBlank() &&
        p("FIREBASE_PROJECT_ID").isNotBlank() && p("FIREBASE_SENDER_ID").isNotBlank()) { "Release requires hosted Firestore configuration." }

}
android {
    namespace = "com.bssc.cgl4.free100mock"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.bssc.cgl4.free100mock" // Finalize before first Play Store upload.
        minSdk = 24
        targetSdk = 36
        versionCode = 7
        versionName = "0.7.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["ADMOB_APP_ID"] = p("ADMOB_APP_ID").ifBlank { testApp }
        buildConfigField("String", "ADMOB_REWARDED_ID", quoted(p("ADMOB_REWARDED_ID").ifBlank { testRewarded }))
        buildConfigField("String", "ADMOB_BANNER_ID", quoted(p("ADMOB_BANNER_ID").ifBlank { testBanner }))
        sampleUnits.forEach { (name, sample) ->
            buildConfigField("String", name, quoted(p(name).ifBlank { sample }))
        }
        buildConfigField("String", "FIREBASE_API_KEY", quoted(p("FIREBASE_API_KEY")))
        buildConfigField("String", "FIREBASE_APP_ID", quoted(p("FIREBASE_APP_ID")))
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(p("FIREBASE_PROJECT_ID")))
        buildConfigField("String", "FIREBASE_SENDER_ID", quoted(p("FIREBASE_SENDER_ID")))
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true; buildConfig = true }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("com.google.android.gms:play-services-ads:24.2.0")
    implementation("com.google.android.ump:user-messaging-platform:3.2.0")
    implementation(platform("com.google.firebase:firebase-bom:33.13.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
