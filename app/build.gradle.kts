plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.gms.google-services")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.e_commerceapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.e_commerceapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    buildFeatures { dataBinding = true }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // add more icons in app
    implementation("androidx.compose.material:material-icons-extended")
    

    // navigation
    implementation(libs.androidx.navigation.compose)

    // lottie animation
    implementation(libs.lottie.compose)


    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)


    // Retrofit core
    implementation(libs.retrofit)
    implementation(libs.converter.gson) // Retrofit with Gson converter (for JSON serialization)
    implementation(libs.moshi.kotlin) // convert JSON object to Java/Kotlin object
    implementation(libs.okhttp)  // send and receive http/https from/to server
    implementation(libs.logging.interceptor)

    // JWT to create token for firebase user
    implementation (libs.kjwt.core)
    implementation (libs.kjwt.jwks)
    implementation (libs.arrow.core)



    // Room database
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.paging)
    testImplementation (libs.androidx.room.testing)
    androidTestImplementation (libs.androidx.room.testing)

    // Hilt core
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // viewmodel compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coil
    implementation(libs.coil.compose)


//    implementation(files("libs/Paymob-SDK-1.6.12.aar"))

//    implementation("com.paymob.sdk:Paymob-SDK:1.6.10")
}


