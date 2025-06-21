plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vasthread.webviewtv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vasthread.webviewtv"
        minSdk = 21
        targetSdk = 35
        versionCode = 6
        versionName = "1.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            //noinspection ChromeOsAbiSupport
            abiFilters += listOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.tencent.tbs:tbssdk:44286")
    implementation("com.tencent.bugly:crashreport:latest.release")
    implementation("com.github.JessYanCoding:AndroidAutoSize:v1.2.1")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("org.apache.commons:commons-lang3:3.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}