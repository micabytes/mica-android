package com.micabytes;

import android.app.Application;

public class GameApplication extends Application {
  // Game World
  private WorldInterface world;
  // Instance
  private static GameApplication instance = null;

  public static GameApplication getInstance() {
    if (instance == null)
      throw new IllegalStateException("GameApplication not created yet!");
    return instance;
  }

  public WorldInterface getWorld() {
    return world;
  }

  public void setWorld(WorldInterface w) {
    world = w;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    //noinspection AssignmentToStaticFieldFromInstanceMethod
    instance = this;
  }

}
