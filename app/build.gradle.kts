plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
    id ("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.live.pastransport"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.live.pastransport"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled =true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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
    buildFeatures {
        viewBinding  = true
        buildConfig=true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation ("com.android.support:multidex:1.0.3")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //circular img view
    implementation(libs.circleimageview)
    implementation(libs.roundedimageview)

    //CountryCodePicker
    implementation(libs.ccp)

    //scalable size unit for texts
    implementation (libs.sdp.android)
    implementation (libs.ssp.android)

    //otp
    implementation (libs.pinview)

    //glide
    implementation (libs.glide)// Check for the latest version
    annotationProcessor (libs.compiler) // For annotation processing
    implementation (libs.glide.transformations)
    implementation(libs.blur.layout)
    //coroutine //retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.adapter.rxjava2)
    implementation(libs.logging.interceptor)
    implementation(libs.lottie)
    implementation(libs.alerter)
    /* hilt */
    kapt(libs.hilt.android.compiler)

    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.pinview)
    //Glide
    annotationProcessor(libs.compiler.v4160)
    implementation(libs.glide.v4160)
    //uc image cropping
    implementation(libs.ucrop)
    implementation(libs.dexter)
    implementation(libs.roundedimageview)
    implementation (libs.android.gif.drawable)
    implementation(libs.google.direction.library)
    implementation(libs.darioweekviewdatepicker)
    implementation("com.google.dagger:hilt-android:2.50")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // view model
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:")
    //ktx
    implementation("androidx.activity:activity-ktx:1.2.3")
    implementation("androidx.fragment:fragment-ktx:1.3.4")
    /** error alerter **/
    //scalable size unit for texts
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    //fragment nav graph
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    // Firebase
    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.6.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging:23.0.8")
    implementation("com.google.android.gms:play-services-auth:20.3.0")
    implementation("com.google.firebase:firebase-analytics:21.5.0")
    //stripe
    implementation("com.stripe:stripe-android:20.0.0")
    implementation("me.zhanghai.android.materialratingbar:library:1.4.0")

    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

}