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
package com.micabytes.gfx

import android.graphics.*
import timber.log.Timber
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * GameSurfaceRendererBitmap is a renderer that handles the rendering of a background bitmap to the
 * screen (e.g., a game map). It is able to do this even if the bitmap is too large to fit into
 * memory. The game should subclass the renderer and extend the drawing methods to add other game
 * elements.
 */
open class BitmapSurfaceRenderer : SurfaceRenderer {
  private val TAG = BitmapSurfaceRenderer::class.java.name
  private val CACHE_THREAD = "cacheThread"
  private val DEFAULT_CONFIG: Bitmap.Config = Bitmap.Config.RGB_565
  private val DEFAULT_SAMPLE_SIZE = 2
  private val DEFAULT_MEM_USAGE = 20
  private val DEFAULT_THRESHOLD = 0.75f
  // BitmapRegionDecoder - this is the class that does the magic
  private var decoder: BitmapRegionDecoder? = null
  // The cached portion of the background image
  private val cachedBitmap = CacheBitmap()
  // The low resolution version of the background image
  private var lowResBitmap: Bitmap? = null
  /**
   * Options for loading the bitmaps
   */
  private val options = BitmapFactory.Options()
  /**
   * What is the down sample size for the sample image? 1=1/2, 2=1/4 3=1/8, etc
   */
  private val sampleSize: Int
  /**
   * What percent of total memory should we use for the cache? The bigger the cache, the longer it
   * takes to read -- 1.2 secs for 25%, 600ms for 10%, 500ms for 5%. User experience seems to be
   * best for smaller values.
   */
  @get:Synchronized @set:Synchronized private var memUsage: Int = 0
  /**
   * Threshold for using low resolution image
   */
  private val lowResThreshold: Float
  /**
   * Calculated rect
   */
  private val calculatedCacheWindowRect = Rect()

  private constructor() : super() {
    options.inPreferredConfig = DEFAULT_CONFIG
    sampleSize = DEFAULT_SAMPLE_SIZE
    memUsage = DEFAULT_MEM_USAGE
    lowResThreshold = DEFAULT_THRESHOLD
  }

  protected constructor(config: Bitmap.Config, sample: Int, memUse: Int, threshold: Float) : super() {
    options.inPreferredConfig = config
    sampleSize = sample
    memUsage = memUse
    lowResThreshold = threshold
  }

  internal class FlushedInputStream(inputStream: InputStream) : FilterInputStream(inputStream) {

    @Throws(IOException::class)
    override fun skip(byteCount: Long): Long {
      var totalBytesSkipped = 0L
      while (totalBytesSkipped < byteCount) {
        var bytesSkipped = `in`.skip(byteCount - totalBytesSkipped)
        if (bytesSkipped == 0L) {
          val bytes = read()
          if (bytes < 0) {
            break  // we reached EOF
          } else {
            bytesSkipped = 1 // we read one byte
          }
        }
        totalBytesSkipped += bytesSkipped
      }
      return totalBytesSkipped
    }
  }

  /**
   * Set the Background bitmap
   *
   * @param inputStream InputStream to the raw data of the bitmap
   */
  @Throws(IOException::class)
  fun setBitmap(inputStream: InputStream) {
    val fixedInput = FlushedInputStream(inputStream)
    val opt = BitmapFactory.Options()
    decoder = BitmapRegionDecoder.newInstance(fixedInput, false)
    fixedInput.reset()
    // Grab the bounds of the background bitmap
    opt.inPreferredConfig = DEFAULT_CONFIG
    opt.inJustDecodeBounds = true
    Timber.d(TAG, "Decode inputStream for Background Bitmap")
    BitmapFactory.decodeStream(fixedInput, null, opt)
    fixedInput.reset()
    backgroundSize.set(opt.outWidth, opt.outHeight)
    Timber.d(TAG, "Background Image: w=" + opt.outWidth + " h=" + opt.outHeight)
    // Create the low resolution background
    opt.inJustDecodeBounds = false
    opt.inSampleSize = 1 shl sampleSize
    lowResBitmap = BitmapFactory.decodeStream(fixedInput, null, opt)
    Timber.d(TAG, "Low Res Image: w=" + lowResBitmap!!.width + " h=" + lowResBitmap!!.height)
    // Initialize cache
    if (cachedBitmap.state == CacheState.NOT_INITIALIZED) {
      synchronized(cachedBitmap) {
        cachedBitmap.state = CacheState.IS_INITIALIZED
      }
    }
  }

