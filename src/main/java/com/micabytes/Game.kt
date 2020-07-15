package com.micabytes

import android.app.Activity
import android.app.Application
import com.micabytes.app.BaseActivityCallback
import com.micabytes.util.CrashReportingTree
import timber.log.Timber

//import com.squareup.leakcanary.LeakCanary

/* The Game Application */
class Game : Application() {

  init {
    instance = this
  }

  val mFTActivityLifecycleCallbacks = BaseActivityCallback()

  override fun onCreate() {
    super.onCreate()
    /*
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
    */
    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    } else {
      Timber.plant(CrashReportingTree())
    }
    registerActivityLifecycleCallbacks(mFTActivityLifecycleCallbacks)
  }

  companion object {
    @JvmStatic
    lateinit var instance: Game
      private set
    val currentActivity: Activity
      get() = instance.mFTActivityLifecycleCallbacks.currentActivity!!
    var world: WorldInterface? = null
    var data: DataInterface? = null
  }

}
