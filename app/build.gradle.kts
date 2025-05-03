plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34
        defaultConfig {
            applicationId = "com.example.myapplication"
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

        buildTypes {
            release {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
            viewBinding {
                var enabled = true
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

        }

        viewBinding {

            enable = true;
        }


    }


    dependencies {
        implementation ("com.github.arimorty:floatingsearchview:2.1.1")
        implementation ("com.github.pavlospt:circleview:1.3")
        implementation ("com.ms-square:expandableTextView:0.1.4")
        implementation ("com.google.android.gms:play-services-places:17.0.0")
        implementation ("me.relex:circleindicator:2.1.6")
        implementation ("com.afollestad.material-dialogs:core:3.3.0")
        implementation ("com.google.android.libraries.places:places:3.3.0")
        implementation ("androidx.biometric:biometric:1.1.0")
        implementation("com.github.mhiew:android-pdf-viewer:3.2.0-beta.1")
        implementation("com.android.billingclient:billing:6.1.0")
        implementation("com.google.android.gms:play-services-wallet:19.2.1")
        implementation("com.google.android.gms:play-services-location:21.1.0")
        implementation("com.nex3z:notification-badge:1.0.5")
        implementation("com.airbnb.android:lottie:6.3.0")
        implementation ("com.google.android.gms:play-services-auth:20.7.0")
        implementation ("com.google.android.gms:play-services-wallet:19.4.0")
         implementation ("com.google.android.material:material:1.4.0")
        implementation ("com.stripe:stripe-android:20.0.0")  // Добавьте зависимость для Stripe SDK
        implementation ("com.squareup.retrofit2:retrofit:2.9.0")       // Для Retrofit
        implementation ("com.squareup.retrofit2:converter-gson:2.9.0") // Для конвертации JSON в объекты Java
        implementation ("com.squareup.okhttp3:okhttp:4.9.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("com.google.firebase:firebase-database:20.3.0")
        implementation("com.google.firebase:firebase-auth:22.3.1")
        implementation("com.google.firebase:firebase-storage:20.3.0")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
        implementation("androidx.activity:activity:1.9.3")
        testImplementation("junit:junit:4.13.2")
        implementation("com.github.bumptech.glide:glide:4.16.0")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    }

