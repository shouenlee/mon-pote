plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.monpote.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        val properties = java.util.Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "AZURE_OPENAI_ENDPOINT", "\"${properties.getProperty("azure.openai.endpoint", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${properties.getProperty("azure.openai.apikey", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_DEPLOYMENT", "\"${properties.getProperty("azure.openai.deployment", "")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:model"))

    implementation(libs.retrofit)
    implementation(libs.retrofit.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.coroutines.android)
}
