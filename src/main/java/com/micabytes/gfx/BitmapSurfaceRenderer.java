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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;

import com.micabytes.util.GameLog;

import org.jetbrains.annotations.NonNls;

import java.io.IOException;
import java.io.InputStream;

/**
 * GameSurfaceRendererBitmap is a renderer that handles the rendering of a background bitmap to the
 * screen (e.g., a game map). It is able to do this even if the bitmap is too large to fit into
 * memory. The game should subclass the renderer and extend the drawing methods to add other game
 * elements.
 */
public class BitmapSurfaceRenderer extends SurfaceRenderer {
  private static final String TAG = BitmapSurfaceRenderer.class.getName();
  // Default Settings
  @NonNls
  private static final String CACHE_THREAD = "cacheThread";
  public static final Bitmap.Config DEFAULT_CONFIG = Bitmap.Config.RGB_565;
  private static final int DEFAULT_SAMPLE_SIZE = 2;
  private static final int DEFAULT_MEM_USAGE = 20;
  private static final float DEFAULT_THRESHOLD = 0.75f;
  // BitmapRegionDecoder - this is the class that does the magic
  private BitmapRegionDecoder decoder;
  // The cached portion of the background image
  private final CacheBitmap cachedBitmap = new CacheBitmap();
  // The low resolution version of the background image
  private Bitmap lowResBitmap;
  /**
   * Options for loading the bitmaps
   */
  private final BitmapFactory.Options options = new BitmapFactory.Options();
  /**
   * What is the down sample size for the sample image? 1=1/2, 2=1/4 3=1/8, etc
   */
  private final int sampleSize;
  /**
   * What percent of total memory should we use for the cache? The bigger the cache, the longer it
   * takes to read -- 1.2 secs for 25%, 600ms for 10%, 500ms for 5%. User experience seems to be
   * best for smaller values.
   */
  private int memUsage;
  /**
   * Threshold for using low resolution image
   */
  private final float lowResThreshold;
  /**
   * Calculated rect
   */
  private final Rect calculatedCacheWindowRect = new Rect();

  @SuppressWarnings("unused")
  private BitmapSurfaceRenderer(Context con) {
    super(con);
    options.inPreferredConfig = DEFAULT_CONFIG;
    sampleSize = DEFAULT_SAMPLE_SIZE;
    memUsage = DEFAULT_MEM_USAGE;
    lowResThreshold = DEFAULT_THRESHOLD;
  }

  protected BitmapSurfaceRenderer(Context con, Bitmap.Config config, int sample, int memUse, float threshold) {
    super(con);
    options.inPreferredConfig = Bitmap.Config.RGB_565;
    sampleSize = 2;
    memUsage = 5;
    lowResThreshold = 0.75f;
  }

  /**
   * Set the Background bitmap
   *
   * @param inputStream InputStream to the raw data of the bitmap
   */
  public void setBitmap(InputStream inputStream) throws IOException {
    BitmapFactory.Options opt = new BitmapFactory.Options();
    decoder = BitmapRegionDecoder.newInstance(inputStream, false);
    inputStream.reset();
    // Grab the bounds of the background bitmap
    opt.inPreferredConfig = DEFAULT_CONFIG;
    opt.inJustDecodeBounds = true;
    GameLog.d(TAG, "Decode inputStream for Background Bitmap");
    BitmapFactory.decodeStream(inputStream, null, opt);
    inputStream.reset();
    backgroundSize.set(opt.outWidth, opt.outHeight);
    GameLog.i(TAG, "Background Image: w=" + opt.outWidth + " h=" + opt.outHeight);
    // Create the low resolution background
    opt.inJustDecodeBounds = false;
    opt.inSampleSize = 1 << sampleSize;
    lowResBitmap = BitmapFactory.decodeStream(inputStream, null, opt);
    GameLog.i(TAG, "Low Res Image: w=" + lowResBitmap.getWidth() + " h=" + lowResBitmap.getHeight());
    // Initialize cache
    if (cachedBitmap.getState() == CacheState.NOT_INITIALIZED) {
      synchronized (cachedBitmap) {
        cachedBitmap.setState(CacheState.IS_INITIALIZED);
      }
    }
  }

