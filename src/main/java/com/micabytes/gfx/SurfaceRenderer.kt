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

const val MINIMUM_PIXELS_IN_VIEW = 50

/**
 * SurfaceRenderer is the superclass of the renderer. The game should subclass the renderer and extend the drawing methods to add game drawing.
 * - BitmapSurfaceRenderer can be extended for apps that require background images
 * - TileMapSurfaceRenderer can be extended for apps that need to display TileMaps (not currently onTouchUp to date)
 * - HexMapSurfaceRenderer can be extended for apps that need to display HexMaps
 */
abstract class SurfaceRenderer {
  // The ViewPort
  protected val viewPort = ViewPort()
  // The Dimensions of the Game Area
  val backgroundSize = Point()
  // Zoom
  val zoom: Float
    get() = viewPort.zoom

  // Rendering thread started
  abstract fun start()

  // Rendering thread stopped
  abstract fun stop()

  // Rendering updates can be suspended
  abstract fun suspend()

  // Rendering updates can be resumed
  abstract fun resume()

  // Draw to the canvas
  fun draw(canvas: Canvas) {
    viewPort.draw(canvas)
  }

  // Draw the base (background) layer of the SurfaceView image
  protected abstract fun drawBase()

  // Draw the game (dynamic) layer of the SurfaceView image
  protected abstract fun drawLayer()

  // Draw any final touches
  protected abstract fun drawFinal()

  /**
   * Get the position (center) of the view
   */
  fun getViewPosition(p: Point) {
    viewPort.getOrigin(p)
  }

  /**
   * Set the position (center) of the view
   */
  fun setViewPosition(x: Int, y: Int) {
    viewPort.setOrigin(x, y)
  }

  /**
   * Set the position (center) of the view based on map coordinates. This is intended to be used with Tile/HexMaps, and needs to be implemented in
   * the derived player Map class.
   */
  open fun setMapPosition(x: Int, y: Int) {
    viewPort.setOrigin(x, y)
  }

  /**
   * Get the dimensions of the view
   */
  fun getViewSize(p: Point) {
    viewPort.getSize(p)
  }

  /**
   * Set the dimensions of the view
   */
  fun setViewSize(w: Int, h: Int) {
    viewPort.setSize(w, h)
  }


  fun zoom(scaleFactor: Float, screenFocus: PointF) {
    viewPort.zoom(scaleFactor, screenFocus)
  }

  /**
   * View Port. This handles the actual drawing, managing dimensions, etc.
   */
  inner class ViewPort {
    // The Bitmap of the current ViewPort
    @get:Synchronized
    var bitmap: Bitmap? = null
    val bitmapLock = Object()
    // Bitmap needs checking.
    // The rect defining where the viewport is within the scene
    @get:Synchronized
    val window = Rect(0, 0, 0, 0)
    // The zoom factor of the viewport
    @get:Synchronized
    @set:Synchronized
    var zoom = 10.0f // 1.0f

    private val physicalWidth: Int
      get() = synchronized(bitmapLock) {
        return bitmap!!.width
      }

    private val physicalHeight: Int
      get() = synchronized(bitmapLock) {
        return bitmap!!.height
      }

    @Synchronized
    fun getOrigin(p: Point) {
      p.set(window.left, window.top)
    }

    fun setOrigin(xp: Int, yp: Int) {
      var x = xp
      var y = yp
      var w: Int
      var h: Int
      synchronized(this) {
        w = window.width()
        h = window.height()
      }
      // check bounds
      if (x < 0)
        x = 0
      if (y < 0)
        y = 0
      if (x + w > backgroundSize.x)
        x = backgroundSize.x - w
      if (y + h > backgroundSize.y)
        y = backgroundSize.y - h
      // Set the Window rect
      synchronized(this) {
        window.set(x, y, x + w, y + h)
      }
    }

    fun setSize(w: Int, h: Int) {
      var x: Int
      var y: Int
      synchronized(bitmapLock) {
        if (bitmap != null) {
          bitmap!!.recycle()
          bitmap = null
        }
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
        x = window.left
        y = window.top
      }
      // check bounds
      if (x < 0)
        x = 0
      if (y < 0)
        y = 0
      if (x + w > backgroundSize.x)
        x = backgroundSize.x - w
      if (y + h > backgroundSize.y)
        y = backgroundSize.y - h
      // Set the Window rect
      synchronized(this) {
        window.set(x, y, x + w, y + h)
      }
    }

    @Synchronized
    fun getSize(p: Point) {
      p.x = window.width()
      p.y = window.height()
    }

    fun getPhysicalSize(p: Point) {
      if (bitmap == null) return
      p.x = physicalWidth
      p.y = physicalHeight
    }

    fun zoom(factor: Float, screenFocus: PointF) {
      var screenSize: PointF
      synchronized(bitmapLock) {
        if (bitmap == null) return
        screenSize = PointF(bitmap!!.width.toFloat(), bitmap!!.height.toFloat())
      }
      val sceneSize = PointF(backgroundSize)
      val screenWidthToHeight = screenSize.x / screenSize.y
      val screenHeightToWidth = screenSize.y / screenSize.x
      synchronized(this) {
        var newZoom = zoom * factor
        val w1 = RectF(window)
        val w2 = RectF()
        val sceneFocus = PointF(
            w1.left + screenFocus.x / screenSize.x * w1.width(),
            w1.top + screenFocus.y / screenSize.y * w1.height()
        )
        var w2Width = physicalWidth * newZoom
        if (w2Width > sceneSize.x) {
          w2Width = sceneSize.x
          newZoom = w2Width / physicalWidth
        }
        if (w2Width < MINIMUM_PIXELS_IN_VIEW) {
          w2Width = MINIMUM_PIXELS_IN_VIEW.toFloat()
          newZoom = w2Width / physicalWidth
        }
        var w2Height = w2Width * screenHeightToWidth
        if (w2Height > sceneSize.y) {
          w2Height = sceneSize.y
          w2Width = w2Height * screenWidthToHeight
          newZoom = w2Width / physicalWidth
        }
        if (w2Height < MINIMUM_PIXELS_IN_VIEW) {
          w2Height = MINIMUM_PIXELS_IN_VIEW.toFloat()
          w2Width = w2Height * screenWidthToHeight
          newZoom = w2Width / physicalWidth
        }
        w2.left = sceneFocus.x - screenFocus.x / screenSize.x * w2Width
        w2.top = sceneFocus.y - screenFocus.y / screenSize.y * w2Height
        if (w2.left < 0)
          w2.left = 0f
        if (w2.top < 0)
          w2.top = 0f
        w2.right = w2.left + w2Width
        w2.bottom = w2.top + w2Height
        if (w2.right > sceneSize.x) {
          w2.right = sceneSize.x
          w2.left = w2.right - w2Width
        }
        if (w2.bottom > sceneSize.y) {
          w2.bottom = sceneSize.y
          w2.top = w2.bottom - w2Height
        }

        window.set(w2.left.toInt(), w2.top.toInt(), w2.right.toInt(), w2.bottom.toInt())
        zoom = newZoom
      }
    }

    internal fun draw(canvas: Canvas?) {
      drawBase()
      drawLayer()
      drawFinal()
      synchronized(bitmapLock) {
        if (canvas != null && bitmap != null) {
          canvas.drawBitmap(bitmap!!, 0.0f, 0.0f, null)
        }
      }
    }

  }

}
