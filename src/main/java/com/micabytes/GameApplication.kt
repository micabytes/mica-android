package com.micabytes

import android.app.Application

class GameApplication : Application() {
  var world: WorldInterface? = null

  override fun onCreate() {
    super.onCreate()
    instance = this
  }

  companion object {
    lateinit var instance: GameApplication
      private set
  }

}
