plugins {
    id("com.android.application")
}

android {
    namespace = "com.weathercock.profilepicker_plus"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.weathercock.profilepicker_plus"
        minSdk = 15
        targetSdk = 34
        versionCode = 112
        versionName = "1.2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        buildConfig = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation("com.android.support:support-v13:28.0.0")
    implementation("com.google.zxing:core:3.5.3")
}
