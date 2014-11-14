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

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.util.Log;

import com.micabyte.android.BuildConfig;

/**
 * GameSurfaceRendererBitmap is a renderer that handles the rendering of a background bitmap to the
 * screen (e.g., a game map). It is able to do this even if the bitmap is too large to fit into
 * memory. The game should subclass the renderer and extend the drawing methods to add other game
 * elements.
 *
 * @author micabyte
 */
@SuppressWarnings("WeakerAccess")
public class BitmapSurfaceRenderer extends SurfaceRenderer {
    private static final String TAG = BitmapSurfaceRenderer.class.getName();
    // Default Settings
    public static final Config DEFAULT_CONFIG = Config.RGB_565;
    private static final int DEFAULT_SAMPLE_SIZE = 2;
    private static final int DEFAULT_MEM_USAGE = 20;
    private static final float DEFAULT_THRESHOLD = 0.75f;
    // BitmapRegionDecoder - this is the class that does the magic
    private BitmapRegionDecoder decoder_;
    // The cached portion of the background image
    private final CacheBitmap cachedBitmap_ = new CacheBitmap();
    // The low resolution version of the background image
    private Bitmap lowResBitmap_;
    /**
     * Options for loading the bitmaps
     */
    private final BitmapFactory.Options options_ = new BitmapFactory.Options();
    /**
     * What is the down sample size for the sample image? 1=1/2, 2=1/4 3=1/8, etc
     */
    private final int sampleSize_;
    /**
     * What percent of total memory should we use for the cache? The bigger the cache, the longer it
     * takes to read -- 1.2 secs for 25%, 600ms for 10%, 500ms for 5%. User experience seems to be
     * best for smaller values.
     */
    private int memUsage_;
    /**
     * Threshold for using low resolution image
     */
    private final float lowResThreshold_;
    /**
     * Calculated rect
     */
    private final Rect calculatedCacheWindowRect = new Rect();

    private BitmapSurfaceRenderer(Context c) {
        super(c);
        this.options_.inPreferredConfig = DEFAULT_CONFIG;
        this.sampleSize_ = DEFAULT_SAMPLE_SIZE;
        this.memUsage_ = DEFAULT_MEM_USAGE;
        this.lowResThreshold_ = DEFAULT_THRESHOLD;
    }

    protected BitmapSurfaceRenderer(Context c, Bitmap.Config config, int sampleSize, int memUsage, float threshold) {
        super(c);
        this.options_.inPreferredConfig = config;
        this.sampleSize_ = sampleSize;
        this.memUsage_ = memUsage;
        this.lowResThreshold_ = threshold;
    }

