import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.monpote.core.network"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        buildConfigField("String", "AZURE_OPENAI_ENDPOINT", "\"${localProperties.getProperty("azure.openai.endpoint", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${localProperties.getProperty("azure.openai.apikey", "")}\"")
        buildConfigField("String", "AZURE_OPENAI_DEPLOYMENT", "\"${localProperties.getProperty("azure.openai.deployment", "")}\"")
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
