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
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * SurfaceRenderer is the superclass of the renderer. The game should subclass the renderer and
 * extend the drawing methods to add game drawing.
 * 
 * BitmapSurfaceRenderer can be extended for apps that require background images.
 * TileMapSurfaceRenderer can be extended for apps that need to display TileMaps.
 *
 * @author micabyte
 */
public abstract class SurfaceRenderer {
	// Max Scale Factor
	private static final float DEFAULT_MAXSCALEFACTOR = 1.0f;
	private final float maxScaleFactor_;
    // Context
    protected Context context_;
    // The ViewPort
    protected final ViewPort viewPort_ = new ViewPort();
    // The Dimensions of the Game Area
    protected Point backgroundSize_ = new Point();
    // The Current Scale Factor
    protected float scaleFactor_ = 1.0f;

    protected SurfaceRenderer(Context c) {
        this.context_ = c;
        this.maxScaleFactor_ = DEFAULT_MAXSCALEFACTOR;
    }

    protected SurfaceRenderer(Context c, float mscale) {
        this.context_ = c;
        this.maxScaleFactor_ = mscale;
    }

    /** Rendering thread started */
    public abstract void start();

    /** Rendering thread stopped */
    public abstract void stop();

    /** Rendering updates can be suspended */
    public abstract void suspend(boolean b);

    /** Draw to the canvas */
    public void draw(Canvas c) {
        this.viewPort_.draw(c);
    }

    /** Draw the base (background) layer of the SurfaceView image */
    protected abstract void drawBase(ViewPort p);

    /** Draw the game (dynamic) layer of the SurfaceView image */
    protected abstract void drawLayer(ViewPort p);

    /** Get the position (center) of the view */
    public void getViewPosition(Point p) {
        this.viewPort_.getViewPosition(p);
    }

    /** Set the position (center) of the view */
    public void setViewPosition(int x, int y) {
        this.viewPort_.setViewPosition(x, y);
    }

    /** Set the position of the view */
    public void moveViewPosition(int fx, int fy, int dx, int dy) {
        this.viewPort_.moveViewPosition(fx, fy, dx, dy);
    }

    /** Get the dimensions of the view */
    public void getViewSize(Point p) {
        this.viewPort_.getViewPortSize(p);
    }

    /** Set the dimensions of the view */
    public void setView(int w, int h) {
        this.viewPort_.setViewPortSize(w, h);
    }

    /** Returns a Point representing the size of the scene. Don't modify the returned Point! */
    public Point getBackgroundSize() {
        return this.backgroundSize_;
    }

    public float getScaleFactor() {
        return this.scaleFactor_;
    }

    public void setScaleFactor(float s) {
        float tempScaleFactor = s;
        // Check bounds
        Point viewSize = new Point();
        Point backgroundSize = getBackgroundSize();
        getViewSize(viewSize);
        float min =
                Math.max((float) viewSize.x / (float) backgroundSize.x, (float) viewSize.y
                        / (float) backgroundSize.y);
        tempScaleFactor = Math.max(min, Math.min(tempScaleFactor, this.maxScaleFactor_));
        this.scaleFactor_ = tempScaleFactor;
    }

    public void changeScaleFactor(float multiplier) {
        float tempScaleFactor = this.scaleFactor_ * multiplier;
        // Check bounds
        Point viewSize = new Point();
        Point backgroundSize = getBackgroundSize();
        getViewSize(viewSize);
        float min =
                Math.max((float) viewSize.x / (float) backgroundSize.x, (float) viewSize.y
                        / (float) backgroundSize.y);
        tempScaleFactor = Math.max(min, Math.min(tempScaleFactor, this.maxScaleFactor_));
        // Calculate new origin
        Point tempP = new Point();
        this.getViewPosition(tempP);
        // Set scale factor
        this.scaleFactor_ = tempScaleFactor;
        this.setViewPosition(tempP.x, tempP.y);
    }

    /** View Port. This handles the actual drawing, managing dimensions, etc. */
    public class ViewPort {
        /** The Bitmap of the current ViewPort */
        public Bitmap bitmap_ = null;
        /** A Rect that can be used for drawing. Same size as bitmap */
        private final Rect bitmapSize_ = new Rect();
        /**
         * The center of the ViewPort, calculated as coordinates of the background/virtual image.
         * This is the point used for calculations all over.
         * 
         * Using the center point (rather than, e.g., the left upper or left lower) allows us to
         * handle panning and zooming a lot easier.
         */
        private final Point viewPortCenter_ = new Point();

