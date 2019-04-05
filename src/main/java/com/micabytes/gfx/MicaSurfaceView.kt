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

import android.content.Context
import android.graphics.Canvas
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a SurfaceListener to receive touch
 * events and a SurfaceRenderer to handle the drawing.
 */
class MicaSurfaceView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback, GestureDetector.OnGestureListener {
  var listener: SurfaceListener? = null
  var renderer: SurfaceRenderer? = null
  private var touch: TouchHandler = TouchHandler(context)
  private var gesture: GestureDetector = GestureDetector(context, this)
  private var scaleGesture: ScaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
  private var lastScaleTime: Long = 0
  private var executor: ScheduledExecutorService? = null

  var viewPosition: Point
    get() {
      val ret = Point()
      renderer?.getViewPosition(ret)
      return ret
    }
    set(p) {
      renderer?.setViewPosition(p.x, p.y)
    }

  val zoom: Float
    get() = renderer?.zoom ?: 1.0F

  init {
    if (!isInEditMode) {
      holder.addCallback(this)
      isFocusable = true
    }
  }

  fun setZoom(z: Float, center: PointF) = renderer?.zoom(z, center)

  override fun surfaceCreated(holder: SurfaceHolder) {
    Timber.d(TAG, "surfaceCreate")
    if (renderer != null) renderer!!.start()
    touch.start()
    start()
    Timber.d(TAG, "surfaceCreated")
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    touch.stop()
    if (renderer != null) renderer!!.stop()
    stop()
    listener = null
    renderer = null
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    renderer?.setViewSize(width, height)
    val p = Point()
    renderer?.getViewPosition(p)
    setZoom(zoom, PointF(p.x.toFloat(), p.y.toFloat()))
    renderer?.setViewPosition(p.x, p.y)
  }

  fun start() {
    executor = Executors.newSingleThreadScheduledExecutor()
    executor?.scheduleAtFixedRate({
      draw()
    }, 100, 100, TimeUnit.MILLISECONDS)
  }

