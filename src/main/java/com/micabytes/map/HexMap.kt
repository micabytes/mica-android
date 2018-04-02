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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect

import com.micabytes.gfx.SurfaceRenderer
import com.micabytes.util.Array2D
import com.micabytes.util.GameLog

/**
 * HexMap superclass
 *
 * This implementation works for pointy-side up HexMaps. Needs to be adjusted
 * if it is going to be used for flat-side up maps.
 */
abstract class HexMap protected constructor() {
  private lateinit var zones: Array2D<TileMapZone>
  private var scaleFactor: Float = 0.toFloat()
  private val viewPortOrigin = Point()
  private val viewPortSize = Point()
  private val destRect = Rect()
  private var windowLeft: Int = 0
  private var windowTop: Int = 0
  private var windowRight: Int = 0
  private var windowBottom: Int = 0
  private val tilePaint = Paint()
  protected val tileText = Paint()
  // Draw
  protected val canvas = Canvas()

  init {
    tilePaint.isAntiAlias = true
    tilePaint.isFilterBitmap = true
    tilePaint.isDither = true
    val select = Paint()
    select.style = Paint.Style.STROKE
    select.color = Color.RED
    select.strokeWidth = 2f

  }

  fun setHexMap(map: Array<Array<TileMapZone>>) {
    /*zones = Array(map.size) { arrayOfNulls(map[0].size) }
    for (i in map.indices) {
      System.arraycopy(map[i], 0, zones[i], 0, map[i].size)
    }
    mapHeight = map[0].size
    mapWidth = map.size
    tileRect = Rect(0, 0, map[0][0].width, map[0][0].height)
    tileSlope = tileRect.height() / 4
    */
  }

  fun drawBase(con: Context, p: SurfaceRenderer.ViewPort) {
    if (p.bitmap == null) {
      GameLog.e(TAG, "Viewport bitmap is null")
      return
    }
    canvas.setBitmap(p.bitmap)
    scaleFactor = p.zoom
    val yOffset = tileRect.height() - tileSlope
    p.getOrigin(viewPortOrigin)
    p.getSize(viewPortSize)
    windowLeft = viewPortOrigin.x
    windowTop = viewPortOrigin.y
    windowRight = viewPortOrigin.x + viewPortSize.x
    windowBottom = viewPortOrigin.y + viewPortSize.y
    var xOffset: Int
    if (isStandardOrientation) {
      // Clip tiles not in view
      var iMn = windowLeft / tileRect.width() - 1
      if (iMn < 0) iMn = 0
      var jMn = windowTop / (tileRect.height() - tileSlope) - 1
      if (jMn < 0) jMn = 0
      var iMx = windowRight / tileRect.width() + 2
      if (iMx >= mapWidth) iMx = mapWidth
      var jMx = windowBottom / (tileRect.height() - tileSlope) + 2
      if (jMx >= mapHeight) jMx = mapHeight
      // Draw Tiles
      for (i in iMn until iMx) {
        for (j in jMn until jMx) {
          if (zones[i, j] != null) {
            xOffset = if (j % 2 == 0) tileRect.width() / 2 else 0
            destRect.left = ((i * tileRect.width() - windowLeft - xOffset) / scaleFactor).toInt()
            destRect.top = ((j * (tileRect.height() - tileSlope) - windowTop - yOffset) / scaleFactor).toInt()
            destRect.right = ((i * tileRect.width() + tileRect.width() - windowLeft - xOffset) / scaleFactor).toInt()
            destRect.bottom = ((j * (tileRect.height() - tileSlope) + tileRect.height() - windowTop - yOffset) / scaleFactor).toInt()
            zones[i, j].drawBase(canvas, tileRect, destRect, tilePaint)
          }
        }
      }
    } else {
      // Clip tiles not in view
      var iMn = mapWidth - windowRight / tileRect.width() - 2
      if (iMn < 0) iMn = 0
      var jMn = mapHeight - (windowBottom / (tileRect.height() - tileSlope) + 2)
      if (jMn < 0) jMn = 0
      var iMx = mapWidth - (windowLeft / tileRect.width() + 1)
      if (iMx >= mapWidth) iMx = mapWidth - 1
      var jMx = mapHeight - (windowTop / (tileRect.height() - tileSlope) + 1)
      if (jMx >= mapHeight) jMx = mapHeight - 1
      // Draw Tiles
      for (i in iMx downTo iMn) {
        for (j in jMx downTo jMn) {
          if (zones[i, j] != null) {
            xOffset = if (j % 2 == 1) tileRect.width() / 2 else 0
            destRect.left = (((mapWidth - i - 1) * tileRect.width() - windowLeft - xOffset) / scaleFactor).toInt()
            destRect.top = (((mapHeight - j - 1) * (tileRect.height() - tileSlope) - windowTop - yOffset) / scaleFactor).toInt()
            destRect.right = (((mapWidth - i - 1) * tileRect.width() + tileRect.width() - windowLeft - xOffset) / scaleFactor).toInt()
            destRect.bottom = (((mapHeight - j - 1) * (tileRect.height() - tileSlope) + tileRect.height() - windowTop - yOffset) / scaleFactor).toInt()
            zones[i, j].drawBase(canvas, tileRect, destRect, tilePaint)
          }
        }
      }
    }
  }

  abstract fun drawLayer(context: Context, p: SurfaceRenderer.ViewPort)

  abstract fun drawFinal(context: Context, p: SurfaceRenderer.ViewPort)

  abstract fun getViewPortOrigin(x: Int, y: Int, p: SurfaceRenderer.ViewPort): Point

  companion object {
    private val TAG = HexMap::class.java.name
    var isStandardOrientation = true
    var mapWidth = 0
    var mapHeight = 0
    var tileSlope = 0
    var tileRect = Rect()
  }

}
