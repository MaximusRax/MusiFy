plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.brinux.musify"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.brinux.musify"
        minSdk = 28
        targetSdk = 35
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
    buildFeatures{
        viewBinding= true;
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.exoplayer:exoplayer:2.19.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
//    implementation("com.github.bumptech.glide:compiler:4.15.1")
    implementation("androidx.palette:palette:1.0.0")
    implementation("com.github.alexei-frolo:WaveformSeekBar:1.1")
    implementation("jp.wasabeef:glide-transformations:4.3.0")

}