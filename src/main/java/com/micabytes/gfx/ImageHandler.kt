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

import android.app.ActivityManager
import android.content.Context
import android.content.res.Resources
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.util.LruCache
import android.util.DisplayMetrics
import android.widget.ImageView

import com.micabytes.GameApplication

import de.hdodenhof.circleimageview.CircleImageView

/**
 * ImageHandler is a singleton class that is used to manage bitmaps resources used programmatically
 * in the app (i.e., not bitmaps assigned in layouts). By allocating and managing them in a central
 * cache, we avoid the problem of leaking bitmaps in the code (assuming one doesn't copy them) and
 * reuse bitmaps where possible. Bitmaps are also placed in a SoftReference so that - at least in
 * theory - they are purged when memory runs low.
 */
class ImageHandler private constructor() {

  init {
    initCache()
  }

  companion object {
    private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888
    private val COLOR_RED = -0xbdbdbe
    private val PIXEL_ROUNDING = 12
    val DENSITY_MINIMUM = 0.1f
    val MEGABYTE = 1024
    var density = 0.0f
      get() {
        if (field < DENSITY_MINIMUM) {
          val context = GameApplication.instance
          val resources = context.resources
          val metrics = resources.displayMetrics
          return metrics.density
        }
        return field
      }
    private set(i) {
      field = i
      if (density < DENSITY_MINIMUM) {
        val context = GameApplication.instance
        val resources = context.resources
        val metrics = resources.displayMetrics
        field = metrics.density
      }
    }
    // Bitmap cache
    private var memoryCache: LruCache<Int, Bitmap>? = null

    private fun initCache() {
      val memoryClass = (GameApplication.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
      val memoryCacheSize = MEGABYTE * MEGABYTE * memoryClass / 8
      memoryCache = object : LruCache<Int, Bitmap>(memoryCacheSize) {
        override fun sizeOf(key: Int?, value: Bitmap): Int {
          return value.rowBytes * value.height
        }
      }
    }

    @JvmStatic
    fun getJ(key: Int) = get(key)

    @JvmOverloads
    operator fun get(key: Int, config: Bitmap.Config = DEFAULT_CONFIG): Bitmap {
      //if (key == 0)
      //  GameLog.d(TAG, "Null resource sent to get()");
      if (memoryCache == null) initCache()
      val cached = memoryCache!!.get(key)
      if (cached != null)
        return cached
      val ret = loadBitmap(key, config)
      if (ret == null) {
        val conf = Bitmap.Config.ARGB_8888
        return Bitmap.createBitmap(1, 1, conf)
      }
      memoryCache!!.put(key, ret)
      return ret
    }

    private fun loadBitmap(key: Int, bitmapConfig: Bitmap.Config): Bitmap? {
      if (key == 0) return null
      val opts = BitmapFactory.Options()
      opts.inPreferredConfig = bitmapConfig
      return BitmapFactory.decodeResource(GameApplication.instance.resources, key, opts)
    }

    fun getSceneBitmap(bkg: Int, left: Int, right: Int): Bitmap? {
      val bitmap = get(bkg)
      val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(output)
      val paint = Paint()
      val rect = Rect(0, 0, bitmap.width, bitmap.height)
      val rectF = RectF(rect)
      paint.isAntiAlias = true
      canvas.drawARGB(0, 0, 0, 0)
      paint.color = COLOR_RED
      val roundPx = PIXEL_ROUNDING.toFloat()
      canvas.drawRoundRect(rectF, roundPx, roundPx, paint)
      paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
      canvas.drawBitmap(bitmap, rect, rect, paint)
      if (left > 0) {
        canvas.drawBitmap(get(left), 0f, 0f, null)
      }
      if (right > 0) {
        canvas.drawBitmap(get(right), 0f, 0f, null)
        // canvas.drawBitmap(get(right), bitmap.getWidth()/2, 0, null);
      }
      return output
    }

    fun getDimensions(key: Int): BitmapFactory.Options {
      val opt = BitmapFactory.Options()
      opt.inPreferredConfig = BitmapSurfaceRenderer.DEFAULT_CONFIG
      opt.inJustDecodeBounds = true
      BitmapFactory.decodeResource(GameApplication.instance.resources, key, opt)
      return opt
    }

  }

}
