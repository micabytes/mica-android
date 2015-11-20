package com.micabytes.util;

import android.support.annotation.NonNull;
import org.jetbrains.annotations.NonNls;

public interface AttributeInterface {
  // Returns the reference serialization ID of the object (used for savegames and such)
  String getId();
  // Returns the attribute required by s as an object
  @NonNull
  Object getAttribute(@NonNls String s);
}
