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
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Scroller;

import com.micabytes.util.GameLog;

import org.jetbrains.annotations.NonNls;

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a
 * SurfaceListener to receive touch events and a SurfaceRenderer to handle the drawing.
 */
@SuppressWarnings("MethodReturnAlwaysConstant")
public class MicaSurfaceView extends SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener {
  private static final String TAG = MicaSurfaceView.class.getName();
  @NonNls private static final String DRAW_THREAD = "drawThread";
  private static final int SCALE_MOVE_GUARD = 500;
  @NonNls public static final String GOOGLE = "google";
  @NonNls public static final String ASUS = "asus";
  @NonNls public static final String NEXUS_7 = "Nexus 7";
  /**
   * The Game Controller. This where we send UI events other than scroll and pinch-zoom in order to be handled
   */
  private SurfaceListener listener;
  /**
   * The Game Renderer. This handles all of the drawing duties to the Surface view
   */
  @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
  private SurfaceRenderer renderer;
  // The Touch Handlers
  private TouchHandler touch;
  private GestureDetector gesture;
  private ScaleGestureDetector scaleGesture;
  private long lastScaleTime;
  // Rendering Thread
  private GameSurfaceViewThread thread;
  //private Runnable threadEvent = null;

  public MicaSurfaceView(Context context) {
    super(context);
    // This ensures that we don't get errors when using it in layout editing
    if (isInEditMode()) return;
    touch = new TouchHandler(context);
    initialize(context);
  }

  public MicaSurfaceView(Context context, AttributeSet attrs) {
    super(context, attrs);
    // This ensures that we don't get errors when using it in layout editing
    if (isInEditMode()) return;
    touch = new TouchHandler(context);
    initialize(context);
  }

