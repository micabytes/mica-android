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
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.widget.Scroller;

import com.micabyte.android.game.BuildConfig;

/**
 * MicaSurfaceView encapsulates all of the logic for handling 2D game maps. Pass it a
 * SurfaceListener to receive touch events and a SurfaceRenderer to handle the drawing.
 * 
 * @author micabyte
 */
public class MicaSurfaceView extends android.view.SurfaceView
        implements
            SurfaceHolder.Callback,
            OnGestureListener {
    public static final String TAG = MicaSurfaceView.class.getName();
    /**
     * The Game Controller. This where we send UI events other than scroll and pinch-zoom in order
     * to be handled
     */
    private SurfaceListener listener_ = null;
    /** The Game Renderer. This handles all of the drawing duties to the Surface view */
    protected SurfaceRenderer renderer_ = null;
    // The Touch Handlers
    private TouchHandler touch_;
    private GestureDetector gesture_;
    private ScaleGestureDetector scaleGesture_;
    // Rendering Thread
    private GameSurfaceViewThread thread_;

    public MicaSurfaceView(Context context) {
        super(context);
        initialize(context);
    }

    public MicaSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public MicaSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        // Set SurfaceHolder callback
        getHolder().addCallback(this);
        // Initialize touch handlers
        this.touch_ = new TouchHandler(context);
        this.gesture_ = new GestureDetector(context, this);
        this.scaleGesture_ = new ScaleGestureDetector(context, new ScaleListener());
        // This ensures that we don't get errors when using it in Eclipse layout editing
        if (isInEditMode()) return;
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

    /** Return the position of the current view (center) */
    public Point getViewPosition() {
        Point ret = new Point();
        this.renderer_.getViewPosition(ret);
        return ret;
    }

    public void setViewPosition(Point p) {
        this.renderer_.setViewPosition(p.x, p.y);
    }

    public void centerViewPosition() {
        Point backgroundSize = this.renderer_.getBackgroundSize();
        int x = backgroundSize.x / 2;
        int y = backgroundSize.y / 2;
        this.renderer_.setViewPosition(x, y);
    }

    public float getScaleFactor() {
        return this.renderer_.getScaleFactor();
    }

    public void setScaleFactor(float s) {
        this.renderer_.setScaleFactor(s);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.thread_ = new GameSurfaceViewThread(holder);
        this.thread_.start();
        this.renderer_.start();
        this.touch_.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        this.touch_.stop();
        this.renderer_.stop();
        this.thread_.surfaceDestroyed();
        boolean done = true;
        while (!done) {
            try {
                this.thread_.join();
                done = true;
            } catch (InterruptedException e) {
                // Repeat until success
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        this.renderer_.setView(w, h);
        // Recheck scale factor and reset position to prevent out of bounds
        this.renderer_.setScaleFactor(this.renderer_.getScaleFactor());
        Point p = new Point();
        this.renderer_.getViewPosition(p);
        this.renderer_.setViewPosition(p.x, p.y);
        // Debug
        if (BuildConfig.DEBUG) Log.d(TAG, "surfaceChanged; new dimensions: w=" + w + ", h= " + h);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        this.thread_.onWindowFocusChanged(hasFocus);
    }

    // ----------------------------------------------------------------------

    /** The Rendering thread for the MicaSurfaceView */
    class GameSurfaceViewThread extends Thread {
        private final SurfaceHolder surfaceHolder_;
        private boolean isDone_ = false;
        private boolean hasFocus_ = false;

        public GameSurfaceViewThread(SurfaceHolder surfaceHolder) {
            setName("GameSurfaceViewThread");
            this.surfaceHolder_ = surfaceHolder;
        }

        @Override
        public void run() {
            Canvas canvas;
            // This is the rendering loop; it goes until asked to quit.
            while (!this.isDone_) {
                // CPU timeout - help keep things cool
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    // NOOP
                }
                // Rendering paused
                synchronized (this) {
                    if (!this.hasFocus_) {
                        while (!this.hasFocus_) {
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                // NOOP
                            }
                        }
                    }
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
                } finally {
                    if (canvas != null) {
                        this.surfaceHolder_.unlockCanvasAndPost(canvas);
                    }
                }
            }

        }

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
                this.isDone_ = true;
            }
        }

    }

    // ----------------------------------------------------------------------

    /** Handle Touch Events */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean consumed = this.gesture_.onTouchEvent(event);
        if (consumed) return true;
        this.scaleGesture_.onTouchEvent(event);
        // Calculate actual event position in background view
        Point c = new Point();
        this.renderer_.getViewPosition(c);
        Point b = new Point();
        this.renderer_.getViewSize(b);
        float s = this.renderer_.getScaleFactor();
        int x = (int) (c.x - (((b.x / 2) - event.getX()) / s));
        int y = (int) (c.y - (((b.y / 2) - event.getY()) / s));
        // Resolve events
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                this.listener_.onTouchDown(x, y);
                return this.touch_.down(event);
            case MotionEvent.ACTION_MOVE:
                if (!this.scaleGesture_.isInProgress()) {
                    return this.touch_.move(event);
                }
                break;
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
        public ScaleListener() {
            super();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            MicaSurfaceView.this.renderer_.changeScaleFactor(detector.getScaleFactor());
            MicaSurfaceView.this.invalidate();
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
        // Scroller
        final Scroller scroller_;
        // Thread for handling
        TouchHandlerThread touchThread_;

        TouchHandler(Context context) {
            this.scroller_ = new Scroller(context);
        }

        void start() {
            this.touchThread_ = new TouchHandlerThread(this);
            this.touchThread_.start();
        }

        void stop() {
            this.touchThread_.done();
            this.touchThread_.interrupt();
            boolean retry = true;
            while (retry) {
                try {
                    this.touchThread_.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // Wait until done
                }
            }
            this.touchThread_ = null;
        }

        /** Handle a down event */
        boolean down(MotionEvent event) {
            // Cancel rendering suspension
            MicaSurfaceView.this.renderer_.suspend(false);
            // Get position
            synchronized (this) {
                this.state_ = TouchState.IN_TOUCH;
                Point p = new Point();
                MicaSurfaceView.this.renderer_.getViewPosition(p);
                this.touchDown_.x = (int) event.getX();
                this.touchDown_.y = (int) event.getY();
                this.viewCenterAtDown_.x = p.x;
                this.viewCenterAtDown_.y = p.y;
            }
            return true;
        }

        /** Handle a move event */
        boolean move(MotionEvent event) {
            if (this.state_ == TouchState.IN_TOUCH) {
                int deltaX = (int) (event.getX() - this.touchDown_.x);
                int deltaY = (int) (event.getY() - this.touchDown_.y);
                MicaSurfaceView.this.renderer_.moveViewPosition(this.viewCenterAtDown_.x,
                        this.viewCenterAtDown_.y, deltaX, deltaY);
                MicaSurfaceView.this.invalidate();
                // if (BuildConfig.DEBUG) Log.d(TAG, "TouchHandler.move to " +
                // (this.viewOriginAtDown_.x - deltaX) + ", " + (this.viewOriginAtDown_.y -
                // deltaY));
                return true;
            }
            return false;
        }

        /** Handle an up event */
        boolean up(MotionEvent event) {
            if (this.state_ == TouchState.IN_TOUCH) {
                this.state_ = TouchState.NO_TOUCH;
            }
            return true;
        }

        /** Handle a cancel event */
        boolean cancel(MotionEvent event) {
            if (this.state_ == TouchState.IN_TOUCH) {
                this.state_ = TouchState.NO_TOUCH;
            }
            return true;
        }

        boolean fling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            MicaSurfaceView.this.renderer_.getViewPosition(this.viewCenterAtFling_);
            Point backSize = MicaSurfaceView.this.renderer_.getBackgroundSize();
            synchronized (this) {
                this.state_ = TouchState.ON_FLING;
                MicaSurfaceView.this.renderer_.suspend(true);
                this.scroller_.fling(this.viewCenterAtFling_.x, this.viewCenterAtFling_.y,
                        (int) -velocityX, (int) -velocityY, 0, backSize.x, 0, backSize.y);
                this.touchThread_.interrupt();
            }
            return true;
        }

        /**
         * Touch Handler Thread
         */
        class TouchHandlerThread extends Thread {
            private final TouchHandler touchHandler_;
            private boolean isDone_ = false;

            TouchHandlerThread(TouchHandler touch) {
                this.touchHandler_ = touch;
                setName("touchThread");
            }

            @Override
            public void run() {
                while (!this.isDone_) {
                    while ((this.touchHandler_.state_ != TouchState.ON_FLING)
                            && (this.touchHandler_.state_ != TouchState.IN_FLING)) {
                        try {
                            Thread.sleep(Integer.MAX_VALUE);
                        } catch (InterruptedException e) {
                            // NOOP
                        }
                        if (this.isDone_) return;
                    }
                    synchronized (this.touchHandler_) {
                        if (this.touchHandler_.state_ == TouchState.ON_FLING) {
                            this.touchHandler_.state_ = TouchState.IN_FLING;
                        }
                    }
                    if (this.touchHandler_.state_ == TouchState.IN_FLING) {
                        TouchHandler.this.scroller_.computeScrollOffset();
                        MicaSurfaceView.this.renderer_.setViewPosition(
                                TouchHandler.this.scroller_.getCurrX(),
                                TouchHandler.this.scroller_.getCurrY());
                        if (TouchHandler.this.scroller_.isFinished()) {
                            MicaSurfaceView.this.renderer_.suspend(false);
                            synchronized (this.touchHandler_) {
                                this.touchHandler_.state_ = TouchState.NO_TOUCH;
                                try {
                                    Thread.sleep(5);
                                } catch (InterruptedException e) {
                                    // NOOP
                                }
                            }
                        }
                    }
                }
            }

            public void done() {
                this.isDone_ = true;
            }

        }

    }

}