  @Override
  protected void drawBase() {
    cachedBitmap.draw(viewPort);
  }

  @Override
  protected void drawLayer() {
    // NOOP - override function and add game specific code
  }

  @Override
  protected void drawFinal() {
    // NOOP - override function and add game specific code
  }

  /**
   * Starts the renderer
   */
  @Override
  public void start() {
    cachedBitmap.start();
  }

  /**
   * Stops the renderer
   */
  @Override
  public void stop() {
    cachedBitmap.stop();
  }

  /**
   * Suspend the renderer
   */
  @Override
  public void suspend() {
    cachedBitmap.suspend();
  }

  /**
   * Suspend the renderer
   */
  @Override
  public void resume() {
    cachedBitmap.resume();
  }

  /**
   * Invalidate the cache.
   */
  public void invalidate() {
    cachedBitmap.invalidate();
  }


  /**
   * Loads the relevant slice of the background bitmap that needs to be kept in memory. <p/> The
   * loading can take a long time depending on the size.
   *
   * @param rect The portion of the background bitmap to be cached
   * @return The bitmap representing the requested area of the background
   */
  private Bitmap loadCachedBitmap(Rect rect) {
    return decoder.decodeRegion(rect, options);
  }

  /**
   * This function tries to recover from an OutOfMemoryError in the CacheThread.
   */
  private void cacheBitmapOutOfMemoryError() {
    if (memUsage > 0) memUsage -= 1;
    GameLog.e(TAG, "OutOfMemory caught; reducing cache size to " + memUsage + " percent.");
  }

  /**
   * This method fills the passed-in bitmap with sample data. This function must return data fast;
   * this is our fall back solution in all the cases where the user is moving too fast for us to
   * load the actual bitmap data from memory. The quality of the user experience rests on the speed
   * of this function.
   */
  private void drawLowResolutionBackground(Bitmap bitmap, Rect rect) {
    int left = rect.left >> sampleSize;
    int top = rect.top >> sampleSize;
    int right = rect.right >> sampleSize;
    int bottom = rect.bottom >> sampleSize;
    Rect srcRect = new Rect(left, top, right, bottom);
    Rect dstRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    // Draw to Canvas
    Canvas canvas = new Canvas(bitmap);
    canvas.drawBitmap(lowResBitmap, srcRect, dstRect, null);
  }

  /**
   * Determine the dimensions of the CacheBitmap based on the current ViewPort. <p/> Minimum size is
   * equal to the viewport; otherwise it is dimensioned relative to the available memory. {@link
   * CacheBitmap} is locked while the calculation is done, so this has to be fast.
   *
   * @param rect The dimensions of the current viewport
   * @return The dimensions of the cache
   */
  @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
  private Rect calculateCacheDimensions(Rect rect) {
    long bytesToUse = (Runtime.getRuntime().maxMemory() * memUsage) / 100;
    Point sz = getBackgroundSize();
    int vw = rect.width();
    int vh = rect.height();
    GameLog.d(TAG, "old cache.originRect = " + rect.toShortString());
    // Calculate the margins within the memory budget
    int tw = 0;
    int th = 0;
    int mw = tw;
    int mh = th;
    int bytesPerPixel = 4;
    while (((vw + tw) * (vh + th) * bytesPerPixel) < bytesToUse) {
      tw++;
      mw = tw;
      th++;
      mh = th;
    }
    // Trim margins to image size
    if ((vw + mw) > sz.x) mw = Math.max(0, sz.x - vw);
    if ((vh + mh) > sz.y) mh = Math.max(0, sz.y - vh);
    // Figure out the left & right based on the margin.
    // LATER: THe logic here assumes that the viewport is <= our size.
    // If that's not the case, then this logic breaks.
    int left = rect.left - (mw >> 1);
    int right = rect.right + (mw >> 1);
    if (left < 0) {
      right -= left; // Adds the overage on the left side back to the right
      left = 0;
    }
    if (right > sz.x) {
      left -= right - sz.x; // Adds overage on right side back to left
      right = sz.x;
    }
    // Figure out the top & bottom based on the margin. We assume our viewport
    // is <= our size. If that's not the case, then this logic breaks.
    int top = rect.top - (mh >> 1);
    int bottom = rect.bottom + (mh >> 1);
    if (top < 0) {
      bottom -= top; // Adds the overage on the top back to the bottom
      top = 0;
    }
    if (bottom > sz.y) {
      top -= bottom - sz.y; // Adds overage on bottom back to top
      bottom = sz.y;
    }
    // Set the origin based on our new calculated values.
    calculatedCacheWindowRect.set(left, top, right, bottom);
    GameLog.d(TAG, "new cache.originRect = " + calculatedCacheWindowRect.toShortString() + " size=" + sz);
    return calculatedCacheWindowRect;
  }


