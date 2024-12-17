plugins {
    id("org.jetbrains.kotlin.android")
    id("com.android.application")
    id("androidx.navigation.safeargs.kotlin") version "2.7.3"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android") version "2.48"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.0"
}

android {
    namespace = "com.iremsilayildirim.capstone"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.iremsilayildirim.capstone"
        minSdk = 25
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        dataBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.2"
    }
}

dependencies {
    implementation(libs.androidx.foundation.android)
    // Compose
    val compose_version = "1.6.0-alpha01"
    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("androidx.compose.runtime:runtime:$compose_version")
    implementation("androidx.compose.runtime:runtime-livedata:$compose_version")


    // Navigation & Fragment
    val nav_version = "2.7.7"
    val fragment_version = "1.8.1"
    implementation("androidx.fragment:fragment-ktx:$fragment_version")
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.multidex:multidex:2.0.1")

    // Room (for database)
    val room_version = "2.6.0"
    implementation("androidx.room:room-runtime:$room_version") // Room runtime
    annotationProcessor("androidx.room:room-compiler:$room_version") // Room compiler
    implementation("androidx.room:room-ktx:$room_version") // Room KTX
    kapt("androidx.room:room-compiler:$room_version") // Room compiler with kapt

    // Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // AndroidX Core and AppCompat
    implementation(libs.androidx.core.ktx) // AndroidX Core KTX

    // UI components
    implementation("com.google.android.material:material:1.12.0") // Google Material Components

    // Testing libraries
    testImplementation(libs.junit) // Unit testing
    androidTestImplementation(libs.androidx.junit) // Android testing with JUnit
    androidTestImplementation(libs.androidx.espresso.core) // Espresso testing for UI

    // Retrofit (for networking)
    implementation("com.squareup.retrofit2:retrofit:2.11.0") // Retrofit for HTTP requests
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // Gson converter for Retrofit
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // OkHttp logging interceptor

    // Image loading libraries
    implementation("com.github.bumptech.glide:glide:4.15.1") // Glide image loading library
    kapt("com.github.bumptech.glide:glide:4.15.1") // Glide KAPT annotation processor
    implementation("io.coil-kt:coil:2.7.0") // Coil image loading library

    // Dagger Hilt (dependency injection)
    implementation("com.google.dagger:hilt-android:2.47") // Hilt for Android
    kapt("com.google.dagger:hilt-android-compiler:2.47") // Hilt compiler

    // CardView (for displaying content in a card style)
    implementation("androidx.cardview:cardview:1.0.0")

    // Circle ImageView (for circular profile pictures)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // AndroidX Lifecycle for lifecycleScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // Kotlin Coroutines for launch and other coroutine features
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.0")

    // Other AndroidX Libraries
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.moshi:moshi:1.15.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt {
    useBuildCache = false
    correctErrorTypes = true

}