    /**
     * Set the Background bitmap
     *
     * @param inputStream InputStream to the raw data of the bitmap
     * @throws IOException
     */
    public void setBitmap(InputStream inputStream) throws IOException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        this.decoder_ = BitmapRegionDecoder.newInstance(inputStream, false);
        inputStream.reset();
        // Grab the bounds of the background bitmap
        opt.inPreferredConfig = DEFAULT_CONFIG;
        opt.inJustDecodeBounds = true;
        if (BuildConfig.DEBUG) Log.d(TAG, "Decode inputStream for Background Bitmap");
        BitmapFactory.decodeStream(inputStream, null, opt);
        inputStream.reset();
        this.backgroundSize_.set(opt.outWidth, opt.outHeight);
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Background Image: w=" + opt.outWidth + " h=" + opt.outHeight);
        // Create the low resolution background
        opt.inJustDecodeBounds = false;
        opt.inSampleSize = (1 << this.sampleSize_);
        this.lowResBitmap_ = BitmapFactory.decodeStream(inputStream, null, opt);
        if (BuildConfig.DEBUG)
            Log.i(TAG, "Low Res Image: w=" + this.lowResBitmap_.getWidth() + " h=" + this.lowResBitmap_.getHeight());
        // Initialize cache
        if (this.cachedBitmap_.getState() == CacheState.NOT_INITIALIZED) {
            synchronized (this.cachedBitmap_) {
                this.cachedBitmap_.setState(CacheState.IS_INITIALIZED);
            }
        }
    }

    @Override
    protected void drawBase() {
        this.cachedBitmap_.draw(this.viewPort_);
    }

    @Override
    protected void drawLayer() {
        // NOOP - override function and add game specific code
    }

    @Override
    protected void drawFinal() {
        // NOOP - override function and add game specific code
    }

    /**
     * Starts the renderer
     */
    @Override
    public void start() {
        this.cachedBitmap_.start();
    }

    /**
     * Stops the renderer
     */
    @Override
    public void stop() {
        this.cachedBitmap_.stop();
    }

    /**
     * Suspend the renderer
     */
    @Override
    public void suspend(boolean suspend) {
        this.cachedBitmap_.suspend(suspend);
    }

    /**
     * Invalidate the cache.
     */
    public void invalidate() {
        this.cachedBitmap_.invalidate();
    }


    /**
     * Loads the relevant slice of the background bitmap that needs to be kept in memory.
     * <p/>
     * The loading can take a long time depending on the size.
     *
     * @param rect The portion of the background bitmap to be cached
     * @return The bitmap representing the requested area of the background
     */
    Bitmap loadCachedBitmap(Rect rect) {
        return this.decoder_.decodeRegion(rect, this.options_);
    }

    /**
     * This function tries to recover from an OutOfMemoryError in the CacheThread.
     *
     * @param error The OutOfMemoryError exception data
     */
    void cacheBitmapOutOfMemoryError(OutOfMemoryError error) {
        if (this.memUsage_ > 0) this.memUsage_ -= 1;
        Log.e(TAG, "OutOfMemory caught; reducing cache size to " + this.memUsage_ + " percent.");
        error.printStackTrace();
    }

    /**
     * This method fills the passed-in bitmap with sample data. This function must return data fast;
     * this is our fall back solution in all the cases where the user is moving too fast for us to
     * load the actual bitmap data from memory. The quality of the user experience rests on the
     * speed of this function.
     */
    void drawLowResolutionBackground(Bitmap bitmap, Rect rect) {
        int left = (rect.left >> this.sampleSize_);
        int top = (rect.top >> this.sampleSize_);
        int right = (rect.right >> this.sampleSize_);
        int bottom = (rect.bottom >> this.sampleSize_);
        Rect srcRect = new Rect(left, top, right, bottom);
        Rect dstRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Draw to Canvas
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(this.lowResBitmap_, srcRect, dstRect, null);
    }

    /**
     * Determine the dimensions of the CacheBitmap based on the current ViewPort.
     * <p/>
     * Minimum size is equal to the viewport; otherwise it is dimensioned relative to the available
     * memory. {@link CacheBitmap} is locked while the calculation is done, so this has to be fast.
     *
     * @param rect The dimensions of the current viewport
     * @return The dimensions of the cache
     */
    Rect calculateCacheDimensions(Rect rect) {
        final int BYTES_PER_PIXEL = 4;
        long bytesToUse = Runtime.getRuntime().maxMemory() * this.memUsage_ / 100;
        Point sz = getBackgroundSize();
        int vw = rect.width();
        int vh = rect.height();
        if (BuildConfig.DEBUG) Log.d(TAG, "old cache.originRect = " + rect.toShortString());
        // Calculate the margins within the memory budget
        int tw = 0;
        int th = 0;
        int mw = tw;
        int mh = th;
        while ((vw + tw) * (vh + th) * BYTES_PER_PIXEL < bytesToUse) {
            mw = tw++;
            mh = th++;
        }
        // Trim margins to image size
        if (vw + mw > sz.x) mw = Math.max(0, sz.x - vw);
        if (vh + mh > sz.y) mh = Math.max(0, sz.y - vh);
        // Figure out the left & right based on the margin.
        // LATER: THe logic here assumes that the viewport is <= our size.
        // If that's not the case, then this logic breaks.
        int left = rect.left - (mw >> 1);
        int right = rect.right + (mw >> 1);
        if (left < 0) {
            right = right - left; // Adds the overage on the left side back to the right
            left = 0;
        }
        if (right > sz.x) {
            left = left - (right - sz.x); // Adds overage on right side back to left
            right = sz.x;
        }
        // Figure out the top & bottom based on the margin. We assume our viewport
        // is <= our size. If that's not the case, then this logic breaks.
        int top = rect.top - (mh >> 1);
        int bottom = rect.bottom + (mh >> 1);
        if (top < 0) {
            bottom = bottom - top; // Adds the overage on the top back to the bottom
            top = 0;
        }
        if (bottom > sz.y) {
            top = top - (bottom - sz.y); // Adds overage on bottom back to top
            bottom = sz.y;
        }
        // Set the origin based on our new calculated values.
        this.calculatedCacheWindowRect.set(left, top, right, bottom);
        if (BuildConfig.DEBUG)
            if (BuildConfig.DEBUG) Log.d(TAG, "new cache.originRect = " + this.calculatedCacheWindowRect.toShortString() + " size=" + sz.toString());
        return this.calculatedCacheWindowRect;
    }


    /**
     * The current state of the cached bitmap
     */
    private enum CacheState {
        READY, NOT_INITIALIZED, IS_INITIALIZED, BEGIN_UPDATE, IS_UPDATING, DISABLED
    }

    /**
     * The cached bitmap object. This object is continually kept up to date by CacheThread. If the
     * object is locked, the background is updated using the low resolution background image instead
     */
    private class CacheBitmap {
        /**
         * The current position and dimensions of the cache within the background image
         */
        final Rect cacheWindow_ = new Rect(0, 0, 0, 0);
        /**
         * The current state of the cache
         */
        private CacheState state_ = CacheState.NOT_INITIALIZED;
        /**
         * The currently cached bitmap
         */
        Bitmap bitmap_ = null;
        /**
         * The cache bitmap loading thread
         */
        private CacheThread cacheThread_;

        public CacheBitmap() {
            super();
        }

        CacheState getState() {
            return this.state_;
        }

        void setState(CacheState newState) {
            this.state_ = newState;
        }

        void start() {
            if (this.cacheThread_ != null) {
                this.cacheThread_.setRunning(false);
                this.cacheThread_.interrupt();
                this.cacheThread_ = null;
            }
            this.cacheThread_ = new CacheThread(this);
            this.cacheThread_.setName("cacheThread");
            this.cacheThread_.start();
        }

        void stop() {
            this.cacheThread_.setRunning(false);
            this.cacheThread_.interrupt();
            boolean retry = true;
            while (retry) {
                try {
                    this.cacheThread_.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // Wait until thread is dead
                }
            }
            this.cacheThread_ = null;
        }

        void invalidate() {
            synchronized (this) {
                setState(CacheState.IS_INITIALIZED);
                this.cacheThread_.interrupt();
            }
        }

        public void suspend(boolean suspend) {
            // Suspends or resume the cache thread.
            if (suspend) {
                synchronized (this) {
                    setState(CacheState.DISABLED);
                }
            } else {
                if (getState() == CacheState.DISABLED) {
                    synchronized (this) {
                        setState(CacheState.IS_INITIALIZED);
                    }
                }
            }
        }

        /**
         * Draw the CacheBitmap on the viewport
         */
        void draw(ViewPort p) {
            Bitmap bitmap = null;
            synchronized (this) {
                switch (getState()) {
                    case NOT_INITIALIZED:
                        // Error
                        Log.e(TAG, "Attempting to update an uninitialized CacheBitmap");
                        return;
                    case IS_INITIALIZED:
                        // Start data caching
                        setState(CacheState.BEGIN_UPDATE);
                        this.cacheThread_.interrupt();
                        break;
                    case BEGIN_UPDATE:
                    case IS_UPDATING:
                        // Currently updating; low resolution version used
                        break;
                    case DISABLED:
                        // Use of high resolution version disabled
                        break;
                    case READY:
                        if (this.bitmap_ == null) {
                            // No data loaded
                            setState(CacheState.BEGIN_UPDATE);
                            this.cacheThread_.interrupt();
                        } else if (!this.cacheWindow_.contains(p.window)) {
                            // No cached data available
                            setState(CacheState.BEGIN_UPDATE);
                            this.cacheThread_.interrupt();
                        } else {
                            bitmap = this.bitmap_;
                        }
                        break;
                }
            }
            // Use the low resolution version if the cache is empty or scale factor is < threshold
            if (bitmap == null) //|| (BitmapSurfaceRenderer.this.scaleFactor_ < BitmapSurfaceRenderer.this.lowResThreshold_))
                drawLowResolution();
            else
                drawHighResolution(bitmap);
        }

        /**
         * Used to hold the source Rect for bitmap drawing
         */
        private final Rect srcRect_ = new Rect(0, 0, 0, 0);
        /**
         * Used to hold the dest Rect for bitmap drawing
         */
        private final Rect dstRect_ = new Rect(0, 0, 0, 0);
        private final Point dstSize_ = new Point();

        /**
         * Use the high resolution cached bitmap for drawing
         */
        void drawHighResolution(Bitmap bitmap) {
            Rect wSize = BitmapSurfaceRenderer.this.viewPort_.window;
            if (bitmap != null) {
                synchronized (BitmapSurfaceRenderer.this.viewPort_) {
                    int left = wSize.left - this.cacheWindow_.left;
                    int top = wSize.top - this.cacheWindow_.top;
                    int right = left + wSize.width();
                    int bottom = top + wSize.height();
                    BitmapSurfaceRenderer.this.viewPort_.getPhysicalSize(this.dstSize_);
                    this.srcRect_.set(left, top, right, bottom);
                    this.dstRect_.set(0, 0, this.dstSize_.x, this.dstSize_.y);
                    Canvas canvas = new Canvas(BitmapSurfaceRenderer.this.viewPort_.bitmap_);
                    canvas.drawBitmap(bitmap, this.srcRect_, this.dstRect_, null);
                }
            }
        }

        void drawLowResolution() {
            if (getState() != CacheState.NOT_INITIALIZED) {
                synchronized (BitmapSurfaceRenderer.this.viewPort_) {
                    drawLowResolutionBackground(BitmapSurfaceRenderer.this.viewPort_.bitmap_, BitmapSurfaceRenderer.this.viewPort_.window);
                }
            }
        }

    }

    /**
     * This thread handles the background loading of the {@link CacheBitmap}.
     * <p/>
     * The CacheThread starts an update when the {@link CacheBitmap#state_} is
     * {@link CacheState#BEGIN_UPDATE} and updates the bitmap given the current window.
     * <p/>
     * The CacheThread needs to be careful how it locks {@link CacheBitmap} in order to ensure the
     * smoothest possible performance (loading can take a while).
     */
    class CacheThread extends Thread {
        private boolean isRunning_ = false;
        // The CacheBitmap
        private final CacheBitmap cache_;

        CacheThread(CacheBitmap cache) {
            setName("CacheThread");
            this.cache_ = cache;
        }

        @Override
        public void run() {
            this.isRunning_ = true;
            Rect viewportRect = new Rect(0, 0, 0, 0);
            while (this.isRunning_) {
                // Wait until we are ready to go
                while (this.isRunning_ && this.cache_.getState() != CacheState.BEGIN_UPDATE) {
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    } catch (InterruptedException e) {
                        // NOOP
                    }
                }
                if (!this.isRunning_) return;
                // Start Loading Timer
                long startTime = System.currentTimeMillis();
                // Load Data
                boolean continueLoading = false;
                synchronized (this.cache_) {
                    if (this.cache_.getState() == CacheState.BEGIN_UPDATE) {
                        this.cache_.setState(CacheState.IS_UPDATING);
                        this.cache_.bitmap_ = null;
                        continueLoading = true;
                    }
                }
                if (continueLoading) {
                    synchronized (BitmapSurfaceRenderer.this.viewPort_) {
                        viewportRect.set(BitmapSurfaceRenderer.this.viewPort_.window);
                    }
                    synchronized (this.cache_) {
                        if (this.cache_.getState() == CacheState.IS_UPDATING)
                            this.cache_.cacheWindow_.set(calculateCacheDimensions(viewportRect));
                        else
                            continueLoading = false;
                    }
                    if (continueLoading) {
                        try {
                            Bitmap bitmap = loadCachedBitmap(this.cache_.cacheWindow_);
                            if (bitmap != null) {
                                synchronized (this.cache_) {
                                    if (this.cache_.getState() == CacheState.IS_UPDATING) {
                                        this.cache_.bitmap_ = bitmap;
                                        this.cache_.setState(CacheState.READY);
                                    } else {
                                        Log.w(TAG, "Loading of background image cache aborted");
                                    }
                                }
                            }
                            // End Loading Timer
                            long endTime = System.currentTimeMillis();
                            if (BuildConfig.DEBUG)
                                if (BuildConfig.DEBUG) Log.d(TAG, "Loaded background image in " + (endTime - startTime) + "ms");
                        } catch (OutOfMemoryError e) {
                            if (BuildConfig.DEBUG) Log.d(TAG, "CacheThread out of memory");
                            // Out of memory error detected. Lower the memory allocation
                            synchronized (this.cache_) {
                                cacheBitmapOutOfMemoryError(e);
                                if (this.cache_.getState() == CacheState.IS_UPDATING) {
                                    this.cache_.setState(CacheState.BEGIN_UPDATE);
                                }
                            }
                        }
                    }
                }
            }
        }

        @SuppressWarnings("SameParameterValue")
        public void setRunning(boolean b) {
            this.isRunning_ = b;
        }

    }

}
