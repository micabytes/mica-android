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
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.widget.Scroller;

import com.micabyte.android.BuildConfig;

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a
 * SurfaceListener to receive touch events and a SurfaceRenderer to handle the drawing.
 * 
 * @author micabyte
 */
public class MicaSurfaceView extends android.view.SurfaceView implements SurfaceHolder.Callback, OnGestureListener {
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
		this.touch_ = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
		this.touch_ = new TouchHandler(context);
		initialize(context);
	}

	public MicaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
		this.touch_ = new TouchHandler(context);
		initialize(context);
	}

	private void initialize(Context context) {
		// Set SurfaceHolder callback
		getHolder().addCallback(this);
		// Initialize touch handlers
		this.gesture_ = new GestureDetector(context, this);
		this.scaleGesture_ = new ScaleGestureDetector(context, new ScaleListener());
		// Allow focus
		setFocusable(true);
	}

	/** Sets the surface view listener */
	public void setListener(SurfaceListener l) {
		this.listener_ = l;
	}

	/** Sets the renderer and creates the rendering thread */
	public void setRenderer(SurfaceRenderer r) {
		this.renderer_ = r;
	}

	// Return the position of the current view (center)
	public Point getViewPosition() {
		Point ret = new Point();
		this.renderer_.getViewPosition(ret);
		return ret;
	}

    public void setViewPort(int w, int h) {
        this.renderer_.setViewSize(w, h);
    }

	public void setViewPosition(Point p) {
		this.renderer_.setViewPosition(p.x, p.y);
	}

    public void setMapPosition(Point p) { this.renderer_.setMapPosition(p.x, p.y); }

	public void centerViewPosition() {
        Point viewportSize = new Point();
        Point sceneSize = this.renderer_.getBackgroundSize();
        this.renderer_.getViewSize(viewportSize);

        int x = (sceneSize.x - viewportSize.x) / 2;
        int y = (sceneSize.y - viewportSize.y) / 2;
        this.renderer_.setViewPosition(x, y);
	}

	public Point getViewSize() {
		Point ret = new Point();
		this.renderer_.getViewPosition(ret);
		return ret;
	}

	public float getZoom() {
		return this.renderer_.getZoom();
	}

	public void setZoom(float z, PointF center) {
		this.renderer_.zoom(z, center);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.thread_ = new GameSurfaceViewThread(holder);
		this.thread_.setName("drawThread");
		this.thread_.setRunning(true);
		this.thread_.start();
		this.renderer_.start();
		this.touch_.start();
		// Required to ensure thread has focus
		//if (this.thread_ != null)
		//	this.thread_.onWindowFocusChanged(true);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		this.touch_.stop();
		this.renderer_.stop();
		this.thread_.setRunning(false);
		//this.thread_.surfaceDestroyed();
		boolean retry = true;
		while (retry) {
			try {
				this.thread_.join();
				retry = false;
			}
			catch (InterruptedException e) {
				// Repeat until success
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		this.renderer_.setViewSize(w, h);
		// Recheck scale factor and reset position to prevent out of bounds
		//this.setZoom().setScaleFactor(this.renderer_.getScaleFactor());
		//Point p = new Point();
		//this.renderer_.getViewPosition(p);
		//this.renderer_.setViewPosition(p.x, p.y);
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
			this.surfaceHolder_ = surfaceHolder;
		}

		public void setRunning(boolean b) {
			this.running_ = b;
		}

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
			while (this.running_) {
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
					canvas = this.surfaceHolder_.lockCanvas();
					if (canvas != null) {
						synchronized (this.surfaceHolder_) {
							MicaSurfaceView.this.renderer_.draw(canvas);
						}
					}
				}
				finally {
					if (canvas != null) {
						this.surfaceHolder_.unlockCanvasAndPost(canvas);
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
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean consumed = this.gesture_.onTouchEvent(event);
		if (consumed) return true;
		this.scaleGesture_.onTouchEvent(event);
		// Calculate actual event_ position in background view
		Point c = new Point();
		this.renderer_.getViewPosition(c);
		float s = this.renderer_.getZoom();
		int x = (int) (c.x + (event.getX() * s));
		int y = (int) (c.y + (event.getY() * s));
		// Resolve events
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				this.listener_.onTouchDown(x, y);
				return this.touch_.down(event);
			case MotionEvent.ACTION_MOVE:
                long SCALE_MOVE_GUARD = 500;
                if (this.scaleGesture_.isInProgress() || System.currentTimeMillis()-this.lastScaleTime_< SCALE_MOVE_GUARD)
					break;
				return this.touch_.move(event);
			case MotionEvent.ACTION_UP:
				this.listener_.onTouchUp(x, y);
				return this.touch_.up(event);
			case MotionEvent.ACTION_CANCEL:
				return this.touch_.cancel(event);
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return this.touch_.fling(e1, e2, velocityX, velocityY);
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
			super();
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            if (scaleFactor!=0f && scaleFactor!=1.0f){
                scaleFactor = 1/scaleFactor;
                this.screenFocus.set(detector.getFocusX(),detector.getFocusY());
                MicaSurfaceView.this.renderer_.zoom(
                        scaleFactor,
                        this.screenFocus);
                invalidate();
            }
            MicaSurfaceView.this.lastScaleTime_ = System.currentTimeMillis();
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
			this.scroller_ = new Scroller(context);
		}

		void start() {
			this.touchThread_ = new TouchHandlerThread(this);
			this.touchThread_.setName("touchThread");
			this.touchThread_.start();
		}

		void stop() {
			this.touchThread_.isRunning_ = false;
			this.touchThread_.interrupt();
			boolean retry = true;
			while (retry) {
				try {
					this.touchThread_.join();
					retry = false;
				}
				catch (InterruptedException e) {
					// Wait until done
				}
			}
			this.touchThread_ = null;
		}

		/** Handle a down event_ */
		boolean down(MotionEvent event) {
			// Cancel rendering suspension
			MicaSurfaceView.this.renderer_.suspend(false);
			// Get position
			synchronized (this) {
				this.state_ = TouchState.IN_TOUCH;
				this.touchDown_.x = (int) event.getX();
				this.touchDown_.y = (int) event.getY();
				Point p = new Point();
				MicaSurfaceView.this.renderer_.getViewPosition(p);
				this.viewCenterAtDown_.set(p.x, p.y);
			}
			return true;
		}

		/** Handle a move event_ */
		boolean move(MotionEvent event) {
			if (this.state_ == TouchState.IN_TOUCH) {
				float zoom = MicaSurfaceView.this.renderer_.getZoom();
				float deltaX = (event.getX() - this.touchDown_.x) * zoom;
				float deltaY = (event.getY() - this.touchDown_.y) * zoom;
				float newX = this.viewCenterAtDown_.x - deltaX;
				float newY = this.viewCenterAtDown_.y - deltaY;
				MicaSurfaceView.this.renderer_.setViewPosition((int) newX, (int) newY);
				MicaSurfaceView.this.invalidate();
				return true;
			}
			return false;
		}

		/** Handle an up event_ */
		boolean up(MotionEvent event) {
			if (this.state_ == TouchState.IN_TOUCH) {
				this.state_ = TouchState.NO_TOUCH;
			}
			return true;
		}

		/** Handle a cancel event_ */
		boolean cancel(MotionEvent event) {
			if (this.state_ == TouchState.IN_TOUCH) {
				this.state_ = TouchState.NO_TOUCH;
			}
			return true;
		}

		boolean fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			MicaSurfaceView.this.renderer_.getViewPosition(this.viewCenterAtFling_);
			MicaSurfaceView.this.renderer_.getViewSize(this.viewSizeAtFling_);
			this.backgroundSizeAtFling_ = MicaSurfaceView.this.renderer_.getBackgroundSize();
			synchronized (this) {
				this.state_ = TouchState.ON_FLING;
				MicaSurfaceView.this.renderer_.suspend(true);
				this.scroller_.fling(this.viewCenterAtFling_.x, this.viewCenterAtFling_.y, (int) -velocityX, (int) -velocityY, 0, this.backgroundSizeAtFling_.x - this.viewSizeAtFling_.x, 0, this.backgroundSizeAtFling_.y - this.viewSizeAtFling_.y);
				this.touchThread_.interrupt();
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
				this.touchHandler_ = touch;
				setName("touchThread");
			}

			@Override
			public void run() {
				this.isRunning_ = true;
				while (this.isRunning_) {
					while ((this.touchHandler_.state_ != TouchState.ON_FLING) && (this.touchHandler_.state_ != TouchState.IN_FLING)) {
						try {
							Thread.sleep(Integer.MAX_VALUE);
						}
						catch (InterruptedException e) {
							// NOOP
						}
						if (!this.isRunning_) return;
					}
					synchronized (this.touchHandler_) {
						if (this.touchHandler_.state_ == TouchState.ON_FLING) {
							this.touchHandler_.state_ = TouchState.IN_FLING;
						}
					}
					if (this.touchHandler_.state_ == TouchState.IN_FLING) {
						TouchHandler.this.scroller_.computeScrollOffset();
						MicaSurfaceView.this.renderer_.setViewPosition(TouchHandler.this.scroller_.getCurrX(), TouchHandler.this.scroller_.getCurrY());
						if (TouchHandler.this.scroller_.isFinished()) {
							MicaSurfaceView.this.renderer_.suspend(false);
							synchronized (this.touchHandler_) {
								this.touchHandler_.state_ = TouchState.NO_TOUCH;
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
				this.isRunning_ = b;
			}

		}

	}

}
