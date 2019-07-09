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
package com.micabytes.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import com.micabytes.gfx.SurfaceRenderer
import com.micabytes.util.Array2D
import timber.log.Timber

/** TileMap superclass  */
abstract class TileMap (protected val zones: Array2D<TileMapZone?>) {
  protected var scaleFactor: Float = 0.toFloat()
  protected val viewPortOrigin = Point()
  protected val viewPortSize = Point()
  protected val destRect = Rect()
  protected var windowLeft: Int = 0
  protected var windowTop: Int = 0
  protected var windowRight: Int = 0
  protected var windowBottom: Int = 0
  val standardOrientation = true
  // Draw
  protected val canvas = Canvas()

  val renderHeight: Int
    get() = mapHeight * tileRect.height()

  val renderWidth: Int
    get() = mapWidth * tileRect.width()

  val tileHeight: Int
    get() = tileRect.height()

  val tileWidth: Int
    get() = tileRect.width()

  open fun drawBase(context: Context, p: SurfaceRenderer.ViewPort) {
    if (p.bitmap == null) {
      Timber.e("Viewport bitmap is null in TileMap")
      return
    }
    canvas.setBitmap(p.bitmap)
    val paint = Paint()
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    paint.isDither = true
    val scaleFactor = p.zoom
    val tileSize = tileRect.width()
    p.getOrigin(viewPortOrigin)
    p.getSize(viewPortSize)
    val windowLeft = viewPortOrigin.x
    val windowTop = viewPortOrigin.y
    val windowRight = viewPortOrigin.x + viewPortSize.x
    val windowBottom = viewPortOrigin.y + viewPortSize.y
    // Clip tiles not in view
    var iMn = windowLeft / tileSize
    if (iMn < 0) iMn = 0
    var jMn = windowTop / tileSize
    if (jMn < 0) jMn = 0
    var iMx = windowRight / tileSize + 1
    if (iMx >= mapWidth) iMx = mapWidth
    var jMx = windowBottom / tileSize + 1
    if (jMx >= mapHeight) jMx = mapHeight
    // Draw Tiles
    for (i in iMn until iMx) {
      for (j in jMn until jMx) {
        if (zones[i, j] != null) {
          destRect.left = ((i * tileSize - windowLeft) / scaleFactor).toInt()
          destRect.top = ((j * tileSize - windowTop) / scaleFactor).toInt()
          destRect.right = ((i * tileSize + tileSize - windowLeft) / scaleFactor).toInt()
          destRect.bottom = ((j * tileSize + tileSize - windowTop) / scaleFactor).toInt()
          zones[i, j]?.drawBase(canvas, tileRect, destRect, paint)
        }
      }
    }
  }

  abstract fun drawLayer(context: Context, p: SurfaceRenderer.ViewPort)

  abstract fun drawFinal(context: Context, p: SurfaceRenderer.ViewPort)

  abstract fun getViewPortOrigin(x: Int, y: Int, p: SurfaceRenderer.ViewPort): Point

  companion object {
    @JvmStatic var mapWidth: Int = 0
    @JvmStatic var mapHeight: Int = 0
    @JvmStatic var tileRect = Rect()
  }

}