  fun draw() {
    var canvas: Canvas? = null
    try {
      canvas = holder.lockCanvas()
      synchronized(holder) {
        if (canvas != null)
          renderer?.draw(canvas)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      if (canvas != null) {
        try {
          holder.unlockCanvasAndPost(canvas)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  fun stop() {
    executor?.shutdown()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (renderer == null) return false
    val consumed = gesture.onTouchEvent(event)
    if (consumed) return true
    scaleGesture.onTouchEvent(event)
    // Calculate actual event position in background view
    val point = Point()
    renderer!!.getViewPosition(point)
    val zoom = renderer!!.zoom
    val x = (point.x + event.x * zoom).toInt()
    val y = (point.y + event.y * zoom).toInt()
    // Resolve events
    when (event.action and MotionEvent.ACTION_MASK) {
      MotionEvent.ACTION_DOWN -> {
        listener!!.onTouchDown(x, y)
        return touch.down(event)
      }
      MotionEvent.ACTION_MOVE -> {
        if (scaleGesture.isInProgress || System.currentTimeMillis() - lastScaleTime < SCALE_MOVE_GUARD.toLong())
          return super.onTouchEvent(event)
        return touch.move(event)
      }
      MotionEvent.ACTION_UP -> {
        listener!!.onTouchUp(x, y)
        return touch.onTouchUp(event)
      }
      MotionEvent.ACTION_CANCEL -> return touch.cancel(event)
      else -> {
      }
    }
    return super.onTouchEvent(event)
  }

  override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
    return touch.fling(e1, e2, velocityX, velocityY)
  }

  override fun onDown(e: MotionEvent): Boolean {
    return false
  }

  override fun onLongPress(e: MotionEvent) {
    // NOOP
  }

  override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
    return false
  }

  override fun onShowPress(e: MotionEvent) {
    // NOOP
  }

  override fun onSingleTapUp(e: MotionEvent): Boolean {
    return false
  }

  /**
   * Scale Listener Used to change the scale factor on the GameSurfaceRenderer
   */
  private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
    private val screenFocus = PointF()

    override fun onScale(detector: ScaleGestureDetector): Boolean {
      var scaleFactor = detector.scaleFactor
      if (scaleFactor != 0.0f) {
        scaleFactor = 1 / scaleFactor
        screenFocus.set(detector.focusX, detector.focusY)
        renderer!!.zoom(
            scaleFactor,
            screenFocus)
        invalidate()
      }
      lastScaleTime = System.currentTimeMillis()
      return true
    }
  }

  private enum class TouchState {
    NO_TOUCH, IN_TOUCH, ON_FLING, IN_FLING
  }

  private inner class TouchHandler internal constructor(context: Context) {
    // Current Touch State
    @get:Synchronized
    @set:Synchronized
    private var state = TouchState.NO_TOUCH
    private val touchDown = Point(0, 0)
    private val viewCenterAtDown = Point(0, 0)
    private val viewCenterAtFling = Point()
    private val viewSizeAtFling = Point()
    private var backgroundSizeAtFling = Point()
    internal val scroller: Scroller = Scroller(context)
    private var touchThread: TouchHandlerThread? = null

    internal fun start() {
      touchThread = TouchHandlerThread(this)
      touchThread!!.name = TOUCH_THREAD
      touchThread!!.start()
    }

    internal fun stop() {
      touchThread!!.running = false
      touchThread!!.interrupt()
      var retry = true
      while (retry) {
        try {
          touchThread!!.join()
          retry = false
        } catch (ignored: InterruptedException) {
          // Wait until done
        }

      }
      touchThread = null
    }

    internal fun down(event: MotionEvent): Boolean {
      // Cancel rendering suspension
      renderer!!.resume()
      // Get position
      synchronized(this) {
        state = TouchState.IN_TOUCH
        touchDown.x = event.x.toInt()
        touchDown.y = event.y.toInt()
        val p = Point()
        renderer!!.getViewPosition(p)
        viewCenterAtDown.set(p.x, p.y)
      }
      return true
    }

    internal fun move(event: MotionEvent): Boolean {
      if (state == TouchState.IN_TOUCH) {
        val zoom = renderer!!.zoom
        val deltaX = (event.x - touchDown.x) * zoom
        val deltaY = (event.y - touchDown.y) * zoom
        val newX = viewCenterAtDown.x - deltaX
        val newY = viewCenterAtDown.y - deltaY
        renderer!!.setViewPosition(newX.toInt(), newY.toInt())
        invalidate()
        return true
      }
      return false
    }

    internal fun onTouchUp(event: MotionEvent): Boolean {
      if (state == TouchState.IN_TOUCH) {
        state = TouchState.NO_TOUCH
      }
      return true
    }

    internal fun cancel(event: MotionEvent): Boolean {
      if (state == TouchState.IN_TOUCH) {
        state = TouchState.NO_TOUCH
      }
      return true
    }

    internal fun fling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
      renderer!!.getViewPosition(viewCenterAtFling)
      renderer!!.getViewSize(viewSizeAtFling)
      synchronized(this) {
        backgroundSizeAtFling = renderer!!.backgroundSize
        state = TouchState.ON_FLING
        renderer!!.suspend()
        scroller.fling(
            viewCenterAtFling.x,
            viewCenterAtFling.y,
            (-velocityX).toInt(),
            (-velocityY).toInt(),
            0,
            backgroundSizeAtFling.x - viewSizeAtFling.x,
            0,
            backgroundSizeAtFling.y - viewSizeAtFling.y)
        if (touchThread != null)
          touchThread!!.interrupt()
      }
      return true
    }

    /**
     * Touch Handler Thread
     */
    internal inner class TouchHandlerThread(private val touchHandler: TouchHandler) : Thread() {
      var running: Boolean = false

      init {
        name = TOUCH_THREAD
      }

      override fun run() {
        running = true
        while (running) {
          while (touchHandler.state != TouchState.ON_FLING && touchHandler.state != TouchState.IN_FLING) {
            try {
              Thread.sleep(Integer.MAX_VALUE.toLong())
            } catch (ignored: InterruptedException) {
              // NOOP
            }

            if (!running) return
          }
          synchronized(touchHandler) {
            if (touchHandler.state == TouchState.ON_FLING) {
              touchHandler.state = TouchState.IN_FLING
            }
          }
          if (touchHandler.state == TouchState.IN_FLING) {

            try {
              scroller.computeScrollOffset()
              renderer!!.setViewPosition(scroller.currX, scroller.currY)
              if (scroller.isFinished) {
                renderer!!.resume()
                synchronized(touchHandler) {
                  touchHandler.state = TouchState.NO_TOUCH

                  try {

                    Thread.sleep(5)
                  } catch (ignored: InterruptedException) {
                    // NOOP
                  }

                }
              }
            } catch (e: ArrayIndexOutOfBoundsException) {
              Timber.e(e)
              try {

                Thread.sleep(500)
              } catch (ignored: InterruptedException) {
                // NOOP
              }

            }
            // Fix for phone error.
          }
        }
      }

    }

  }

  companion object {
    private val TAG = MicaSurfaceView::class.java.name
    private const val SCALE_MOVE_GUARD = 500
    const val TOUCH_THREAD = "touchThread"
  }

}
