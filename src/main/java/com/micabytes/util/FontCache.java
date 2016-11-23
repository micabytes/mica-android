package com.micabytes.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.widget.TextView;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("Singleton")
public final class FontCache {
  private static final String TAG = FontCache.class.getName();
  private static final char BACKSLASH = '/';
  private static final char DOT = '.';
  @NonNls private static final String FONT_DIR = "fonts";
  private static final Map<String, Typeface> CACHE = new HashMap<>();
  private static final Map<String, String> FONT_MAPPING = new HashMap<>();
  private static FontCache instance = null;
  private Context context = null;

  public static FontCache getInstance(Context c) {
    if (instance == null) {
      instance = new FontCache(c.getApplicationContext());
    }
    return instance;
  }

  public static void addFont(String name, String fontFilename) {
    FONT_MAPPING.put(name, fontFilename);
  }

  private FontCache(Context c) {
    context = c;
    AssetManager am = c.getResources().getAssets();
    String[] fileList;
    try {
      fileList = am.list(FONT_DIR);
    } catch (IOException ignored) {
      GameLog.e(TAG, "Error loading fonts from assets/fonts.");
      return;
    }

    for (String filename : fileList) {
      @NonNls String alias = filename.substring(0, filename.lastIndexOf(DOT));
      FONT_MAPPING.put(alias, filename);
      FONT_MAPPING.put(alias.toLowerCase(), filename);
    }
  }

  @Nullable
  public Typeface get(String fontName) {
    String fontFilename = FONT_MAPPING.get(fontName);
    if (fontFilename == null) {
      GameLog.e(TAG, "Couldn't find font " + fontName + ". Maybe you need to call addFont() first?");
      return null;
    }
    if (CACHE.containsKey(fontFilename)) {
      return CACHE.get(fontFilename);
    } else {
      Typeface typeface = Typeface.createFromAsset(context.getAssets(), FONT_DIR + BACKSLASH + fontFilename);
      CACHE.put(fontFilename, typeface);
      return typeface;
    }
  }

  @BindingAdapter({"font"})
  public static void setFont(TextView view, String fontName) {
    view.setTypeface(getInstance(view.getContext()).get(fontName));
  }

}
