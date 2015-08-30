package com.micabytes.util;

import android.support.annotation.NonNull;
import org.jetbrains.annotations.NonNls;

public interface StoryObject {
  public int getInteger(@NonNls String s);
  @NonNull public String getString(@NonNls String s);
  @NonNull public StoryObject getObject(@NonNls String s);
}
