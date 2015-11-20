package com.micabytes.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

import com.micabytes.GameApplication;

@SuppressWarnings("UtilityClass")
public final class FontHandler {
  private static final SparseArray<Typeface> FONTS = new SparseArray<>();

  private FontHandler() {
    // NOOP
  }

  // Returns the hashcode of the font
  public static void loadFont(int key, String fontPath) {
    Context c = GameApplication.getInstance();
    Typeface type = Typeface.createFromAsset(c.getAssets(), fontPath);
    FONTS.append(key, type);
  }

  public static Typeface get(int key) {
    return FONTS.get(key);
  }

}
