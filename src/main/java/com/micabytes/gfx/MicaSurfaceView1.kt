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
import android.os.Build
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Scroller

import com.micabytes.util.GameLog

import org.jetbrains.annotations.NonNls

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a
 * SurfaceListener to receive touch events and a SurfaceRenderer to handle the drawing.
 */
class MicaSurfaceView : SurfaceView, SurfaceHolder.Callback, GestureDetector.OnGestureListener {
  /**
   * The Game Controller. This where we send UI events other than scroll and pinch-zoom in order to be handled
   */
  private var listener: SurfaceListener? = null
  /**
   * The Game Renderer. This handles all of the drawing duties to the Surface view
   */
  private var renderer: SurfaceRenderer? = null
  // The Touch Handlers
  private var touch: TouchHandler? = null
  private var gesture: GestureDetector? = null
  private var scaleGesture: ScaleGestureDetector? = null
  private var lastScaleTime: Long = 0
  // Rendering Thread
  private var thread: GameSurfaceViewThread? = null

  // Return the position of the current view (center)
  var viewPosition: Point
    get() {
      val ret = Point()
      if (renderer != null) renderer!!.getViewPosition(ret)
      return ret
    }
    set(p) {
      if (renderer != null) renderer!!.setViewPosition(p.x, p.y)
    }

  val viewSize: Point
    get() {
      val ret = Point()
      if (renderer != null) renderer!!.getViewPosition(ret)
      return ret
    }

  val zoom: Float
    get() = renderer!!.zoom
  //private Runnable threadEvent = null;

  constructor(context: Context) : super(context) {
    if (isInEditMode) return
    touch = TouchHandler(context)
    initialize(context)
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
    if (isInEditMode) return
    touch = TouchHandler(context)
    initialize(context)
  }

  constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
    if (isInEditMode) return
    touch = TouchHandler(context)
    initialize(context)
  }

  private fun initialize(context: Context) {
    // Set SurfaceHolder callback
    holder.addCallback(this)
    // Initialize touch handlers
    gesture = GestureDetector(context, this)
    scaleGesture = ScaleGestureDetector(context, ScaleListener())
    // Allow focus
    isFocusable = true
  }

  /**
   * Sets the surface view listener
   */
  fun setListener(surfaceListener: SurfaceListener) {
    listener = surfaceListener
  }

  /**
   * Sets the renderer and creates the rendering thread
   */
  fun setRenderer(r: SurfaceRenderer) {
    renderer = r
  }

  fun setViewPort(w: Int, h: Int) {
    renderer!!.setViewSize(w, h)
  }

  fun setMapPosition(p: Point) {
    renderer!!.setMapPosition(p.x, p.y)
  }

  fun centerViewPosition() {
    if (renderer == null) return
    val viewportSize = Point()
    val sceneSize = renderer!!.backgroundSize
    renderer!!.getViewSize(viewportSize)

    val x = (sceneSize.x - viewportSize.x) / 2
    val y = (sceneSize.y - viewportSize.y) / 2
    renderer!!.setViewPosition(x, y)
  }

  fun setZoom(z: Float, center: PointF) {
    if (renderer != null) renderer!!.zoom(z, center)
  }

  override fun surfaceCreated(holder: SurfaceHolder) {
    GameLog.d(TAG, "surfaceCreate")
    thread = GameSurfaceViewThread(holder)
    thread!!.name = DRAW_THREAD
    thread!!.setRunning(true)
    thread!!.start()
    if (renderer != null) renderer!!.start()
    if (touch != null) touch!!.start()
    // Required to ensure thread has focus
    if (thread != null)
      thread!!.onWindowFocusChanged(true)
    GameLog.d(TAG, "surfaceCreated")
  }

  override fun surfaceDestroyed(holder: SurfaceHolder) {
    GameLog.d(TAG, "surfaceDestroying")
    if (touch != null) touch!!.stop()
    if (renderer != null) renderer!!.stop()
    if (thread != null) thread!!.setRunning(false)
    //thread.surfaceDestroyed();
    var retry = true
    while (retry && thread != null) {
      try {
        thread!!.join()
        retry = false
      } catch (ignored: InterruptedException) {
        // Repeat until success
      }

    }
    GameLog.d(TAG, "surfaceDestroyed")
  }

  override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    if (renderer != null) {
      renderer!!.setViewSize(width, height)
      // Recheck scale factor and reset position to prevent out of bounds
      val p = Point()
      renderer!!.getViewPosition(p)
      setZoom(zoom, PointF(p.x.toFloat(), p.y.toFloat()))
      //Point p = new Point();
      //this.renderer.getViewPosition(p);
      renderer!!.setViewPosition(p.x, p.y)
    }
    // Debug
    GameLog.d(TAG, "surfaceChanged; new dimensions: w=$width, h= $height")
    // Required to ensure thread has focus
    if (thread != null)
      thread!!.onWindowFocusChanged(true)
  }

  override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
    super.onWindowFocusChanged(hasWindowFocus)
    if (thread != null)
      thread!!.onWindowFocusChanged(hasWindowFocus)
    GameLog.d(TAG, "onWindowFocusChanged")
  }

  /**
   * The Rendering thread for the MicaSurfaceView
   */
  private inner class GameSurfaceViewThread internal constructor(private val surfaceHolder: SurfaceHolder) : Thread() {
    private val lock = java.lang.Object()
    private val delay: Int
    private var running: Boolean = false
    private var hasFocus: Boolean = false

    init {
      name = GameSurfaceViewThread::class.java.name
      if (Build.BRAND.equals(GOOGLE, ignoreCase = true) &&
          Build.MANUFACTURER.equals(ASUS, ignoreCase = true) &&
          Build.MODEL.equals(NEXUS_7, ignoreCase = true)) {
        GameLog.w(TAG, "Sleep 500ms (Device: Asus Nexus 7)")
        delay = BUG_DELAY
      } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
        GameLog.w(TAG, "Sleep 500ms (Handle issue 58385 in Android 4.3)")
        //
        delay = BUG_DELAY
      } else {
        delay = 5
      }
    }

    fun setRunning(run: Boolean) {
      running = run
    }

    override fun run() {
      if (renderer == null) return
      try {
        Thread.sleep(delay.toLong())
      } catch (ignored: InterruptedException) {
        // NOOP
      }

      var canvas: Canvas? = null
      // This is the rendering loop; it goes until asked to quit.
      while (running) {
        try {
          Thread.sleep(5) // CPU timeout - help keep things cool
        } catch (ignored: InterruptedException) {
          // NOOP
        }

        try {
          canvas = surfaceHolder.lockCanvas()
          if (canvas != null) {
            synchronized(surfaceHolder) {
              renderer!!.draw(canvas)
            }
          }
        } finally {
          if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas)
          }
        }
      }
    }

    @Synchronized
    internal fun onWindowFocusChanged(focus: Boolean) {
      hasFocus = focus
      if (hasFocus) {
        lock.notifyAll()
      }
    }

  }

  // ----------------------------------------------------------------------

  /**
   * Handle Touch Events
   */
  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (renderer == null) return false
    val consumed = gesture!!.onTouchEvent(event)
    if (consumed) return true
    scaleGesture!!.onTouchEvent(event)
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
        return touch!!.down(event)
      }
      MotionEvent.ACTION_MOVE -> {
        if (scaleGesture!!.isInProgress || System.currentTimeMillis() - lastScaleTime < SCALE_MOVE_GUARD.toLong())
          return super.onTouchEvent(event)
        return touch!!.move(event)
      }
      MotionEvent.ACTION_UP -> {
        listener!!.onTouchUp(x, y)
        return touch!!.onTouchUp(event)
      }
      MotionEvent.ACTION_CANCEL -> return touch!!.cancel(event)
      else -> {
      }
    }
    return super.onTouchEvent(event)
  }

  override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
    return touch!!.fling(e1, e2, velocityX, velocityY)
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
    @get:Synchronized @set:Synchronized private var state = TouchState.NO_TOUCH
    // Point initially touched
    private val touchDown = Point(0, 0)
    // View Center onTouchDown
    private val viewCenterAtDown = Point(0, 0)
    // View Center onFling
    private val viewCenterAtFling = Point()
    // View Center onFling
    private val viewSizeAtFling = Point()
    // View Center onFling
    private var backgroundSizeAtFling = Point()
    // Scroller
    internal val scroller: Scroller
    // Thread for handling
    private var touchThread: TouchHandlerThread? = null

    init {
      scroller = Scroller(context)
    }

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

    /**
     * Handle a down event
     */
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

    /**
     * Handle a move event
     */
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

    /**
     * Handle an onTouchUp event
     */
    internal fun onTouchUp(event: MotionEvent): Boolean {
      if (state == TouchState.IN_TOUCH) {
        state = TouchState.NO_TOUCH
      }
      return true
    }

    /**
     * Handle a cancel event
     */
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
              GameLog.logException(e)
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
    @NonNls private val DRAW_THREAD = "drawThread"
    private val SCALE_MOVE_GUARD = 500
    @NonNls val GOOGLE = "google"
    @NonNls val ASUS = "asus"
    @NonNls val NEXUS_7 = "Nexus 7"
    val TOUCH_THREAD = "touchThread"
    val BUG_DELAY = 500

  }

}
