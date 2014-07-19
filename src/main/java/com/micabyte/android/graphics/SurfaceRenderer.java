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
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

/**
 * SurfaceRenderer is the superclass of the renderer. The game should subclass
 * the renderer and extend the drawing methods to add game drawing.
 * <p/>
 * - BitmapSurfaceRenderer can be extended for apps that require background
 * images.
 * - TileMapSurfaceRenderer can be extended for apps that need to display
 * TileMaps.
 *
 * @author micabyte
 */
public abstract class SurfaceRenderer {
    // View Size Minimum
    private final static int MINIMUM_PIXELS_IN_VIEW = 50;
    // Context
    protected Context context_;
    // The ViewPort
    protected final ViewPort viewPort_ = new ViewPort();
    // The Dimensions of the Game Area
    Point backgroundSize_ = new Point();
    // The Current Scale Factor
    //protected float scaleFactor_ = 1.0f;

    protected SurfaceRenderer(Context c) {
        this.context_ = c;
    }

    /**
     * Rendering thread started
     */
    public abstract void start();

    /**
     * Rendering thread stopped
     */
    public abstract void stop();

    /**
     * Rendering updates can be suspended
     */
    public abstract void suspend(boolean b);

    /**
     * Draw to the canvas
     */
    public void draw(Canvas c) {
        this.viewPort_.draw(c);
    }

    /**
     * Draw the base (background) layer of the SurfaceView image
     */
    protected abstract void drawBase();

    /**
     * Draw the game (dynamic) layer of the SurfaceView image
     */
    protected abstract void drawLayer();

    /**
     * Draw any final touches
     */
    protected abstract void drawFinal();

    /**
     * Get the position (center) of the view
     */
    public void getViewPosition(Point p) {
        this.viewPort_.getOrigin(p);
    }

    /**
     * Set the position (center) of the view
     */
    public void setViewPosition(int x, int y) {
        this.viewPort_.setOrigin(x, y);
    }

    /**
     * Get the dimensions of the view
     */
    public void getViewSize(Point p) {
        this.viewPort_.getSize(p);
    }

    /**
     * Set the dimensions of the view
     */
    public void setView(int w, int h) {
        this.viewPort_.setSize(w, h);
    }

    /**
     * Returns a Point representing the size of the scene. Don't modify the returned Point!
     */
    public Point getBackgroundSize() {
        return this.backgroundSize_;
    }

    /*
     Set the position of the view *
    public void moveViewPosition(int fx, int fy, int dx, int dy) {
        this.viewPort_.moveViewPosition(fx, fy, dx, dy);
    }

    public float getScaleFactor() {
        return this.viewPort_.getZoom();
    }

    public void setScaleFactor(float s) {
    	/*
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
        *
    }

    public void changeScaleFactor(float multiplier) {
    	/*
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
        *
    }
    */

    /**
     * View Port. This handles the actual drawing, managing dimensions, etc.
     */
    public class ViewPort {
        // The Bitmap of the current ViewPort
        public Bitmap bitmap_ = null;
        // The rect defining where the viewport is within the scene
        public final Rect window = new Rect(0, 0, 0, 0);
        float zoom = 1.0f;

        public void setOrigin(int xp, int yp) {
            synchronized (this) {
                int x = xp;
                int y = yp;
                int w = this.window.width();
                int h = this.window.height();
                // check bounds
                if (x < 0)
                    x = 0;
                if (y < 0)
                    y = 0;
                if (x + w > SurfaceRenderer.this.backgroundSize_.x)
                    x = SurfaceRenderer.this.backgroundSize_.x - w;
                if (y + h > SurfaceRenderer.this.backgroundSize_.y)
                    y = SurfaceRenderer.this.backgroundSize_.y - h;
                this.window.set(x, y, x + w, y + h);
            }
        }

        public void getOrigin(Point p) {
            synchronized (this) {
                p.set(this.window.left, this.window.top);
            }
        }

        public void setSize(int w, int h) {
            synchronized (this) {
                if (this.bitmap_ != null) {
                    this.bitmap_.recycle();
                    this.bitmap_ = null;
                }
                this.bitmap_ = Bitmap.createBitmap(w, h, Config.RGB_565);
                Log.d("SF", "Created bitmap of size " + w + " " + h);
                this.window.set(
                        this.window.left,
                        this.window.top,
                        this.window.left + w,
                        this.window.top + h);
            }
        }

        public void getSize(Point p) {
            synchronized (this) {
                p.x = this.window.width();
                p.y = this.window.height();
            }
        }

        public void getPhysicalSize(Point p) {
            synchronized (this) {
                p.x = getPhysicalWidth();
                p.y = getPhysicalHeight();
            }
        }

