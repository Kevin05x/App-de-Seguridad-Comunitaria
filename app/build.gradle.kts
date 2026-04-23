plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "ingenieriasistemas.mildergd.appalertaprevencioncomunitaria"
    compileSdk = 36

    defaultConfig {
        applicationId = "ingenieriasistemas.mildergd.appalertaprevencioncomunitaria"
        minSdk = 31
        targetSdk = 36
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    //Firebase BOM para controlar las versiones
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.storage)
    // Google  y json
    implementation(libs.gson)
    //Room
    implementation(libs.room.runtime)
    implementation(libs.play.services.location)
    annotationProcessor(libs.room.compiler)
    // UI y Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    //Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //Glide para cargar imágenes
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation(libs.firebase.messaging)

    //Habilitar los servicios de ubicación en tu proyecto.
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("org.mindrot:jbcrypt:0.4")


}


