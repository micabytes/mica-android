package com.micabytes

import android.app.Application

/* The Game Application */
class Game : Application() {

  override fun onCreate() {
    super.onCreate()
    instance = this
  }

  companion object {
    @JvmStatic
    lateinit var instance: Game
      private set
    lateinit var world: WorldInterface
    lateinit var data: DataInterface
  }

}