  /**
   * The current state of the cached bitmap
   */
  private enum CacheState {
    READY, NOT_INITIALIZED, IS_INITIALIZED, BEGIN_UPDATE, IS_UPDATING, DISABLED
  }

  /**
   * The cached bitmap object. This object is continually kept onTouchUp to date by CacheThread. If
   * the object is locked, the background is updated using the low resolution background image
   * instead
   */
  @SuppressWarnings("AssignmentToNull")
  private class CacheBitmap {
    /**
     * The current position and dimensions of the cache within the background image
     */
    final Rect cacheWindow = new Rect(0, 0, 0, 0);
    /**
     * The current state of the cache
     */
    private CacheState state = CacheState.NOT_INITIALIZED;
    /**
     * The currently cached bitmap
     */
    Bitmap bitmap;
    /**
     * The cache bitmap loading thread
     */
    private CacheThread cacheThread;

    synchronized CacheState getState() {
      return state;
    }

    synchronized void setState(CacheState newState) {
      state = newState;
    }

    synchronized void start() {
      if (cacheThread != null) {
        cacheThread.setRunning(false);
        cacheThread.interrupt();
        cacheThread = null;
      }
      cacheThread = new CacheThread(this);
      cacheThread.setName(CACHE_THREAD);
      cacheThread.start();
    }

    synchronized void stop() {
      cacheThread.setRunning(false);
      cacheThread.interrupt();
      boolean retry = true;
      while (retry) {
        try {
          cacheThread.join();
          retry = false;
        } catch (InterruptedException ignored) {
          // Wait until thread is dead
        }
      }
      cacheThread = null;
    }

    synchronized void invalidate() {
      setState(CacheState.IS_INITIALIZED);
      cacheThread.interrupt();
    }

    public synchronized void suspend() {
      setState(CacheState.DISABLED);
    }

    public void resume() {
      if (getState() == CacheState.DISABLED) {
        synchronized (this) {
          setState(CacheState.IS_INITIALIZED);
        }
      }
    }

    /**
     * Draw the CacheBitmap on the viewport
     */
    void draw(ViewPort p) {
      Bitmap bmp = null;
      synchronized (this) {
        switch (getState()) {
          case NOT_INITIALIZED:
            // Error
            GameLog.e(TAG, "Attempting to update an uninitialized CacheBitmap");
            return;
          case IS_INITIALIZED:
            // Start data caching
            setState(CacheState.BEGIN_UPDATE);
            cacheThread.interrupt();
            break;
          case BEGIN_UPDATE:
          case IS_UPDATING:
            // Currently updating; low resolution version used
            break;
          case DISABLED:
            // Use of high resolution version disabled
            break;
          case READY:
            if ((bitmap == null) || !cacheWindow.contains(p.getWindow())) {
              // No data loaded OR No cached data available
              setState(CacheState.BEGIN_UPDATE);
              cacheThread.interrupt();
            } else {
              bmp = bitmap;
            }
            break;
        }
      }
      // Use the low resolution version if the cache is empty or scale factor is < threshold
      if ((bmp == null) || (getZoom() < lowResThreshold))
        drawLowResolution();
      else
        drawHighResolution(bmp);
    }

    /**
     * Used to hold the source Rect for bitmap drawing
     */
    private final Rect srcRect = new Rect(0, 0, 0, 0);
    /**
     * Used to hold the dest Rect for bitmap drawing
     */
    private final Rect dstRect = new Rect(0, 0, 0, 0);
    private final Point dstSize = new Point();

