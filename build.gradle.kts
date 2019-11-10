plugins {
  id("com.android.library")
  kotlin("android")
  kotlin("android.extensions")
}

object Versions {
  const val minSdk = 16
  const val targetSdk = 28
  const val gradleBuildTool = "3.0.1"
  const val kotlin = "1.2.21"
  const val googleServices = "3.1.1"
  const val ktlint = "0.15.0"
  const val ktlintGradle = "3.0.0"
  const val fabricGradleTool = "1.25.1"
  const val gradleVersions = "0.17.0"
  const val supportLibrary = "27.0.2"
  const val retrofit = "2.3.0"
  const val kotshi = "1.0.1"
  const val archLifecycle = "1.1.0"
  const val archRoom = "1.0.0"
  const val dagger = "2.14.1"
  const val firebase = "11.8.0"
  const val kotpref = "2.3.0"
  const val glide = "4.5.0"
  const val groupie = "2.0.3"
  const val stetho = "1.5.0"
  const val debot = "2.0.3"
  const val ossLicenses = "0.9.1"
  const val deploygate = "1.1.4"
  const val playPublisher = "1.2.0"
  const val robolectric = "3.6.1"

  const val gradle_versions_version = "0.27.0"
  const val android_billingclient_version = "2.0.3"
  const val android_ktx_version = "1.1.0"
  const val android_navigation_version = "1.0.0"
  const val android_plugin_version = "3.3.0-alpha12"
  const val android_support_version = "28.0.0"
  const val android_tools_version = "3.5.2"
  const val androidx_appcompat_version = "1.1.0"
  const val androidx_constraint_version = "1.1.3"
  const val androidx_ktx_version = "1.0.0-rc02"
  const val androidx_legacy_version = "1.0.0"
  const val androidx_lifecycle_version = "2.1.0"
  const val androidx_room_version = "2.2.1"
  const val blade_ink_version = "0.7.1"
  const val circleimageview_version = "3.0.1"
  const val crashlytics_version = "2.10.1"
  const val fabric_version = "1.31.2"
  const val firebase_ads_version = "18.3.0"
  const val firebase_core_version = "17.2.1"
  const val google_services_version = "4.3.2"
  const val google_gms_version = "17.0.0"
  const val google_play_version = "18.0.1"
  const val jackson_version = "2.10.0"
  const val jackson_databind_version = "2.10.0"
  const val junit_version = "4.12"
  const val kotlin_version = "1.3.50"
  const val kotlin_coroutines_version = "1.3.2"
  const val timber_version = "4.7.1"
  const val tooltip_version = "0.2.3"

}

