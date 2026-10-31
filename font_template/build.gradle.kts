plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.monotype.android.font.kapp.custom_xfont"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.monotype.android.font.kapp.custom_xfont"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "0.4"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}
