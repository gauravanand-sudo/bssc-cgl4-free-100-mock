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
val requiredProductionIds = listOf(
    "ADMOB_BANNER_ID", "ADMOB_INTERSTITIAL_ID",
    "ADMOB_REWARDED_ID", "ADMOB_REWARDED_INTERSTITIAL_ID"
)
val optionalProductionIds = listOf("ADMOB_NATIVE_ID", "ADMOB_APP_OPEN_ID")
val productionAdIdRegex = Regex("^ca-app-pub-[0-9]{16}/[0-9]{10}$")
val productionAppIdRegex = Regex("^ca-app-pub-[0-9]{16}~[0-9]{10}$")
val isReleaseBuild = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }
if (isReleaseBuild) {
    require(productionAppIdRegex.matches(p("ADMOB_APP_ID")) && p("ADMOB_APP_ID") != testApp) {
        "Production requires your real ADMOB_APP_ID (format ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY) from AdMob App settings."
    }
    val allDemoIds = (sampleUnits.values + testRewarded + testBanner).toSet()
    requiredProductionIds.forEach { key ->
        require(productionAdIdRegex.matches(p(key)) && p(key) !in allDemoIds) {
            "Production requires a real AdMob ad-unit ID for $key."
        }
    }
    optionalProductionIds.forEach { key ->
        require(p(key).isBlank() ||
            (productionAdIdRegex.matches(p(key)) && p(key) !in allDemoIds)) {
            "Optional $key must be empty or a real non-demo ad-unit ID."
        }
    }
    val expectedPublisher = p("ADMOB_APP_ID").substringBefore("~")
    (requiredProductionIds + optionalProductionIds).forEach { key ->
        require(p(key).isBlank() || p(key).substringBefore("/") == expectedPublisher) {
            "$key belongs to a different AdMob publisher from ADMOB_APP_ID."
        }
    }
    require(p("FIREBASE_API_KEY").isNotBlank() && p("FIREBASE_APP_ID").isNotBlank() &&
        p("FIREBASE_PROJECT_ID").isNotBlank() && p("FIREBASE_SENDER_ID").isNotBlank()) {
        "Release requires hosted Firestore configuration."
    }
}
android {
    namespace = "com.bssc.cgl4.free100mock"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.bssc.cgl4.free100mock" // Finalize before first Play Store upload.
        minSdk = 24
        targetSdk = 36
        versionCode = 23
        versionName = "0.23.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Debug APKs use ONLY Google's demo units, even if production IDs are configured.
        // This prevents accidental invalid-traffic clicks during testing.
        manifestPlaceholders["ADMOB_APP_ID"] = if(isReleaseBuild) p("ADMOB_APP_ID") else testApp
        buildConfigField("String", "ADMOB_REWARDED_ID",
            quoted(if(isReleaseBuild) p("ADMOB_REWARDED_ID") else testRewarded))
        buildConfigField("String", "ADMOB_BANNER_ID",
            quoted(if(isReleaseBuild) p("ADMOB_BANNER_ID") else testBanner))
        // In production missing Native / App Open units are blank and disabled,
        // never silently replaced by Google's demo IDs.
        sampleUnits.forEach { (name, sample) ->
            val value = if (isReleaseBuild) p(name) else sample
            buildConfigField("String", name, quoted(value))
        }
        buildConfigField("String", "FIREBASE_API_KEY", quoted(p("FIREBASE_API_KEY")))
        buildConfigField("String", "FIREBASE_APP_ID", quoted(p("FIREBASE_APP_ID")))
        buildConfigField("String", "FIREBASE_PROJECT_ID", quoted(p("FIREBASE_PROJECT_ID")))
        buildConfigField("String", "FIREBASE_SENDER_ID", quoted(p("FIREBASE_SENDER_ID")))
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", quoted(p("GOOGLE_WEB_CLIENT_ID")))
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
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("io.coil-kt:coil-compose:2.7.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