        public void getViewPosition(Point p) {
            synchronized (this) {
                p.x = this.viewPortCenter_.x;
                p.y = this.viewPortCenter_.y;
            }
        }

        void setViewPosition(int nx, int ny) {
            synchronized (this) {
                int x = nx;
                int y = ny;
                int windowLeft =
                        (int) (nx - (this.bitmapSize_.right / (SurfaceRenderer.this.scaleFactor_ * 2)));
                int windowTop =
                        (int) (ny - (this.bitmapSize_.bottom / (SurfaceRenderer.this.scaleFactor_ * 2)));
                int windowRight =
                        (int) (nx + (this.bitmapSize_.right / (SurfaceRenderer.this.scaleFactor_ * 2)));
                int windowBottom =
                        (int) (ny + (this.bitmapSize_.bottom / (SurfaceRenderer.this.scaleFactor_ * 2)));
                if (windowLeft < 0)
                    x = (int) (this.bitmapSize_.right / (SurfaceRenderer.this.scaleFactor_ * 2));
                if (windowTop < 0)
                    y = (int) (this.bitmapSize_.bottom / (SurfaceRenderer.this.scaleFactor_ * 2));
                if (windowRight > SurfaceRenderer.this.backgroundSize_.x)
                    x = SurfaceRenderer.this.backgroundSize_.x
                                    - (int) (this.bitmapSize_.right / (SurfaceRenderer.this.scaleFactor_ * 2));
                if (windowBottom > SurfaceRenderer.this.backgroundSize_.y)
                    y = SurfaceRenderer.this.backgroundSize_.y
                                    - (int) (this.bitmapSize_.bottom / (SurfaceRenderer.this.scaleFactor_ * 2));
                // set windows
                this.viewPortCenter_.set(x, y);
            }
        }

        void moveViewPosition(int ox, int oy, int dx, int dy) {
            synchronized (this) {
                // set windows
                setViewPosition(((int) (ox - (dx / SurfaceRenderer.this.scaleFactor_))),
                        ((int) (oy - (dy / SurfaceRenderer.this.scaleFactor_))));
            }
        }

        public Rect getViewPortSize() {
            return this.bitmapSize_;
        }

        public void getViewPortSize(Point p) {
            synchronized (this) {
                p.x = this.bitmapSize_.right;
                p.y = this.bitmapSize_.bottom;
            }
        }

        void setViewPortSize(int w, int h) {
            synchronized (this) {
                if (this.bitmap_ != null) {
                    this.bitmap_.recycle();
                    this.bitmap_ = null;
                }
                this.bitmap_ = Bitmap.createBitmap(w, h, Config.RGB_565);
                this.bitmapSize_.set(0, 0, w, h);
            }
        }

        public Rect getScaledViewPort() {
            Rect ret;
            synchronized (this) {
                int w2 =
                        (int) (this.bitmapSize_.width() / (SurfaceRenderer.this.scaleFactor_ * 2));
                int h2 =
                        (int) (this.bitmapSize_.height() / (SurfaceRenderer.this.scaleFactor_ * 2));
                ret =
                        new Rect(this.viewPortCenter_.x - w2, this.viewPortCenter_.y - h2,
                                this.viewPortCenter_.x + w2, this.viewPortCenter_.y + h2);
            }
            return ret;
        }

        public Rect getUnScaledViewPort() {
            Rect ret;
            synchronized (this) {
                int w2 =
                        this.bitmapSize_.width() / 2;
                int h2 =
                        this.bitmapSize_.height() / 2;
                ret =   new Rect(this.viewPortCenter_.x - w2, this.viewPortCenter_.y - h2,
                                 this.viewPortCenter_.x + w2, this.viewPortCenter_.y + h2);
            }
            return ret;
        }
        
        void draw(Canvas c) {
            drawBase(this);
            drawLayer(this);
            synchronized (this) {
                if (c != null && this.bitmap_ != null) {
                    c.drawBitmap(this.bitmap_, null, this.bitmapSize_, null);
                }
            }
        }

    }

}