  override fun drawBase() {
    cachedBitmap.draw(viewPort)
  }

  override fun drawLayer() {
    // NOOP - override function and add game specific code
  }

  override fun drawFinal() {
    // NOOP - override function and add game specific code
  }

  /**
   * Starts the renderer
   */
  override fun start() {
    cachedBitmap.start()
  }

  /**
   * Stops the renderer
   */
  override fun stop() {
    cachedBitmap.stop()
  }

  /**
   * Suspend the renderer
   */
  override fun suspend() {
    cachedBitmap.suspend()
  }

  /**
   * Suspend the renderer
   */
  override fun resume() {
    cachedBitmap.resume()
  }

  /**
   * Invalidate the cache.
   */
  fun invalidate() {
    cachedBitmap.invalidate()
  }

  /**
   * The current state of the cached bitmap
   */
  internal enum class CacheState {
    READY, NOT_INITIALIZED, IS_INITIALIZED, BEGIN_UPDATE, IS_UPDATING, DISABLED
  }

  /**
   * The cached bitmap object. This object is continually kept up to date by CacheThread. If
   * the object is locked, the background is updated using the low resolution background image
   * instead
   */
  internal inner class CacheBitmap {
    /**
     * The current position and dimensions of the cache within the background image
     */
    internal val cacheWindow = Rect(0, 0, 0, 0)
    /**
     * The current state of the cache
     */
    @get:Synchronized @set:Synchronized internal var state = CacheState.NOT_INITIALIZED
    /**
     * The currently cached bitmap
     */
    internal var bitmap: Bitmap? = null
    /**
     * The cache bitmap loading thread
     */
    private var cacheThread: CacheThread? = null

    /**
     * Used to hold the source Rect for bitmap drawing
     */
    private val srcRect = Rect(0, 0, 0, 0)
    /**
     * Used to hold the dest Rect for bitmap drawing
     */
    private val dstRect = Rect(0, 0, 0, 0)
    private val dstSize = Point()

    internal fun start() {
      if (cacheThread != null) {
        cacheThread!!.setRunning(false)
        cacheThread!!.interrupt()
        cacheThread = null
      }
      cacheThread = CacheThread(this)
      cacheThread!!.name = CACHE_THREAD
      cacheThread!!.start()
    }

    internal fun stop() {
      cacheThread?.setRunning(false)
      cacheThread?.interrupt()
      var retry = true
      while (retry) {
        try {
          cacheThread?.join()
          retry = false
        } catch (ignored: InterruptedException) {
          // Wait until thread is dead
        }

      }
      cacheThread = null
    }

    internal fun invalidate() {
      state = CacheState.IS_INITIALIZED
      cacheThread!!.interrupt()
    }

    fun suspend() {
      state = CacheState.DISABLED
    }

    fun resume() {
      if (state == CacheState.DISABLED) {
        state = CacheState.IS_INITIALIZED
      }
    }

    /**
     * Draw the CacheBitmap on the viewport
     */
    internal fun draw(p: SurfaceRenderer.ViewPort) {
      if (cacheThread == null) return
      var bmp: Bitmap? = null
      when (state) {
        BitmapSurfaceRenderer.CacheState.NOT_INITIALIZED -> {
          // Error
          Timber.e(TAG, "Attempting to update an uninitialized CacheBitmap")
          return
        }
        BitmapSurfaceRenderer.CacheState.IS_INITIALIZED -> {
          // Start data caching
          state = CacheState.BEGIN_UPDATE
          cacheThread!!.interrupt()
        }
        BitmapSurfaceRenderer.CacheState.BEGIN_UPDATE, BitmapSurfaceRenderer.CacheState.IS_UPDATING -> {
        }
        BitmapSurfaceRenderer.CacheState.DISABLED -> {
        }
        BitmapSurfaceRenderer.CacheState.READY -> if (bitmap == null || !cacheWindow.contains(p.window)) {
          // No data loaded OR No cached data available
          state = CacheState.BEGIN_UPDATE
          cacheThread!!.interrupt()
        } else {
          bmp = bitmap
        }
      }// Currently updating; low resolution version used
      // Use of high resolution version disabled
      // Use the low resolution version if the cache is empty or scale factor is < threshold
      if (bmp == null || zoom < lowResThreshold)
        drawLowResolution()
      else
        drawHighResolution(bmp)
    }

    /**
     * Use the high resolution cached bitmap for drawing
     */
    private fun drawHighResolution(bmp: Bitmap?) {
      val wSize = viewPort.window
      if (bmp != null) {
        synchronized(viewPort) {
          val left = wSize.left - cacheWindow.left
          val top = wSize.top - cacheWindow.top
          val right = left + wSize.width()
          val bottom = top + wSize.height()
          viewPort.getPhysicalSize(dstSize)
          srcRect.set(left, top, right, bottom)
          dstRect.set(0, 0, dstSize.x, dstSize.y)
          synchronized(viewPort.bitmapLock) {
            val canvas = Canvas(viewPort.bitmap!!)
            canvas.drawColor(Color.BLACK)
            canvas.drawBitmap(bmp, srcRect, dstRect, null)
          }
        }
      }
    }

    private fun drawLowResolution() {
      if (state != CacheState.NOT_INITIALIZED) {
        drawLowResolutionBackground()
      }
    }

    /**
     * This method fills the passed-in bitmap with sample data. This function must return data fast;
     * this is our fall back solution in all the cases where the user is moving too fast for us to
     * load the actual bitmap data from memory. The quality of the user experience rests on the speed
     * of this function.
     */
    private fun drawLowResolutionBackground() {
      var w: Int
      var h: Int
      synchronized(viewPort.bitmapLock) {
        if (viewPort.bitmap == null) return
        w = viewPort.bitmap!!.width
        h = viewPort.bitmap!!.height
      }
      val rect = viewPort.window
      val left = rect.left shr sampleSize
      val top = rect.top shr sampleSize
      val right = rect.right shr sampleSize
      val bottom = rect.bottom shr sampleSize
      val sRect = Rect(left, top, right, bottom)
      val dRect = Rect(0, 0, w, h)
      // Draw to Canvas
      synchronized(viewPort.bitmapLock) {
        if (viewPort.bitmap != null && lowResBitmap != null) {
          val canvas = Canvas(viewPort.bitmap!!)
          canvas.drawBitmap(lowResBitmap!!, sRect, dRect, null)
        }
      }
    }

  }

