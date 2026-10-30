plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.gupshup"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gupshup"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/* ✅ dependencies MUST be here */
dependencies {

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging:24.0.0")
    implementation("com.google.mlkit:smart-reply:17.0.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.firebase.firestore)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
