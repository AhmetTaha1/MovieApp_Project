import java.util.Properties
import java.io.FileInputStream

plugins {
    id("com.android.application")
}

// local.properties'ten değerleri oku
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val rawTmdbApiKey = localProperties.getProperty("TMDB_API_KEY", "")
val tmdbApiKey = rawTmdbApiKey.replace("\"", "").replace("'", "")

android {
    namespace = "com.movielog.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.movielog.app"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "OMDB_API_KEY", "\"354f8d93\"")
        buildConfigField("String", "GROQ_API_KEY", "\"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\"")
        buildConfigField("String", "TMDB_API_KEY", "\"83ecccd70086adc08f636549fb8fffcd\"")
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

    // YouTube Player
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // Local Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}