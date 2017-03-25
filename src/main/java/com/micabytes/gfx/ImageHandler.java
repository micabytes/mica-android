/*
 * Copyright 2013 MicaByte Systems
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.micabytes.gfx;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.LruCache;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.micabytes.GameApplication;
import com.micabytes.util.GameLog;

/**
 * ImageHandler is a singleton class that is used to manage bitmaps resources used programmatically
 * in the app (i.e., not bitmaps assigned in layouts). By allocating and managing them in a central
 * cache, we avoid the problem of leaking bitmaps in the code (assuming one doesn't copy them) and
 * reuse bitmaps where possible. Bitmaps are also placed in a SoftReference so that - at least in
 * theory - they are purged when memory runs low.
 */
@SuppressWarnings("UtilityClass")
public final class ImageHandler {
  private static final String TAG = ImageHandler.class.getName();
  private static final Bitmap.Config DEFAULT_CONFIG = Bitmap.Config.ARGB_8888;
  private static final int COLOR_RED = 0xff424242;
  private static final int PIXEL_ROUNDING = 12;
  public static final float DENSITY_MINIMUM = 0.1f;
  public static final int MEGABYTE = 1024;
  private static float density = 0.0f;
  // Bitmap cache
  private static LruCache<Integer, Bitmap> memoryCache = null;

  private ImageHandler() {
    initCache();
  }

  private static void initCache() {
    int memoryClass = ((ActivityManager) GameApplication.getInstance().getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
    int memoryCacheSize = MEGABYTE * MEGABYTE * memoryClass / 8;
    memoryCache = new LruCache<Integer, Bitmap>(memoryCacheSize) {
      @Override
      protected int sizeOf(Integer key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
      }
    };
  }

  public static float getDensity() {
    setDensity();
    return density;
  }

  private static void setDensity() {
    if (density < DENSITY_MINIMUM) {
      Context context = GameApplication.getInstance();
      Resources resources = context.getResources();
      DisplayMetrics metrics = resources.getDisplayMetrics();
      density = metrics.density;
    }
  }

  @NonNull
  public static Bitmap get(int key) {
    return get(key, DEFAULT_CONFIG);
  }

  @NonNull
  public static Bitmap get(int key, Bitmap.Config config) {
    if (key == 0)
      GameLog.d(TAG, "Null resource sent to get()");
    if (memoryCache == null) initCache();
    Bitmap cached = memoryCache.get(key);
    if (cached != null)
      return cached;
    Bitmap ret = loadBitmap(key, config);
    if (ret == null) {
      Bitmap.Config conf = Bitmap.Config.ARGB_8888;
      return Bitmap.createBitmap(1, 1, conf);
    }
    memoryCache.put(key, ret);
    return ret;
  }

  @Nullable
  private static Bitmap loadBitmap(int key, Bitmap.Config bitmapConfig) {
    if (key == 0) return null;
    BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = bitmapConfig;
    return BitmapFactory.decodeResource(GameApplication.getInstance().getResources(), key, opts);
  }

  @SuppressWarnings("unused")
  @Nullable
  public static Bitmap getSceneBitmap(int bkg, int left, int right) {
    Bitmap bitmap = get(bkg);
    Bitmap output =
        Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    Paint paint = new Paint();
    Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    RectF rectF = new RectF(rect);
    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    int color = COLOR_RED;
    paint.setColor(color);
    float roundPx = PIXEL_ROUNDING;
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);
    if (left > 0) {
      canvas.drawBitmap(get(left), 0, 0, null);
    }
    if (right > 0) {
      canvas.drawBitmap(get(right), 0, 0, null);
      // canvas.drawBitmap(get(right), bitmap.getWidth()/2, 0, null);
    }
    return output;
  }

  @NonNull
  public static BitmapFactory.Options getDimensions(int key) {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    opt.inPreferredConfig = BitmapSurfaceRenderer.DEFAULT_CONFIG;
    opt.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(GameApplication.getInstance().getResources(), key, opt);
    return opt;
  }

  @BindingAdapter({"src"})
  public static void setImageViewResource(ImageView imageView, int resource) {
    imageView.setImageResource(resource);
  }

  @BindingAdapter({"pix"})
  public static void setImageViewResource(ImageView imageView, Bitmap bmp) {
    imageView.setImageBitmap(bmp);
  }

}
