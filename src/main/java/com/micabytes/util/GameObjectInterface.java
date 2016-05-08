package com.micabytes.util;

import android.support.annotation.NonNull;

public interface GameObjectInterface {
  // Returns the reference serialization ID of the object (used for savegames and such)
  @NonNull String getId();
  // Returns the name of the object
  String getName();
}
