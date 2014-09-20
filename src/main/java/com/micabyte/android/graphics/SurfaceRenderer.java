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
 * SurfaceRenderer is the superclass of the renderer. The game should subclass the renderer and extend the drawing methods to add game drawing.
 * <p/>
 * - BitmapSurfaceRenderer can be extended for apps that require background images
 * - TileMapSurfaceRenderer can be extended for apps that need to display TileMaps (not currently up to date)
 * - HexMapSurfaceRenderer can be extended for apps that need to display HexMaps
 *
 * @author micabyte
 */
public abstract class SurfaceRenderer {
    // View Size Minimum
    private final static int MINIMUM_PIXELS_IN_VIEW = 50;
    // Context
    protected final Context context_;
    // The ViewPort
    protected final ViewPort viewPort_ = new ViewPort();
    // The Dimensions of the Game Area
    final Point backgroundSize_ = new Point();

    /**
     * Constructor for the surface renderer
     *
     * @param c We need to pass in the context, so that we have it when we create bitmaps for drawing operations later. Since the draw operations are
     *          run in a thread, we can't pass the context through the thread (at least not easily)
     */
    SurfaceRenderer(Context c) {
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
     * Set the position (center) of the view based on map coordinates. This is intended to be used with Tile/HexMaps, and needs to be implemented in
     * the derived player Map class.
     */
    public void setMapPosition(int x, int y) {
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
    public void setViewSize(int w, int h) {
        this.viewPort_.setSize(w, h);
    }

    /**
     * Returns a Point representing the size of the scene. Don't modify the returned Point!
     */
    public Point getBackgroundSize() {
        return this.backgroundSize_;
    }


    public void zoom(float scaleFactor, PointF screenFocus) {
        this.viewPort_.zoom(scaleFactor, screenFocus);
    }

    public float getZoom() {
        return this.viewPort_.getZoom();
    }

    /**
     * View Port. This handles the actual drawing, managing dimensions, etc.
     */
    public class ViewPort {
        // The Bitmap of the current ViewPort
        public Bitmap bitmap_ = null;
        // The rect defining where the viewport is within the scene
        public final Rect window = new Rect(0, 0, 0, 0);
        // The zoom factor of the viewport
        float zoom = 1.0f;

        public void getOrigin(Point p) {
            synchronized (this) {
                p.set(this.window.left, this.window.top);
            }
        }

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
                // Set the Window rect
                this.window.set(x, y, x + w, y + h);
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
                int x = this.window.left;
                int y = this.window.top;
                // check bounds
                if (x < 0)
                    x = 0;
                if (y < 0)
                    y = 0;
                if (x + w > SurfaceRenderer.this.backgroundSize_.x)
                    x = SurfaceRenderer.this.backgroundSize_.x - w;
                if (y + h > SurfaceRenderer.this.backgroundSize_.y)
                    y = SurfaceRenderer.this.backgroundSize_.y - h;
                // Set the Window rect
                this.window.set(x, y, x + w, y + h);
                /*this.window.set(
                        this.window.left,
                        this.window.top,
                        this.window.left + w,
                        this.window.top + h);*/
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
    }

}
