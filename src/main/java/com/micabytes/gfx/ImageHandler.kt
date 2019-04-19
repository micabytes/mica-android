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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.LruCache
import com.micabytes.Game

/**
 * ImageHandler is a singleton class that is used to manage bitmaps resources used programmatically
 * in the app (i.e., not bitmaps assigned in layouts). By allocating and managing them in a central
 * cache, we avoid the problem of leaking bitmaps in the code (assuming one doesn't copy them) and
 * reuse bitmaps where possible. Bitmaps are also placed in a SoftReference so that - at least in
 * theory - they are purged when memory runs low.
 */
object ImageHandler {
  private val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888
  private const val DENSITY_MINIMUM = 0.1f
  private const val MEGABYTE = 1024
  var density = 0.0f
    get() {
      if (field < DENSITY_MINIMUM) {
        val context = Game.instance
        val resources = context.resources
        val metrics = resources.displayMetrics
        return metrics.density
      }
      return field
    }
    private set(i) {
      field = i
      if (density < DENSITY_MINIMUM) {
        val context = Game.instance
        val resources = context.resources
        val metrics = resources.displayMetrics
        field = metrics.density
      }
    }
  // Bitmap cache
  private var memoryCache: LruCache<Int, Bitmap>? = null

  init {
    initCache()
  }

  private fun initCache() {
    val memoryClass = (Game.instance.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).memoryClass
    val memoryCacheSize = MEGABYTE * MEGABYTE * memoryClass / 8
    memoryCache = object : LruCache<Int, Bitmap>(memoryCacheSize) {
      override fun sizeOf(key: Int?, value: Bitmap): Int {
        return value.rowBytes * value.height
      }
    }
  }

  fun getBitmapKey(id: String): Int {
    val context = Game.instance
    val key = context.resources.getIdentifier(id, "drawable", context.packageName)
    return key
  }

  fun getBitmap(id: String): Bitmap {
    val context = Game.instance
    val key = context.resources.getIdentifier(id, "drawable", context.packageName)
    return get(key)
  }

  @JvmStatic
  fun getJ(key: Int) = get(key)

  @JvmOverloads
  operator fun get(key: Int, config: Bitmap.Config = DEFAULT_CONFIG): Bitmap {
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

  @JvmOverloads
  operator fun get(key: String, config: Bitmap.Config = DEFAULT_CONFIG): Bitmap = get(Game.instance.resources.getIdentifier(key, "drawable", Game.instance.packageName), config)

  @JvmOverloads
  operator fun get(key: Int, width: Int, height: Int, config: Bitmap.Config = DEFAULT_CONFIG): Bitmap {
    if (key == 0 || width == 0 || height == 0)
      return Bitmap.createBitmap(1, 1, config)
    val res = Game.instance.resources
    return BitmapFactory.Options().run {
      inJustDecodeBounds = true
      BitmapFactory.decodeResource(res, key, this)
      // Calculate inSampleSize
      inSampleSize = calculateInSampleSize(this, width, height)
      // Decode bitmap with inSampleSize set
      inJustDecodeBounds = false
      BitmapFactory.decodeResource(res, key, this)
    }
  }

  private fun loadBitmap(key: Int, bitmapConfig: Bitmap.Config): Bitmap? {
    if (key == 0) return null
    val opts = BitmapFactory.Options()
    opts.inPreferredConfig = bitmapConfig
    return BitmapFactory.decodeResource(Game.instance.resources, key, opts)
  }

  fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    // Raw height and width of image
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
      val halfHeight: Int = height / 2
      val halfWidth: Int = width / 2
      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while (((halfHeight / inSampleSize) >= reqHeight) && ((halfWidth / inSampleSize) >= reqWidth)) {
        inSampleSize *= 2
      }
    }
    return inSampleSize
  }

  fun getDimensions(key: Int): BitmapFactory.Options {
    val opt = BitmapFactory.Options()
    opt.inPreferredConfig = Bitmap.Config.RGB_565
    opt.inJustDecodeBounds = true
    BitmapFactory.decodeResource(Game.instance.resources, key, opt)
    return opt
  }

}

fun Bitmap.flip(): Bitmap {
  val matrix = Matrix().apply { postScale(-1f, 1f, width/2f, width/2f) }
  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}