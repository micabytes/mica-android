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
    private static final int MINIMUM_PIXELS_IN_VIEW = 50;
    // Context
    protected final Context context;
    // The ViewPort
    protected final ViewPort viewPort = new ViewPort();
    // The Dimensions of the Game Area
    final Point backgroundSize = new Point();

    /**
     * Constructor for the surface renderer
     *
     * @param con We need to pass in the context, so that we have it when we create bitmaps for drawing operations later. Since the draw operations are
     *          run in a thread, we can't pass the context through the thread (at least not easily)
     */
    SurfaceRenderer(Context con) {
        context = con;
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
    public abstract void suspend();

    /**
     * Rendering updates can be resumed
     */
    public abstract void resume();

    /**
     * Draw to the canvas
     */
    public void draw(Canvas canvas) {
        viewPort.draw(canvas);
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
        viewPort.getOrigin(p);
    }

    /**
     * Set the position (center) of the view
     */
    public void setViewPosition(int x, int y) {
        viewPort.setOrigin(x, y);
    }

    /**
     * Set the position (center) of the view based on map coordinates. This is intended to be used with Tile/HexMaps, and needs to be implemented in
     * the derived player Map class.
     */
    public void setMapPosition(int x, int y) {
        viewPort.setOrigin(x, y);
    }

    /**
     * Get the dimensions of the view
     */
    public void getViewSize(Point p) {
        viewPort.getSize(p);
    }

    /**
     * Set the dimensions of the view
     */
    public void setViewSize(int w, int h) {
        viewPort.setSize(w, h);
    }

    /**
     * Returns a Point representing the size of the scene. Don't modify the returned Point!
     */
    public Point getBackgroundSize() {
        return backgroundSize;
    }


    public void zoom(float scaleFactor, PointF screenFocus) {
        viewPort.zoom(scaleFactor, screenFocus);
    }

    public float getZoom() {
        return viewPort.getZoom();
    }

    /**
     * View Port. This handles the actual drawing, managing dimensions, etc.
     */
    @SuppressWarnings("PublicInnerClass")
    public class ViewPort {
        // The Bitmap of the current ViewPort
        private Bitmap bitmap;
        // The rect defining where the viewport is within the scene
        private final Rect window = new Rect(0, 0, 0, 0);
        // The zoom factor of the viewport
        private float zoom = 1.0f;

        public synchronized void getOrigin(Point p) {
            p.set(window.left, window.top);
        }

        public synchronized void setOrigin(int xp, int yp) {
            int x = xp;
            int y = yp;
            int w = window.width();
            int h = window.height();
            // check bounds
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;
            if ((x + w) > backgroundSize.x)
                x = backgroundSize.x - w;
            if ((y + h) > backgroundSize.y)
                y = backgroundSize.y - h;
            // Set the Window rect
            window.set(x, y, x + w, y + h);
        }

        public synchronized void setSize(int w, int h) {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            Log.d("SF", "Created bitmap of size " + w + ' ' + h);
            int x = window.left;
            int y = window.top;
            // check bounds
            if (x < 0)
                x = 0;
            if (y < 0)
                y = 0;
            if ((x + w) > backgroundSize.x)
                x = backgroundSize.x - w;
            if ((y + h) > backgroundSize.y)
                y = backgroundSize.y - h;
            // Set the Window rect
            window.set(x, y, x + w, y + h);
        }

        public synchronized void getSize(Point p) {
            p.x = window.width();
            p.y = window.height();
        }

        public synchronized void getPhysicalSize(Point p) {
            p.x = getPhysicalWidth();
            p.y = getPhysicalHeight();
        }

        public synchronized int getPhysicalWidth() {
            return bitmap.getWidth();
        }

        public synchronized int getPhysicalHeight() {
            return bitmap.getHeight();
        }

        public synchronized Bitmap getBitmap() {
            return bitmap;
        }

        public synchronized void setBitmap(Bitmap bmp) {
            bitmap = bmp;
        }

        public synchronized float getZoom() {
            return zoom;
        }

        public synchronized void setZoom(float f) {
            zoom = f;
        }

        @SuppressWarnings("OverlyComplexMethod")
        public void zoom(float factor, PointF screenFocus) {
            if (getBitmap() == null) return;
            PointF screenSize = new PointF(getBitmap().getWidth(), getBitmap().getHeight());
            PointF sceneSize = new PointF(getBackgroundSize());
            float screenWidthToHeight = screenSize.x / screenSize.y;
            float screenHeightToWidth = screenSize.y / screenSize.x;
            synchronized (this) {
                float newZoom = zoom * factor;
                RectF w1 = new RectF(window);
                RectF w2 = new RectF();
                PointF sceneFocus = new PointF(
                        w1.left + ((screenFocus.x / screenSize.x) * w1.width()),
                        w1.top + ((screenFocus.y / screenSize.y) * w1.height())
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
                //noinspection NumericCastThatLosesPrecision
                window.set((int) w2.left, (int) w2.top, (int) w2.right, (int) w2.bottom);
                zoom = newZoom;
            }
        }

        void draw(Canvas canvas) {
            drawBase();
            drawLayer();
            drawFinal();
            synchronized (this) {
                if ((canvas != null) && (bitmap != null)) {
                    canvas.drawBitmap(bitmap, 0.0F, 0.0F, null);
                }
            }
        }

        public Rect getWindow() {
            return window;
        }
    }

}
