package com.micabytes

import android.app.Activity
import android.app.Application
import com.micabytes.app.BaseActivityCallback

/* The Game Application */
class Game : Application() {

  init {
    instance = this
  }

  val mFTActivityLifecycleCallbacks = BaseActivityCallback()

  override fun onCreate() {
    super.onCreate()
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
