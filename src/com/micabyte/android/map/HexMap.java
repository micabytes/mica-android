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
package com.micabyte.android.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.micabyte.android.BaseObject;
import com.micabyte.android.graphics.SurfaceRenderer.ViewPort;

/**
 * HexMap superclass
 * 
 * This implementation works for pointy-side up hexmaps. Needs to be adjusted
 * if it is going to be used for flat-side up maps.
 * 
 * @author micabyte
 */
public abstract class HexMap extends BaseObject {
	public static boolean standardOrientation = true;
    public static int mapWidth = 0;
    public static int mapHeight = 0;
    public static int tileSlope = 0;
    public static Rect tileRect = new Rect();
    protected TileMapZone[][] zones_;
    protected float scaleFactor = 0;
	protected final Point viewPortOrigin_ = new Point();
	protected final Point viewPortSize_ = new Point();
	protected final Rect destRect = new Rect();
    protected int windowLeft = 0;
    protected int windowTop = 0;
    protected int windowRight = 0;
    protected int windowBottom = 0;
	protected final Paint tilePaint = new Paint();
	protected final Paint select = new Paint();
	protected final Paint tileText = new Paint();

    protected HexMap(String id, String name) {
        super(id, name, 0);
        this.tilePaint.setAntiAlias(true);
        this.tilePaint.setFilterBitmap(true);
        this.tilePaint.setDither(true);
		this.select.setStyle(Paint.Style.STROKE);
		this.select.setColor(Color.RED);
		this.select.setStrokeWidth(2);
        
    }

    public void setHexMap(Context c, TileMapZone[][] map) {
        this.zones_ = map;
        HexMap.mapHeight = map[0].length;
        HexMap.mapWidth = map.length;
        HexMap.tileRect = new Rect(0, 0, map[0][0].getWidth(c), map[0][0].getHeight(c));
        HexMap.tileSlope = HexMap.tileRect.height()/4;
    }

    public static int getRenderHeight() {
        return ((HexMap.mapHeight - 2 )* (HexMap.tileRect.height() - HexMap.tileSlope)) + (HexMap.tileSlope);
    }

    public static int getRenderWidth() {
        return ((HexMap.mapWidth) * HexMap.tileRect.width()) - (HexMap.tileRect.width()/2);
    }
    
    public void drawBase(Context c, ViewPort p) {
    	Canvas canvas = new Canvas(p.bitmap_);
    	this.scaleFactor = p.getZoom();
        int yOffset = (HexMap.tileRect.height() - HexMap.tileSlope);
        int xOffset = 0;
		p.getOrigin(this.viewPortOrigin_);
		p.getSize(this.viewPortSize_);
        this.windowLeft = this.viewPortOrigin_.x;
        this.windowTop = this.viewPortOrigin_.y;
        this.windowRight = this.viewPortOrigin_.x + this.viewPortSize_.x;
        this.windowBottom = this.viewPortOrigin_.y + this.viewPortSize_.y;
		if (standardOrientation) {
	        // Clip tiles not in view
	        int iMn = (this.windowLeft / HexMap.tileRect.width()) - 1;
	        if (iMn < 0) iMn = 0;
	        int jMn = (this.windowTop / (HexMap.tileRect.height() - HexMap.tileSlope)) - 1;
	        if (jMn < 0) jMn = 0;
	        int iMx = (this.windowRight / HexMap.tileRect.width()) + 2;
	        if (iMx >= HexMap.mapWidth) iMx = HexMap.mapWidth;
	        int jMx = (this.windowBottom / (HexMap.tileRect.height() - HexMap.tileSlope)) + 2;
	        if (jMx >= HexMap.mapHeight) jMx = HexMap.mapHeight;
	        // Draw Tiles
	        for (int i = iMn; i < iMx; i++) {
	            for (int j = jMn; j < jMx; j++) {
	                if (this.zones_[i][j] != null) {
	                	if (j % 2 == 0)
	                		xOffset = HexMap.tileRect.width()/2;
	                	else
	                		xOffset = 0;
	                    this.destRect.left = (int) (((i * HexMap.tileRect.width()) - this.windowLeft - xOffset) / this.scaleFactor);
	                    this.destRect.top = (int) (((j * (HexMap.tileRect.height() - HexMap.tileSlope)) - this.windowTop - yOffset) / this.scaleFactor);
	                    this.destRect.right = (int) (((i * HexMap.tileRect.width()) + HexMap.tileRect.width() - this.windowLeft - xOffset) / this.scaleFactor);
	                    this.destRect.bottom = (int) (((j * (HexMap.tileRect.height() - HexMap.tileSlope)) + HexMap.tileRect.height() - this.windowTop - yOffset) / this.scaleFactor);
	                    this.zones_[i][j].drawBase(c, canvas, HexMap.tileRect, this.destRect, this.tilePaint);
	                }
	            }
	        }
		}
		else {
	        // Clip tiles not in view
	        int iMn = HexMap.mapWidth - (this.windowRight / HexMap.tileRect.width()) - 2;
	        if (iMn < 0) iMn = 0;
	        int jMn = HexMap.mapHeight - ((this.windowBottom / (HexMap.tileRect.height() - HexMap.tileSlope)) + 2);
	        if (jMn < 0) jMn = 0;
	        int iMx = HexMap.mapWidth - ((this.windowLeft / HexMap.tileRect.width()) + 1);
	        if (iMx >= HexMap.mapWidth) iMx = HexMap.mapWidth;
	        int jMx = HexMap.mapHeight - (this.windowTop / (HexMap.tileRect.height() - HexMap.tileSlope) + 1);
	        if (jMx >= HexMap.mapHeight) jMx = HexMap.mapHeight;
	        //Log.d("HexMap", "Window Dimensions: " + iMn + " " + iMx + " " + jMn + " " + jMx + " " + windowLeft + " " + windowRight + " " + this.tileRect_.width());
	        // Draw Tiles
	        for (int i = iMx; i >= iMn; i--) {
	            for (int j = jMx; j >= jMn; j--) {
	                if (this.zones_[i][j] != null) {
	                	if (j % 2 == 1)
	                		xOffset = HexMap.tileRect.width()/2;
	                	else
	                		xOffset = 0;
	                    this.destRect.left = (int) ((((HexMap.mapWidth - i - 1) * HexMap.tileRect.width()) - this.windowLeft - xOffset) / this.scaleFactor);
	                    this.destRect.top = (int) ((((HexMap.mapHeight - j - 1) * (HexMap.tileRect.height() - HexMap.tileSlope)) - this.windowTop - yOffset) / this.scaleFactor);
	                    this.destRect.right = (int) ((((HexMap.mapWidth - i - 1) * HexMap.tileRect.width()) + HexMap.tileRect.width() - this.windowLeft - xOffset) / this.scaleFactor);
	                    this.destRect.bottom = (int) ((((HexMap.mapHeight - j - 1) * (HexMap.tileRect.height() - HexMap.tileSlope)) + HexMap.tileRect.height() - this.windowTop - yOffset) / this.scaleFactor);
	                    this.zones_[i][j].drawBase(c, canvas, HexMap.tileRect, this.destRect, this.tilePaint);
	                }
	            }
	        }
		}
    }

    public abstract void drawLayer(Context c, ViewPort p);

    public abstract void drawFinal(Context c, ViewPort p);
    
}
