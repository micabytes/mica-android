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
	private SurfaceListener listener_ = null;
	/** The Game Renderer. This handles all of the drawing duties to the Surface view */
    private SurfaceRenderer renderer_ = null;
	// The Touch Handlers
	private TouchHandler touch_;
	private GestureDetector gesture_;
	private ScaleGestureDetector scaleGesture_;
	private long lastScaleTime_ = 0;
    // Rendering Thread
	private GameSurfaceViewThread thread_ = null;
	//private Runnable threadEvent_ = null;

	public MicaSurfaceView(Context context) {
		super(context);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
        touch_ = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
        touch_ = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
        touch_ = new TouchHandler(context);
		initialize(context);
	}

	private void initialize(Context context) {
		// Set SurfaceHolder callback
		getHolder().addCallback(this);
		// Initialize touch handlers
        gesture_ = new GestureDetector(context, this);
        scaleGesture_ = new ScaleGestureDetector(context, new ScaleListener());
		// Allow focus
		setFocusable(true);
	}

	/** Sets the surface view listener */
	public void setListener(SurfaceListener l) {
        listener_ = l;
	}

	/** Sets the renderer and creates the rendering thread */
	public void setRenderer(SurfaceRenderer r) {
        renderer_ = r;
	}

	// Return the position of the current view (center)
	public Point getViewPosition() {
		final Point ret = new Point();
        renderer_.getViewPosition(ret);
		return ret;
	}

    public void setViewPort(int w, int h) {
        renderer_.setViewSize(w, h);
    }

	public void setViewPosition(Point p) {
        renderer_.setViewPosition(p.x, p.y);
	}

    public void setMapPosition(Point p) {
        renderer_.setMapPosition(p.x, p.y); }

	public void centerViewPosition() {
        final Point viewportSize = new Point();
        final Point sceneSize = renderer_.getBackgroundSize();
        renderer_.getViewSize(viewportSize);

        final int x = (sceneSize.x - viewportSize.x) / 2;
        final int y = (sceneSize.y - viewportSize.y) / 2;
        renderer_.setViewPosition(x, y);
	}

	public Point getViewSize() {
		final Point ret = new Point();
        renderer_.getViewPosition(ret);
		return ret;
	}

	public float getZoom() {
		return renderer_.getZoom();
	}

	public void setZoom(float z, PointF center) {
        renderer_.zoom(z, center);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
        thread_ = new GameSurfaceViewThread(holder);
        thread_.setName("drawThread");
        thread_.setRunning(true);
        thread_.start();
        renderer_.start();
        touch_.start();
		// Required to ensure thread has focus
		//if (this.thread_ != null)
		//	this.thread_.onWindowFocusChanged(true);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
        touch_.stop();
        renderer_.stop();
        thread_.setRunning(false);
		//this.thread_.surfaceDestroyed();
		boolean retry = true;
		while (retry) {
			try {
                thread_.join();
				retry = false;
			}
			catch (InterruptedException e) {
				// Repeat until success
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        renderer_.setViewSize(w, h);
		// Recheck scale factor and reset position to prevent out of bounds
        final Point p = new Point();
        renderer_.getViewPosition(p);
        setZoom(getZoom(), new PointF(p.x, p.y));
		//Point p = new Point();
		//this.renderer_.getViewPosition(p);
        renderer_.setViewPosition(p.x, p.y);
		// Debug
		Log.d(TAG, "surfaceChanged; new dimensions: w=" + w + ", h= " + h);
		// Required to ensure thread has focus
		//if (this.thread_ != null)
		//	this.thread_.onWindowFocusChanged(true);

	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		//this.thread_.onWindowFocusChanged(hasFocus);
		if (BuildConfig.DEBUG) Log.d(TAG, "onWindowFocusChanged");
	}

	// Set a Runnable to be run on the rendering thread.
	/*public void setEvent(Runnable r) {
		this.threadEvent_ = r;
		if (this.thread_ != null)
			this.thread_.setEvent(r);
	}*/

	// Clears the runnable event, if any, from the rendering thread.
	/*public void clearEvent() {
		this.thread_.clearEvent();
	}*/

	// ----------------------------------------------------------------------

	/** The Rendering thread for the MicaSurfaceView */
	class GameSurfaceViewThread extends Thread {
		private final SurfaceHolder surfaceHolder_;
		private boolean running_ = false;

		public GameSurfaceViewThread(SurfaceHolder surfaceHolder) {
			setName("GameSurfaceViewThread");
            surfaceHolder_ = surfaceHolder;
		}

		public void setRunning(boolean b) {
            running_ = b;
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
			while (running_) {
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
					canvas = surfaceHolder_.lockCanvas();
					if (canvas != null) {
						synchronized (surfaceHolder_) {
                            renderer_.draw(canvas);
						}
					}
				}
				finally {
					if (canvas != null) {
                        surfaceHolder_.unlockCanvasAndPost(canvas);
					}
				}
			}

		}

		/*
		public void onWindowFocusChanged(boolean hasFocus) {
			synchronized (this) {
				this.hasFocus_ = hasFocus;
				if (this.hasFocus_ == true) {
					notify();
				}
			}
		}

		public void surfaceDestroyed() {
			synchronized (this) {
				this.running_ = false;
			}
		}

		// Queue an "event_" to be run on the rendering thread.
		public void setEvent(Runnable r) {
			synchronized (this) {
				this.event_ = r;
			}
		}

		public void clearEvent() {
			synchronized (this) {
				this.event_ = null;
			}
		}
		*/

	}

	// ----------------------------------------------------------------------

	/** Handle Touch Events */
	@SuppressWarnings("MagicNumber")
    @Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		final boolean consumed = gesture_.onTouchEvent(event);
		if (consumed) return true;
        scaleGesture_.onTouchEvent(event);
		// Calculate actual event_ position in background view
		final Point c = new Point();
        renderer_.getViewPosition(c);
		final float s = renderer_.getZoom();
		final int x = (int) (c.x + (event.getX() * s));
		final int y = (int) (c.y + (event.getY() * s));
		// Resolve events
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
                listener_.onTouchDown(x, y);
				return touch_.down(event);
			case MotionEvent.ACTION_MOVE:
                final long SCALE_MOVE_GUARD = 500;
                if (scaleGesture_.isInProgress() || System.currentTimeMillis()- lastScaleTime_ < SCALE_MOVE_GUARD)
					break;
				return touch_.move(event);
			case MotionEvent.ACTION_UP:
                listener_.onTouchUp(x, y);
				return touch_.up(event);
			case MotionEvent.ACTION_CANCEL:
				return touch_.cancel(event);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return touch_.fling(e1, e2, velocityX, velocityY);
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
            if (scaleFactor!=0f && scaleFactor!=1.0f){
                scaleFactor = 1/scaleFactor;
                screenFocus.set(detector.getFocusX(), detector.getFocusY());
                renderer_.zoom(
                        scaleFactor,
                        screenFocus);
                invalidate();
            }
            lastScaleTime_ = System.currentTimeMillis();
            return true;
 		}
	}

	enum TouchState {
		NO_TOUCH, IN_TOUCH, ON_FLING, IN_FLING
	}

	class TouchHandler {
		// Current Touch State
		TouchState state_ = TouchState.NO_TOUCH;
		// Point initially touched
		private final Point touchDown_ = new Point(0, 0);
		// View Center onTouchDown
		private final Point viewCenterAtDown_ = new Point(0, 0);
		// View Center onFling
		private final Point viewCenterAtFling_ = new Point();
		// View Center onFling
		private final Point viewSizeAtFling_ = new Point();
		// View Center onFling
		private Point backgroundSizeAtFling_ = new Point();
		// Scroller
		final Scroller scroller_;
		// Thread for handling
		TouchHandlerThread touchThread_;

		TouchHandler(Context context) {
            scroller_ = new Scroller(context);
		}

		void start() {
            touchThread_ = new TouchHandlerThread(this);
            touchThread_.setName("touchThread");
            touchThread_.start();
		}

		void stop() {
            touchThread_.isRunning_ = false;
            touchThread_.interrupt();
			boolean retry = true;
			while (retry) {
				try {
                    touchThread_.join();
					retry = false;
				}
				catch (InterruptedException e) {
					// Wait until done
				}
			}
            touchThread_ = null;
		}

		/** Handle a down event_ */
        @SuppressWarnings("SameReturnValue")
        boolean down(MotionEvent event) {
			// Cancel rendering suspension
            renderer_.suspend(false);
			// Get position
			synchronized (this) {
                state_ = TouchState.IN_TOUCH;
                touchDown_.x = (int) event.getX();
                touchDown_.y = (int) event.getY();
				final Point p = new Point();
                renderer_.getViewPosition(p);
                viewCenterAtDown_.set(p.x, p.y);
			}
			return true;
		}

		/** Handle a move event_ */
		boolean move(MotionEvent event) {
			if (state_ == TouchState.IN_TOUCH) {
				final float zoom = renderer_.getZoom();
				final float deltaX = (event.getX() - touchDown_.x) * zoom;
				final float deltaY = (event.getY() - touchDown_.y) * zoom;
				final float newX = viewCenterAtDown_.x - deltaX;
				final float newY = viewCenterAtDown_.y - deltaY;
                renderer_.setViewPosition((int) newX, (int) newY);
                invalidate();
				return true;
			}
			return false;
		}

		/** Handle an up event_ */
        @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean up(MotionEvent event) {
			if (state_ == TouchState.IN_TOUCH) {
                state_ = TouchState.NO_TOUCH;
			}
			return true;
		}

		/** Handle a cancel event_ */
        @SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean cancel(MotionEvent event) {
			if (state_ == TouchState.IN_TOUCH) {
                state_ = TouchState.NO_TOUCH;
			}
			return true;
		}

		@SuppressWarnings({"SameReturnValue", "UnusedParameters"})
        boolean fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            renderer_.getViewPosition(viewCenterAtFling_);
            renderer_.getViewSize(viewSizeAtFling_);
            backgroundSizeAtFling_ = renderer_.getBackgroundSize();
			synchronized (this) {
                state_ = TouchState.ON_FLING;
                renderer_.suspend(true);
                scroller_.fling(viewCenterAtFling_.x, viewCenterAtFling_.y, (int) -velocityX, (int) -velocityY, 0, backgroundSizeAtFling_.x - viewSizeAtFling_.x, 0, backgroundSizeAtFling_.y - viewSizeAtFling_.y);
                touchThread_.interrupt();
			}
			return true;
		}

		/**
		 * Touch Handler Thread
		 */
		class TouchHandlerThread extends Thread {
			private final TouchHandler touchHandler_;
			boolean isRunning_ = false;

			TouchHandlerThread(TouchHandler touch) {
                touchHandler_ = touch;
				setName("touchThread");
			}

			@Override
			public void run() {
                isRunning_ = true;
				while (isRunning_) {
					while ((touchHandler_.state_ != TouchState.ON_FLING) && (touchHandler_.state_ != TouchState.IN_FLING)) {
						try {
							Thread.sleep(Integer.MAX_VALUE);
						}
						catch (InterruptedException e) {
							// NOOP
						}
						if (!isRunning_) return;
					}
					synchronized (touchHandler_) {
						if (touchHandler_.state_ == TouchState.ON_FLING) {
                            touchHandler_.state_ = TouchState.IN_FLING;
						}
					}
					if (touchHandler_.state_ == TouchState.IN_FLING) {
                        scroller_.computeScrollOffset();
                        renderer_.setViewPosition(scroller_.getCurrX(), scroller_.getCurrY());
						if (scroller_.isFinished()) {
                            renderer_.suspend(false);
							synchronized (touchHandler_) {
                                touchHandler_.state_ = TouchState.NO_TOUCH;
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
                isRunning_ = b;
			}

		}

	}

}
