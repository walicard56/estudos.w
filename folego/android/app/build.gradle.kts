import java.util.Properties

plugins {
    id("com.android.application")
}

// Assinatura de release: lida de keystore.properties (local) ou de variáveis de
// ambiente (GitHub Actions). Sem isso, o build de release sai sem assinatura.
val signingProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun signingValue(key: String, env: String): String? =
    signingProps.getProperty(key) ?: System.getenv(env)

val releaseStoreFile = signingValue("storeFile", "FOLEGO_KEYSTORE_FILE")

android {
    namespace = "com.folego.financas"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.folego.financas"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1.0"
    }

    signingConfigs {
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = signingValue("storePassword", "FOLEGO_KEYSTORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "FOLEGO_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "FOLEGO_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // O app web (folego/web) é a única fonte das telas: entra no APK como assets.
    sourceSets["main"].assets.srcDir("../../web")
    androidResources {
        ignoreAssetsPattern = "!sw.js:!LEIA-ME.txt:!.*:!*~"
    }
}

dependencies {
    implementation("androidx.activity:activity:1.10.1")
    implementation("androidx.core:core:1.16.0")
    implementation("androidx.webkit:webkit:1.14.0")
}