  /**
   * This thread handles the background loading of the [CacheBitmap].
   *
   * The CacheThread
   * starts an update when the [CacheBitmap.state] is [CacheState.BEGIN_UPDATE] and
   * updates the bitmap given the current window.
   *
   * The CacheThread needs to be careful how it
   * locks [CacheBitmap] in order to ensure the smoothest possible performance (loading can
   * take a while).
   */
  internal inner class CacheThread(private val cache: CacheBitmap) : Thread() {
    private var running: Boolean = false

    init {
      name = CACHE_THREAD
    }

    override fun run() {
      running = true
      val viewportRect = Rect(0, 0, 0, 0)
      while (running) {
        // Wait until we are ready to go
        while (running && cache.state != CacheState.BEGIN_UPDATE) {
          try {

            Thread.sleep(Integer.MAX_VALUE.toLong())
          } catch (ignored: InterruptedException) {
            // NOOP
          }

        }
        if (!running) return
        // Start Loading Timer
        val startTime = System.currentTimeMillis()
        // Load Data
        var startLoading = false
        synchronized(cache) {
          if (cache.state == CacheState.BEGIN_UPDATE) {
            cache.state = CacheState.IS_UPDATING
            cache.bitmap = null
            startLoading = true
          }
        }
        if (startLoading) {
          synchronized(viewPort) {
            viewportRect.set(viewPort.window)
          }
          var continueLoading = false
          synchronized(cache) {
            if (cache.state == CacheState.IS_UPDATING) {
              cache.cacheWindow.set(calculateCacheDimensions(viewportRect))
              continueLoading = true
            }
          }
          if (continueLoading) {

            try {
              val bitmap = loadCachedBitmap(cache.cacheWindow)
              if (bitmap != null) {
                synchronized(cache) {
                  if (cache.state == CacheState.IS_UPDATING) {
                    cache.bitmap = bitmap
                    cache.state = CacheState.READY
                  } else {
                    Timber.d(TAG, "Loading of background image cache aborted")
                  }
                }
              }
              // End Loading Timer
              val endTime = System.currentTimeMillis()
              Timber.d("Loaded background image in ${(endTime - startTime)} ms")
            } catch (ignored: OutOfMemoryError) {
              Timber.w(TAG, "CacheThread out of memory")
              // Out of memory ERROR detected. Lower the memory allocation
              cacheBitmapOutOfMemoryError()
              synchronized(cache) {
                if (cache.state == CacheState.IS_UPDATING) {
                  cache.state = CacheState.BEGIN_UPDATE
                }
              }
            }

          }
        }
      }
    }

    /**
     * Determine the dimensions of the CacheBitmap based on the current ViewPort.
     *
     * Minimum size is
     * equal to the viewport; otherwise it is dimensioned relative to the available memory. [ ] is locked while the calculation is done, so this has to be fast.
     *
     * @param rect The dimensions of the current viewport
     * @return The dimensions of the cache
     */
    private fun calculateCacheDimensions(rect: Rect): Rect {
      val bytesToUse = Runtime.getRuntime().maxMemory() * memUsage / 100
      val sz = backgroundSize
      val vw = rect.width()
      val vh = rect.height()
      // Calculate the margins within the memory budget
      var tw = 0
      var th = 0
      var mw = tw
      var mh = th
      val bytesPerPixel = 4
      while ((vw + tw) * (vh + th) * bytesPerPixel < bytesToUse) {
        tw++
        mw = tw
        th++
        mh = th
      }
      // Trim margins to image size
      if (vw + mw > sz.x) mw = Math.max(0, sz.x - vw)
      if (vh + mh > sz.y) mh = Math.max(0, sz.y - vh)
      // Figure out the left & right based on the margin.
      // LATER: THe logic here assumes that the viewport is <= our size.
      // If that's not the case, then this logic breaks.
      var left = rect.left - (mw shr 1)
      var right = rect.right + (mw shr 1)
      if (left < 0) {
        right -= left // Adds the overage on the left side back to the right
        left = 0
      }
      if (right > sz.x) {
        left -= right - sz.x // Adds overage on right side back to left
        right = sz.x
      }
      // Figure out the top & bottom based on the margin. We assume our viewport
      // is <= our size. If that's not the case, then this logic breaks.
      var top = rect.top - (mh shr 1)
      var bottom = rect.bottom + (mh shr 1)
      if (top < 0) {
        bottom -= top // Adds the overage on the top back to the bottom
        top = 0
      }
      if (bottom > sz.y) {
        top -= bottom - sz.y // Adds overage on bottom back to top
        bottom = sz.y
      }
      // Set the origin based on our new calculated values.
      calculatedCacheWindowRect.set(left, top, right, bottom)
      return calculatedCacheWindowRect
    }

    fun setRunning(r: Boolean) {
      running = r
    }

    /**
     * Loads the relevant slice of the background bitmap that needs to be kept in memory.
     *
     * The
     * loading can take a long time depending on the size.
     *
     * @param rect The portion of the background bitmap to be cached
     * @return The bitmap representing the requested area of the background
     */
    private fun loadCachedBitmap(rect: Rect): Bitmap? {
      return decoder!!.decodeRegion(rect, options)
    }

    /**
     * This function tries to recover from an OutOfMemoryError in the CacheThread.
     */
    private fun cacheBitmapOutOfMemoryError() {
      if (memUsage > 0) memUsage -= 1
      Timber.e(TAG, "OutOfMemory caught; reducing cache size to $memUsage percent.")
    }

  }

}
