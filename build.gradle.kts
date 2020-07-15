plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("android.extensions")
  kotlin("kapt")
}

android {
  compileSdkVersion(Versions.targetSdk)
  buildFeatures.dataBinding = true
  defaultConfig {
    minSdkVersion(Versions.minSdk)
    targetSdkVersion(Versions.targetSdk)
  }
  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  // Kotlin
  api(Depends.Kotlin.stdlib)
  // Components
  api(Depends.AndroidX.appcompat)
  api(Depends.AndroidX.appcompat)
  api(Depends.AndroidLib.circleImageView)
  api(Depends.AndroidLib.simpleToolTip)
  api(Depends.AndroidLib.timber)
  api(Depends.LifeCycle.extensions)
  api(Depends.LifeCycle.viewmodel)
  // JSON Libraries - Remember to modify proguard-rules.pro
  api(Depends.Jackson.core)
  api(Depends.Jackson.annotations)
  api(Depends.Jackson.databind)
  // Fabric
  api(Depends.Firebase.core)
  api(Depends.Firebase.ads)
  api(Depends.Firebase.crashlytics)
  api(Depends.PlayService.auth)
  api(Depends.PlayService.games)
}
