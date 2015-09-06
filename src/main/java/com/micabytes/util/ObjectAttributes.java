package com.micabytes.util;

import android.support.annotation.NonNull;
import org.jetbrains.annotations.NonNls;

public interface ObjectAttributes {
  public int getInteger(@NonNls String s);
  @NonNull public String getString(@NonNls String s);
  @NonNull public Object getObject(@NonNls String s);
}
