plugins {
    id("com.android.application")
}

android {
    namespace = "ru.monohome.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.monohome.monoicons"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            merges += "META-INF/xposed/*"
        }
    }
}

dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
}
