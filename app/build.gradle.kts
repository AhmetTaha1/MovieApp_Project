plugins {
    id("com.android.application")
}

android {
    namespace = "com.movielog.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.movielog.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "OMDB_API_KEY", "\"buraya yapıstır\"")
        buildConfigField("String", "GROQ_API_KEY", "\"buraya yapıstır\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // ViewModel + LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.3")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.activity)
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // OkHttp (Groq API çağrıları için)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")
}