        public int getPhysicalWidth() {
            return this.bitmap_.getWidth();
        }

        public int getPhysicalHeight() {
            return this.bitmap_.getHeight();
        }

        public float getZoom() {
            return this.zoom;
        }

        public void zoom(float factor, PointF screenFocus) {
            if (this.bitmap_ == null) return;
            if (factor != 1.0) {
                PointF screenSize = new PointF(this.bitmap_.getWidth(), this.bitmap_.getHeight());
                PointF sceneSize = new PointF(getBackgroundSize());
                float screenWidthToHeight = screenSize.x / screenSize.y;
                float screenHeightToWidth = screenSize.y / screenSize.x;
                synchronized (this) {
                    float newZoom = this.zoom * factor;
                    RectF w1 = new RectF(this.window);
                    RectF w2 = new RectF();
                    PointF sceneFocus = new PointF(
                            w1.left + (screenFocus.x / screenSize.x) * w1.width(),
                            w1.top + (screenFocus.y / screenSize.y) * w1.height()
                    );
                    float w2Width = getPhysicalWidth() * newZoom;
                    if (w2Width > sceneSize.x) {
                        w2Width = sceneSize.x;
                        newZoom = w2Width / getPhysicalWidth();
                    }
                    if (w2Width < MINIMUM_PIXELS_IN_VIEW) {
                        w2Width = MINIMUM_PIXELS_IN_VIEW;
                        newZoom = w2Width / getPhysicalWidth();
                    }
                    float w2Height = w2Width * screenHeightToWidth;
                    if (w2Height > sceneSize.y) {
                        w2Height = sceneSize.y;
                        w2Width = w2Height * screenWidthToHeight;
                        newZoom = w2Width / getPhysicalWidth();
                    }
                    if (w2Height < MINIMUM_PIXELS_IN_VIEW) {
                        w2Height = MINIMUM_PIXELS_IN_VIEW;
                        w2Width = w2Height * screenWidthToHeight;
                        newZoom = w2Width / getPhysicalWidth();
                    }
                    w2.left = sceneFocus.x - ((screenFocus.x / screenSize.x) * w2Width);
                    w2.top = sceneFocus.y - ((screenFocus.y / screenSize.y) * w2Height);
                    if (w2.left < 0)
                        w2.left = 0;
                    if (w2.top < 0)
                        w2.top = 0;
                    w2.right = w2.left + w2Width;
                    w2.bottom = w2.top + w2Height;
                    if (w2.right > sceneSize.x) {
                        w2.right = sceneSize.x;
                        w2.left = w2.right - w2Width;
                    }
                    if (w2.bottom > sceneSize.y) {
                        w2.bottom = sceneSize.y;
                        w2.top = w2.bottom - w2Height;
                    }
                    this.window.set((int) w2.left, (int) w2.top, (int) w2.right, (int) w2.bottom);
                    this.zoom = newZoom;
//                    Log.d(TAG,String.format(
//                            "f=%.2f, z=%.2f, scrf(%.0f,%.0f), scnf(%.0f,%.0f) w1s(%.0f,%.0f) w2s(%.0f,%.0f) w1(%.0f,%.0f,%.0f,%.0f) w2(%.0f,%.0f,%.0f,%.0f)",
//                            factor,
//                            zoom,
//                            screenFocus.x,
//                            screenFocus.y,
//                            sceneFocus.x,
//                            sceneFocus.y,
//                            w1.width(),w1.height(),
//                            w2Width, w2Height,
//                            w1.left,w1.top,w1.right,w1.bottom,
//                            w2.left,w2.top,w2.right,w2.bottom
//                            ));
                }
            }
        }

        void draw(Canvas c) {
            drawBase();
            drawLayer();
            drawFinal();
            synchronized (this) {
                if (c != null && this.bitmap_ != null) {
                    c.drawBitmap(this.bitmap_, 0F, 0F, null);
                }
            }
        }

        /** A Rect that can be used for drawing. Same size as bitmap *
         private final Rect bitmapSize_ = new Rect();
         /**
         * The center of the ViewPort, calculated as coordinates of the background/virtual image.
         * This is the point used for calculations all over.
         *
         * Using the center point (rather than, e.g., the left upper or left lower) allows us to
         * handle panning and zooming a lot easier.
         *
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

         public Rect getWindow() {
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
         */

    }

    public void zoom(float scaleFactor, PointF screenFocus) {
        this.viewPort_.zoom(scaleFactor, screenFocus);
    }

    public float getZoom() {
        return this.viewPort_.getZoom();
    }

}