    /**
     * Use the high resolution cached bitmap for drawing
     */
    void drawHighResolution(Bitmap bmp) {
      Rect wSize = viewPort.getWindow();
      if (bmp != null) {
        synchronized (viewPort) {
          int left = wSize.left - cacheWindow.left;
          int top = wSize.top - cacheWindow.top;
          int right = left + wSize.width();
          int bottom = top + wSize.height();
          viewPort.getPhysicalSize(dstSize);
          srcRect.set(left, top, right, bottom);
          dstRect.set(0, 0, dstSize.x, dstSize.y);
          Canvas canvas = new Canvas(viewPort.getBitmap());
          canvas.drawColor(Color.BLACK);
          canvas.drawBitmap(bmp, srcRect, dstRect, null);
        }
      }
    }

    void drawLowResolution() {
      if (getState() != CacheState.NOT_INITIALIZED) {
        synchronized (viewPort) {
          drawLowResolutionBackground(viewPort.getBitmap(), viewPort.getWindow());
        }
      }
    }

  }

  /**
   * This thread handles the background loading of the {@link CacheBitmap}. <p/> The CacheThread
   * starts an update when the {@link CacheBitmap#state} is {@link CacheState#BEGIN_UPDATE} and
   * updates the bitmap given the current window. <p/> The CacheThread needs to be careful how it
   * locks {@link CacheBitmap} in order to ensure the smoothest possible performance (loading can
   * take a while).
   */
  @SuppressWarnings("SameParameterValue")
  class CacheThread extends Thread {
    private boolean isRunning;
    // The CacheBitmap
    private final CacheBitmap cache;

    CacheThread(CacheBitmap cached) {
      setName(CACHE_THREAD);
      cache = cached;
    }

    @SuppressWarnings({"MethodWithMultipleLoops", "OverlyComplexMethod", "OverlyNestedMethod", "WhileLoopSpinsOnField", "RefusedBequest", "OverlyLongMethod"})
    @Override
    public void run() {
      isRunning = true;
      Rect viewportRect = new Rect(0, 0, 0, 0);
      while (isRunning) {
        // Wait until we are ready to go
        while (isRunning && (cache.getState() != CacheState.BEGIN_UPDATE)) {
          try {
            //noinspection BusyWait
            Thread.sleep(Integer.MAX_VALUE);
          } catch (InterruptedException ignored) {
            // NOOP
          }
        }
        if (!isRunning) return;
        // Start Loading Timer
        long startTime = System.currentTimeMillis();
        // Load Data
        boolean startLoading = false;
        synchronized (cache) {
          if (cache.getState() == CacheState.BEGIN_UPDATE) {
            cache.setState(CacheState.IS_UPDATING);
            cache.bitmap = null;
            startLoading = true;
          }
        }
        if (startLoading) {
          synchronized (viewPort) {
            viewportRect.set(viewPort.getWindow());
          }
          boolean continueLoading = false;
          synchronized (cache) {
            if (cache.getState() == CacheState.IS_UPDATING) {
              cache.cacheWindow.set(calculateCacheDimensions(viewportRect));
              continueLoading = true;
            }
          }
          if (continueLoading) {
            //noinspection ErrorNotRethrown
            try {
              Bitmap bitmap = loadCachedBitmap(cache.cacheWindow);
              if (bitmap != null) {
                synchronized (cache) {
                  if (cache.getState() == CacheState.IS_UPDATING) {
                    cache.bitmap = bitmap;
                    cache.setState(CacheState.READY);
                  } else {
                    GameLog.w(TAG, "Loading of background image cache aborted");
                  }
                }
              }
              // End Loading Timer
              long endTime = System.currentTimeMillis();
              GameLog.d(TAG, "Loaded background image in " + (endTime - startTime) + " ms");
            } catch (OutOfMemoryError ignored) {
              GameLog.d(TAG, "CacheThread out of memory");
              // Out of memory ERROR detected. Lower the memory allocation
              synchronized (cache) {
                cacheBitmapOutOfMemoryError();
                if (cache.getState() == CacheState.IS_UPDATING) {
                  cache.setState(CacheState.BEGIN_UPDATE);
                }
              }
            }
          }
        }
      }
    }

    public void setRunning(boolean running) {
      isRunning = running;
    }

  }

}