  public MicaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    // This ensures that we don't get errors when using it in layout editing
    if (isInEditMode()) return;
    touch = new TouchHandler(context);
    initialize(context);
  }

  private void initialize(Context context) {
    // Set SurfaceHolder callback
    getHolder().addCallback(this);
    // Initialize touch handlers
    gesture = new GestureDetector(context, this);
    scaleGesture = new ScaleGestureDetector(context, new ScaleListener());
    // Allow focus
    setFocusable(true);
  }

  /**
   * Sets the surface view listener
   */
  public void setListener(SurfaceListener surfaceListener) {
    listener = surfaceListener;
  }

  /**
   * Sets the renderer and creates the rendering thread
   */
  public void setRenderer(SurfaceRenderer r) {
    renderer = r;
  }

  // Return the position of the current view (center)
  public Point getViewPosition() {
    Point ret = new Point();
    renderer.getViewPosition(ret);
    return ret;
  }

  @SuppressWarnings("unused")
  public void setViewPort(int w, int h) {
    renderer.setViewSize(w, h);
  }

  public void setViewPosition(Point p) {
    renderer.setViewPosition(p.x, p.y);
  }

  @SuppressWarnings("unused")
  public void setMapPosition(Point p) {
    renderer.setMapPosition(p.x, p.y);
  }

  @SuppressWarnings("unused")
  public void centerViewPosition() {
    Point viewportSize = new Point();
    Point sceneSize = renderer.getBackgroundSize();
    renderer.getViewSize(viewportSize);

    int x = (sceneSize.x - viewportSize.x) / 2;
    int y = (sceneSize.y - viewportSize.y) / 2;
    renderer.setViewPosition(x, y);
  }

  @SuppressWarnings("unused")
  public Point getViewSize() {
    Point ret = new Point();
    renderer.getViewPosition(ret);
    return ret;
  }

  public float getZoom() {
    return renderer.getZoom();
  }

  public void setZoom(float z, PointF center) {
    renderer.zoom(z, center);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    GameLog.d(TAG, "surfaceCreate");
    thread = new GameSurfaceViewThread(holder);
    thread.setName(DRAW_THREAD);
    thread.setRunning(true);
    thread.start();
    if (renderer != null) renderer.start();
    if (touch != null) touch.start();
    // Required to ensure thread has focus
    if (thread != null)
      thread.onWindowFocusChanged(true);
    GameLog.d(TAG, "surfaceCreated");
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    GameLog.d(TAG, "surfaceDestroying");
    touch.stop();
    renderer.stop();
    thread.setRunning(false);
    //thread.surfaceDestroyed();
    boolean retry = true;
    while (retry) {
      try {
        thread.join();
        retry = false;
      } catch (InterruptedException ignored) {
        // Repeat until success
      }
    }
    GameLog.d(TAG, "surfaceDestroyed");
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    renderer.setViewSize(width, height);
    // Recheck scale factor and reset position to prevent out of bounds
    Point p = new Point();
    renderer.getViewPosition(p);
    setZoom(getZoom(), new PointF(p.x, p.y));
    //Point p = new Point();
    //this.renderer.getViewPosition(p);
    renderer.setViewPosition(p.x, p.y);
    // Debug
    GameLog.d(TAG, "surfaceChanged; new dimensions: w=" + width + ", h= " + height);
    // Required to ensure thread has focus
    if (thread != null)
      thread.onWindowFocusChanged(true);
  }

  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    super.onWindowFocusChanged(hasWindowFocus);
    if (thread != null)
      thread.onWindowFocusChanged(hasWindowFocus);
    GameLog.d(TAG, "onWindowFocusChanged");
  }

  // ----------------------------------------------------------------------

  /**
   * The Rendering thread for the MicaSurfaceView
   */
  @SuppressWarnings("ClassExplicitlyExtendsThread")
  class GameSurfaceViewThread extends Thread {
    private static final int BUG_DELAY = 500;
    private final int delay;
    private final SurfaceHolder surfaceHolder;
    private boolean running;
    private boolean hasFocus;

    GameSurfaceViewThread(SurfaceHolder surface) {
      setName(GameSurfaceViewThread.class.getName());
      surfaceHolder = surface;
      if (Build.BRAND.equalsIgnoreCase(GOOGLE) &&
          Build.MANUFACTURER.equalsIgnoreCase(ASUS) &&
          Build.MODEL.equalsIgnoreCase(NEXUS_7)) {
        GameLog.w(TAG, "Sleep 500ms (Device: Asus Nexus 7)");
        delay = BUG_DELAY;
      } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN_MR2) {
        GameLog.w(TAG, "Sleep 500ms (Handle issue 58385 in Android 4.3)");
        //
        delay = BUG_DELAY;
      } else {
        delay = 5;
      }
    }

    public void setRunning(boolean run) {
      running = run;
    }

    @SuppressWarnings({"RefusedBequest", "WhileLoopSpinsOnField", "BusyWait"})
    @Override
    public void run() {
      if (renderer == null) return;
      try {
        Thread.sleep(delay);
      } catch (InterruptedException ignored) {
        // NOOP
      }
      Canvas canvas = null;
      // This is the rendering loop; it goes until asked to quit.
      while (running) {
        try {
          Thread.sleep(5); // CPU timeout - help keep things cool
        } catch (InterruptedException ignored) {
          // NOOP
        }
        try {
          canvas = surfaceHolder.lockCanvas();
          if (canvas != null) {
            synchronized (surfaceHolder) {
              renderer.draw(canvas);
            }
          }
        } finally {
          if (canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
          }
        }
      }
    }

    public synchronized void onWindowFocusChanged(boolean focus) {
      hasFocus = focus;
      if (hasFocus) {
        notifyAll();
      }
    }

  }

  // ----------------------------------------------------------------------

  /**
   * Handle Touch Events
   */
  @SuppressWarnings({"NumericCastThatLosesPrecision"})
  @Override
  public boolean onTouchEvent(@NonNull MotionEvent event) {
    boolean consumed = gesture.onTouchEvent(event);
    if (consumed) return true;
    scaleGesture.onTouchEvent(event);
    // Calculate actual event position in background view
    Point point = new Point();
    renderer.getViewPosition(point);
    float zoom = renderer.getZoom();
    int x = (int) (point.x + (event.getX() * zoom));
    int y = (int) (point.y + (event.getY() * zoom));
    // Resolve events
    switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
        listener.onTouchDown(x, y);
        return touch.down(event);
      case MotionEvent.ACTION_MOVE:
        long scaleMoveGuard = SCALE_MOVE_GUARD;
        if (scaleGesture.isInProgress() || ((System.currentTimeMillis() - lastScaleTime) < scaleMoveGuard))
          //noinspection BreakStatement
          break;
        return touch.move(event);
      case MotionEvent.ACTION_UP:
        listener.onTouchUp(x, y);
        return touch.onTouchUp(event);
      case MotionEvent.ACTION_CANCEL:
        return touch.cancel(event);
      default:
        break;
    }
    return super.onTouchEvent(event);
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    return touch.fling(e1, e2, velocityX, velocityY);
  }

  @Override
  public boolean onDown(MotionEvent e) {
    // NOOP
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {
    // NOOP
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    // NOOP
    return false;
  }

  @Override
  public void onShowPress(MotionEvent e) {
    // NOOP
  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    // NOOP
    return false;
  }

  /**
   * Scale Listener Used to change the scale factor on the GameSurfaceRenderer
   */
  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private final PointF screenFocus = new PointF();

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      float scaleFactor = detector.getScaleFactor();
      if (scaleFactor != 0.0f) {
        scaleFactor = 1 / scaleFactor;
        screenFocus.set(detector.getFocusX(), detector.getFocusY());
        renderer.zoom(
            scaleFactor,
            screenFocus);
        invalidate();
      }
      lastScaleTime = System.currentTimeMillis();
      return true;
    }
  }

  protected enum TouchState {
    NO_TOUCH, IN_TOUCH, ON_FLING, IN_FLING
  }

  class TouchHandler {
    @NonNls
    private static final String TOUCH_THREAD = "touchThread";
    // Current Touch State
    private TouchState state = TouchState.NO_TOUCH;
    // Point initially touched
    private final Point touchDown = new Point(0, 0);
    // View Center onTouchDown
    private final Point viewCenterAtDown = new Point(0, 0);
    // View Center onFling
    private final Point viewCenterAtFling = new Point();
    // View Center onFling
    private final Point viewSizeAtFling = new Point();
    // View Center onFling
    private Point backgroundSizeAtFling = new Point();
    // Scroller
    final Scroller scroller;
    // Thread for handling
    private TouchHandlerThread touchThread;

    TouchHandler(Context context) {
      scroller = new Scroller(context);
    }

    void start() {
      touchThread = new TouchHandlerThread(this);
      touchThread.setName(TOUCH_THREAD);
      touchThread.start();
    }

    @SuppressWarnings("AssignmentToNull")
    void stop() {
      touchThread.running = false;
      touchThread.interrupt();
      boolean retry = true;
      while (retry) {
        try {
          touchThread.join();
          retry = false;
        } catch (InterruptedException ignored) {
          // Wait until done
        }
      }
      touchThread = null;
    }

    /**
     * Handle a down event
     */
    @SuppressWarnings({"SameReturnValue", "BooleanMethodNameMustStartWithQuestion", "NumericCastThatLosesPrecision"})
    boolean down(MotionEvent event) {
      // Cancel rendering suspension
      renderer.resume();
      // Get position
      synchronized (this) {
        setState(TouchState.IN_TOUCH);
        touchDown.x = (int) event.getX();
        touchDown.y = (int) event.getY();
        Point p = new Point();
        renderer.getViewPosition(p);
        viewCenterAtDown.set(p.x, p.y);
      }
      return true;
    }

    /**
     * Handle a move event
     */
    @SuppressWarnings({"NumericCastThatLosesPrecision", "BooleanMethodNameMustStartWithQuestion"})
    boolean move(MotionEvent event) {
      if (getState() == TouchState.IN_TOUCH) {
        float zoom = renderer.getZoom();
        float deltaX = (event.getX() - touchDown.x) * zoom;
        float deltaY = (event.getY() - touchDown.y) * zoom;
        float newX = viewCenterAtDown.x - deltaX;
        float newY = viewCenterAtDown.y - deltaY;
        renderer.setViewPosition((int) newX, (int) newY);
        invalidate();
        return true;
      }
      return false;
    }

    /**
     * Handle an onTouchUp event
     */
    @SuppressWarnings({"BooleanMethodNameMustStartWithQuestion", "UnusedParameters", "SameReturnValue"})
    boolean onTouchUp(MotionEvent event) {
      if (getState() == TouchState.IN_TOUCH) {
        setState(TouchState.NO_TOUCH);
      }
      return true;
    }

    /**
     * Handle a cancel event
     */
    @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
    boolean cancel(MotionEvent event) {
      if (getState() == TouchState.IN_TOUCH) {
        setState(TouchState.NO_TOUCH);
      }
      return true;
    }

    @SuppressWarnings({"UnusedParameters", "BooleanMethodNameMustStartWithQuestion", "NumericCastThatLosesPrecision", "SameReturnValue"})
    boolean fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      renderer.getViewPosition(viewCenterAtFling);
      renderer.getViewSize(viewSizeAtFling);
      synchronized (this) {
        backgroundSizeAtFling = renderer.getBackgroundSize();
        setState(TouchState.ON_FLING);
        renderer.suspend();
        scroller.fling(
            viewCenterAtFling.x,
            viewCenterAtFling.y,
            (int) -velocityX,
            (int) -velocityY,
            0,
            backgroundSizeAtFling.x - viewSizeAtFling.x,
            0,
            backgroundSizeAtFling.y - viewSizeAtFling.y);
        if (touchThread != null)
          touchThread.interrupt();
      }
      return true;
    }

    /**
     * Touch Handler Thread
     */
    @SuppressWarnings({"InnerClassTooDeeplyNested", "ClassExplicitlyExtendsThread"})
    class TouchHandlerThread extends Thread {
      private final TouchHandler touchHandler;
      boolean running;

      TouchHandlerThread(TouchHandler t) {
        touchHandler = t;
        setName(TOUCH_THREAD);
      }

      @SuppressWarnings({"MethodWithMultipleLoops", "RefusedBequest", "WhileLoopSpinsOnField", "BusyWait", "OverlyComplexMethod", "OverlyNestedMethod"})
      @Override
      public void run() {
        running = true;
        while (running) {
          while ((touchHandler.getState() != TouchState.ON_FLING) && (touchHandler.getState() != TouchState.IN_FLING)) {
            try {
              Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ignored) {
              // NOOP
            }
            if (!running) return;
          }
          synchronized (touchHandler) {
            if (touchHandler.getState() == TouchState.ON_FLING) {
              touchHandler.setState(TouchState.IN_FLING);
            }
          }
          if (touchHandler.getState() == TouchState.IN_FLING) {
            //noinspection ProhibitedExceptionCaught
            try {
              scroller.computeScrollOffset();
              renderer.setViewPosition(scroller.getCurrX(), scroller.getCurrY());
              if (scroller.isFinished()) {
                renderer.resume();
                synchronized (touchHandler) {
                  touchHandler.setState(TouchState.NO_TOUCH);
                  //noinspection NestedTryStatement
                  try {
                    //noinspection SleepWhileHoldingLock
                    Thread.sleep(5);
                  } catch (InterruptedException ignored) {
                    // NOOP
                  }
                }
              }
            }
            // Fix for phone error.
            catch (ArrayIndexOutOfBoundsException e) {
              GameLog.logException(e);
              try {
                //noinspection MagicNumber
                Thread.sleep(500);
              } catch (InterruptedException ignored) {
                // NOOP
              }
            }
          }
        }
      }

    }

    private synchronized TouchState getState() {
      return state;
    }

    private synchronized void setState(TouchState st) {
      state = st;
    }

  }

}
