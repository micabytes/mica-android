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
package com.micabyte.android.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.appcompat.BuildConfig;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.widget.Scroller;

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a
 * SurfaceListener to receive touch events and a SurfaceRenderer to handle the drawing.
 * 
 * @author micabyte
 */
public class MicaSurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback, GestureDetector.OnGestureListener {
	private static final String TAG = MicaSurfaceView.class.getName();
	/** The Game Controller. This where we send UI events other than scroll and pinch-zoom in order to be handled */
	private SurfaceListener listener = null;
	/** The Game Renderer. This handles all of the drawing duties to the Surface view */
    private SurfaceRenderer renderer = null;
	// The Touch Handlers
	private TouchHandler touch;
	private GestureDetector gesture;
	private ScaleGestureDetector scaleGesture;
	private long lastScaleTime = 0;
    // Rendering Thread
	private GameSurfaceViewThread thread = null;
	//private Runnable threadEvent = null;

	public MicaSurfaceView(Context context) {
		super(context);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
        touch = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
        touch = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        // This ensures that we don't get errors when using it in Eclipse layout editing
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

	/** Sets the surface view listener */
	public void setListener(SurfaceListener l) {
        listener = l;
	}

	/** Sets the renderer and creates the rendering thread */
	public void setRenderer(SurfaceRenderer r) {
        renderer = r;
	}

	// Return the position of the current view (center)
	public Point getViewPosition() {
		final Point ret = new Point();
        renderer.getViewPosition(ret);
		return ret;
	}

    public void setViewPort(int w, int h) {
        renderer.setViewSize(w, h);
    }

	public void setViewPosition(Point p) {
        renderer.setViewPosition(p.x, p.y);
	}

    public void setMapPosition(Point p) {
        renderer.setMapPosition(p.x, p.y); }

	public void centerViewPosition() {
        final Point viewportSize = new Point();
        final Point sceneSize = renderer.getBackgroundSize();
        renderer.getViewSize(viewportSize);

        final int x = (sceneSize.x - viewportSize.x) / 2;
        final int y = (sceneSize.y - viewportSize.y) / 2;
        renderer.setViewPosition(x, y);
	}

	public Point getViewSize() {
		final Point ret = new Point();
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
        thread = new GameSurfaceViewThread(holder);
        thread.setName("drawThread");
        thread.setRunning(true);
        thread.start();
        renderer.start();
        touch.start();
		// Required to ensure thread has focus
		//if (this.thread != null)
		//	this.thread.onWindowFocusChanged(true);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        touch.stop();
        renderer.stop();
        thread.setRunning(false);
		//this.thread.surfaceDestroyed();
		boolean retry = true;
		while (retry) {
			try {
                thread.join();
				retry = false;
			}
			catch (InterruptedException e) {
				// Repeat until success
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        renderer.setViewSize(w, h);
		// Recheck scale factor and reset position to prevent out of bounds
        final Point p = new Point();
        renderer.getViewPosition(p);
        setZoom(getZoom(), new PointF(p.x, p.y));
		//Point p = new Point();
		//this.renderer.getViewPosition(p);
        renderer.setViewPosition(p.x, p.y);
		// Debug
		Log.d(TAG, "surfaceChanged; new dimensions: w=" + w + ", h= " + h);
		// Required to ensure thread has focus
		//if (this.thread != null)
		//	this.thread.onWindowFocusChanged(true);

	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//this.thread.onWindowFocusChanged(hasFocus);
		if (BuildConfig.DEBUG) Log.d(TAG, "onWindowFocusChanged");
	}

	// Set a Runnable to be run on the rendering thread.
	/*public void setEvent(Runnable r) {
		this.threadEvent = r;
		if (this.thread != null)
			this.thread.setEvent(r);
	}*/

	// Clears the runnable event, if any, from the rendering thread.
	/*public void clearEvent() {
		this.thread.clearEvent();
	}*/

	// ----------------------------------------------------------------------

	/** The Rendering thread for the MicaSurfaceView */
	class GameSurfaceViewThread extends Thread {
		private final SurfaceHolder surfaceHolder;
		private boolean running = false;

		public GameSurfaceViewThread(SurfaceHolder surface) {
			setName("GameSurfaceViewThread");
            surfaceHolder = surface;
		}

		public void setRunning(boolean b) {
            running = b;
		}

		@SuppressWarnings("MagicNumber")
        @Override
		public void run() {
            Canvas canvas;
            // Handle issue 58385 in Android 4.3
            int delayMillis = 5;
            if (Build.VERSION.SDK_INT == 18) delayMillis = 475;
            try {
                Thread.sleep(delayMillis);
            }
            catch (InterruptedException e) {
                // NOOP
            }
			// This is the rendering loop; it goes until asked to quit.
			while (running) {
				// CPU timeout - help keep things cool
				try {
					Thread.sleep(5);
				}
				catch (InterruptedException e) {
					// NOOP
				}
				// Render Graphics
				canvas = null;
				try {
					canvas = surfaceHolder.lockCanvas();
					if (canvas != null) {
						synchronized (surfaceHolder) {
                            renderer.draw(canvas);
						}
					}
				}
				finally {
					if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}

		}

		/*
		public void onWindowFocusChanged(boolean hasFocus) {
			synchronized (this) {
				this.hasFocus = hasFocus;
				if (this.hasFocus == true) {
					notify();
				}
			}
		}

		public void surfaceDestroyed() {
			synchronized (this) {
				this.running = false;
			}
		}

		// Queue an "event" to be run on the rendering thread.
		public void setEvent(Runnable r) {
			synchronized (this) {
				this.event = r;
			}
		}

		public void clearEvent() {
			synchronized (this) {
				this.event = null;
			}
		}
		*/

	}

	// ----------------------------------------------------------------------

	/** Handle Touch Events */
	@SuppressWarnings("MagicNumber")
    @Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		final boolean consumed = gesture.onTouchEvent(event);
		if (consumed) return true;
        scaleGesture.onTouchEvent(event);
		// Calculate actual event position in background view
		final Point c = new Point();
        renderer.getViewPosition(c);
		final float s = renderer.getZoom();
		final int x = (int) (c.x + (event.getX() * s));
		final int y = (int) (c.y + (event.getY() * s));
		// Resolve events
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
                listener.onTouchDown(x, y);
				return touch.down(event);
			case MotionEvent.ACTION_MOVE:
                final long SCALE_MOVE_GUARD = 500;
                if (scaleGesture.isInProgress() || ((System.currentTimeMillis() - lastScaleTime) < SCALE_MOVE_GUARD))
					break;
				return touch.move(event);
			case MotionEvent.ACTION_UP:
                listener.onTouchUp(x, y);
				return touch.up(event);
			case MotionEvent.ACTION_CANCEL:
				return touch.cancel(event);
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

        public ScaleListener() {
        }

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if ((scaleFactor != 0f) && (scaleFactor != 1.0f)){
                scaleFactor = 1/scaleFactor;
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

	enum TouchState {
		NO_TOUCH, IN_TOUCH, ON_FLING, IN_FLING
	}

	class TouchHandler {
		// Current Touch State
		TouchState state = TouchState.NO_TOUCH;
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
		TouchHandlerThread touchThread;

		TouchHandler(Context context) {
            scroller = new Scroller(context);
		}

		void start() {
            touchThread = new TouchHandlerThread(this);
            touchThread.setName("touchThread");
            touchThread.start();
		}

		void stop() {
            touchThread.isRunning = false;
            touchThread.interrupt();
			boolean retry = true;
			while (retry) {
				try {
                    touchThread.join();
					retry = false;
				}
				catch (InterruptedException e) {
					// Wait until done
				}
			}
            touchThread = null;
		}

		/** Handle a down event */
        @SuppressWarnings("SameReturnValue")
        boolean down(MotionEvent event) {
			// Cancel rendering suspension
            renderer.suspend(false);
			// Get position
			synchronized (this) {
                state = TouchState.IN_TOUCH;
                touchDown.x = (int) event.getX();
                touchDown.y = (int) event.getY();
				final Point p = new Point();
                renderer.getViewPosition(p);
                viewCenterAtDown.set(p.x, p.y);
			}
			return true;
		}

		/** Handle a move event */
		boolean move(MotionEvent event) {
			if (state == TouchState.IN_TOUCH) {
				final float zoom = renderer.getZoom();
				final float deltaX = (event.getX() - touchDown.x) * zoom;
				final float deltaY = (event.getY() - touchDown.y) * zoom;
				final float newX = viewCenterAtDown.x - deltaX;
				final float newY = viewCenterAtDown.y - deltaY;
                renderer.setViewPosition((int) newX, (int) newY);
                invalidate();
				return true;
			}
			return false;
		}

		/** Handle an up event */
        @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean up(MotionEvent event) {
			if (state == TouchState.IN_TOUCH) {
                state = TouchState.NO_TOUCH;
			}
			return true;
		}

		/** Handle a cancel event */
        @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean cancel(MotionEvent event) {
			if (state == TouchState.IN_TOUCH) {
                state = TouchState.NO_TOUCH;
			}
			return true;
		}

		@SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            renderer.getViewPosition(viewCenterAtFling);
            renderer.getViewSize(viewSizeAtFling);
            backgroundSizeAtFling = renderer.getBackgroundSize();
			synchronized (this) {
                state = TouchState.ON_FLING;
                renderer.suspend(true);
                scroller.fling(viewCenterAtFling.x, viewCenterAtFling.y, (int) -velocityX, (int) -velocityY, 0, backgroundSizeAtFling.x - viewSizeAtFling.x, 0, backgroundSizeAtFling.y - viewSizeAtFling.y);
                touchThread.interrupt();
			}
			return true;
		}

		/**
		 * Touch Handler Thread
		 */
		class TouchHandlerThread extends Thread {
			private final TouchHandler touchHandler;
			boolean isRunning = false;

			TouchHandlerThread(TouchHandler touch) {
                touchHandler = touch;
				setName("touchThread");
			}

			@Override
			public void run() {
                isRunning = true;
				while (isRunning) {
					while ((touchHandler.state != TouchState.ON_FLING) && (touchHandler.state != TouchState.IN_FLING)) {
						try {
							Thread.sleep(Integer.MAX_VALUE);
						}
						catch (InterruptedException e) {
							// NOOP
						}
						if (!isRunning) return;
					}
					synchronized (touchHandler) {
						if (touchHandler.state == TouchState.ON_FLING) {
                            touchHandler.state = TouchState.IN_FLING;
						}
					}
					if (touchHandler.state == TouchState.IN_FLING) {
                        scroller.computeScrollOffset();
                        renderer.setViewPosition(scroller.getCurrX(), scroller.getCurrY());
						if (scroller.isFinished()) {
                            renderer.suspend(false);
							synchronized (touchHandler) {
                                touchHandler.state = TouchState.NO_TOUCH;
								try {
									Thread.sleep(5);
								}
								catch (InterruptedException e) {
									// NOOP
								}
							}
						}
					}
				}
			}

			public void setRunning(boolean b) {
                isRunning = b;
			}

		}

	}

}