android {
  compileSdkVersion(Versions.targetSdk)
  dataBinding.isEnabled = true
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

object Depends {
  object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
  }

  object AndroidX {
    const val appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat_version}"
  }

  object Support {
    val support_v4 = "com.android.support:support-v4:${Versions.supportLibrary}"
    val appcompat_v7 = "com.android.support:appcompat-v7:${Versions.supportLibrary}"
    val design = "com.android.support:design:${Versions.supportLibrary}"
    val cardview_v7 = "com.android.support:cardview-v7:${Versions.supportLibrary}"
    val customtabs = "com.android.support:customtabs:${Versions.supportLibrary}"
    val constraint = "com.android.support.constraint:constraint-layout:1.1.0-beta4"
    val multidex = "com.android.support:multidex:1.0.2"
    val support_emoji = "com.android.support:support-emoji-appcompat:${Versions.supportLibrary}"
    val preference_v7 = "com.android.support:preference-v7:${Versions.supportLibrary}"
    val preference_v14 = "com.android.support:preference-v14:${Versions.supportLibrary}"
  }

  val ktx = "androidx.core:core-ktx:0.1"

  object OkHttp3 {
    val loggingIntercepter = "com.squareup.okhttp3:logging-interceptor:3.9.1"
  }

  object Retrofit {
    val core = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val converterMoshi = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"
    val adapterRxJava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
  }

  object Kotshi {
    val api = "se.ansman.kotshi:api:${Versions.kotshi}"
    val compiler = "se.ansman.kotshi:compiler:${Versions.kotshi}"
  }

  object LifeCycle {
    val runtime = "android.arch.lifecycle:runtime:${Versions.archLifecycle}"
    val extensions = "android.arch.lifecycle:extensions:${Versions.archLifecycle}"
    val reactivestreams = "android.arch.lifecycle:reactivestreams:${Versions.archLifecycle}"
  }

  object Room {
    val runtime = "android.arch.persistence.room:runtime:${Versions.archRoom}"
    val rxjava2 = "android.arch.persistence.room:rxjava2:${Versions.archRoom}"
    val compiler = "android.arch.persistence.room:compiler:${Versions.archRoom}"
  }

  object RxJava2 {
    val core = "io.reactivex.rxjava2:rxjava:2.1.9"
    val android = "io.reactivex.rxjava2:rxandroid:2.0.1"
    val kotlin = "io.reactivex.rxjava2:rxkotlin:2.2.0"
  }

  val rxbroadcast = "com.cantrowitz:rxbroadcast:2.0.0"

  object Binding {
    val compiler = "com.android.databinding:compiler:3.0.1"
  }

  object Dagger {
    val core = "com.google.dagger:dagger:${Versions.dagger}"
    val compiler = "com.google.dagger:dagger-compiler:${Versions.dagger}"
    val android = "com.google.dagger:dagger-android:${Versions.dagger}"
    val androidSupport = "com.google.dagger:dagger-android-support:${Versions.dagger}"
    val androidProcessor = "com.google.dagger:dagger-android-processor:${Versions.dagger}"
  }

  object PlayService {
    val map = "com.google.android.gms:play-services-maps:${Versions.firebase}"
    val oss = "com.google.android.gms:play-services-oss-licenses:${Versions.firebase}"
  }

  object Firebase {
    val firestore = "com.google.firebase:firebase-firestore:${Versions.firebase}"
    val auth = "com.google.firebase:firebase-auth:${Versions.firebase}"
    val core = "com.google.firebase:firebase-core:${Versions.firebase}"
    val messaging = "com.google.firebase:firebase-messaging:${Versions.firebase}"
  }

  val threetenabp = "com.jakewharton.threetenabp:threetenabp:1.0.5"

  object Kotpref {
    val kotpref = "com.chibatching.kotpref:kotpref:${Versions.kotpref}"
    val initializer = "com.chibatching.kotpref:initializer:${Versions.kotpref}"
    val enumSupport = "com.chibatching.kotpref:enum-support:${Versions.kotpref}"
  }

  object Glide {
    val core = "com.github.bumptech.glide:glide:${Versions.glide}"
    val okhttp3 = "com.github.bumptech.glide:okhttp3-integration:${Versions.glide}"
    val compiler = "com.github.bumptech.glide:compiler:${Versions.glide}"
  }

  object Groupie {
    val core = "com.xwray:groupie:${Versions.groupie}"
    val binding = "com.xwray:groupie-databinding:${Versions.groupie}"
  }

  val downloadableCalligraphy = "com.github.takahirom.downloadable.calligraphy:downloadable-calligraphy:0.1.2"

  object Stetho {
    val core = "com.facebook.stetho:stetho:${Versions.stetho}"
    val okhttp3 = "com.facebook.stetho:stetho-okhttp3:${Versions.stetho}"
  }

  val crashlytics = "com.crashlytics.sdk.android:crashlytics:2.8.0@aar"
  val timber = "com.jakewharton.timber:timber:4.6.0"
  val leakcanary = "com.squareup.leakcanary:leakcanary-android:1.5.4"

  object Debot {
    val core = "com.tomoima.debot:debot:${Versions.debot}"
    val noop = "com.tomoima.debot:debot-no-op:${Versions.debot}"
  }

  val junit = "junit:junit:4.12"
  val mockitoKotlin = "com.nhaarman:mockito-kotlin:1.5.0"

  object Robolectric {
    val core = "org.robolectric:robolectric:${Versions.robolectric}"
    val multidex = "org.robolectric:shadows-multidex:${Versions.robolectric}"
  }

  val assertk = "com.willowtreeapps.assertk:assertk:0.9"
  val threetenbp = "org.threeten:threetenbp:1.3.6"

  object SupportTest {
    val runner = "com.android.support.test:runner:1.0.1"
    val espresso = "com.android.support.test.espresso:espresso-core:3.0.1"
    val contrib = "com.android.support.test.espresso:espresso-contrib:3.0.1"
  }

}

dependencies {
  implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
  // Kotlin
  api(Depends.Kotlin.stdlib)
  // Components
  api(Depends.AndroidX.appcompat)
  api("de.hdodenhof:circleimageview:${Versions.circleimageview_version}")
  api("com.github.douglasjunior:android-simple-tooltip:${Versions.tooltip_version}")
  api("com.jakewharton.timber:timber:${Versions.timber_version}")
  // JSON Libraries - Remember to modify proguard-rules.pro
  api("com.fasterxml.jackson.core:jackson-core:${Versions.jackson_version}")
  api("com.fasterxml.jackson.core:jackson-annotations:${Versions.jackson_version}")
  api("com.fasterxml.jackson.core:jackson-databind:${Versions.jackson_databind_version}")
  // Fabric
  api("com.google.firebase:firebase-core:${Versions.firebase_core_version}")
  api("com.google.firebase:firebase-ads:${Versions.firebase_ads_version}")
  api("com.crashlytics.sdk.android:crashlytics:${Versions.crashlytics_version}")
  api("com.google.android.gms:play-services-games:${Versions.google_play_version}")
  api("com.google.android.gms:play-services-auth:${Versions.google_gms_version}")
  // Glide
  //api("com.github.bumptech.glide:glide:4.8.0"
  //debugImplementation "com.squareup.leakcanary:leakcanary-android:1.6.3"
  //releaseImplementation "com.squareup.leakcanary:leakcanary-android-no-op:1.6.3"
  // Optional, if you use support library fragments:
  //debugImplementation "com.squareup.leakcanary:leakcanary-support-fragment:1.6.3"

}