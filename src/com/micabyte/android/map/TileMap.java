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
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.micabyte.android.BaseObject;
import com.micabyte.android.graphics.SurfaceRenderer.ViewPort;

/**
 * TileMap superclass
 * 
 * @author micabyte
 */
public abstract class TileMap extends BaseObject {
    public static int mapWidth;
    public static int mapHeight;
    protected TileMapZone[][] zones_;
    protected Rect tileRect_;

    protected TileMap(String id, String name) {
        super(id, name);
    }

    public void setTileMap(Context c, TileMapZone[][] map) {
        this.zones_ = map;
        TileMap.mapHeight = map[0].length;
        TileMap.mapWidth = map.length;
        this.tileRect_ = new Rect(0, 0, map[0][0].getTileSize(c), map[0][0].getTileSize(c));
    }
    
    public int getRenderHeight() {
        return (TileMap.mapHeight * this.tileRect_.height());
    }

    public int getRenderWidth() {
        return (TileMap.mapWidth * this.tileRect_.width());
    }
    
    public int getTileHeight() {
    	return this.tileRect_.width();
    }

    public int getTileWidth() {
    	return this.tileRect_.height();
    }
    
    public void drawBase(Context c, ViewPort p, float scaleFactor) {
        Canvas canvas = new Canvas(p.bitmap_);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        int tileSize = this.tileRect_.width();
        Point viewPortCenter = new Point();
        p.getViewPosition(viewPortCenter);
        int windowLeft = (int) (viewPortCenter.x - (p.getViewPortSize().right / (scaleFactor * 2)));
        int windowTop = (int) (viewPortCenter.y - (p.getViewPortSize().bottom / (scaleFactor * 2)));
        int windowRight =
                (int) (viewPortCenter.x + (p.getViewPortSize().right / (scaleFactor * 2)));
        int windowBottom =
                (int) (viewPortCenter.y + (p.getViewPortSize().bottom / (scaleFactor * 2)));
        Rect destRect = new Rect();
        // Clip tiles not in view
        int iMn = windowLeft / tileSize;
        if (iMn < 0) iMn = 0;
        int jMn = windowTop / tileSize;
        if (jMn < 0) jMn = 0;
        int iMx = (windowRight / tileSize) + 1;
        if (iMx >= TileMap.mapWidth) iMx = TileMap.mapWidth;
        int jMx = (windowBottom / tileSize) + 1;
        if (jMx >= TileMap.mapHeight) jMx = TileMap.mapHeight;
        // Draw Tiles
        for (int i = iMn; i < iMx; i++) {
            for (int j = jMn; j < jMx; j++) {
                if (this.zones_[i][j] != null) {
                    destRect.left = (int) (((i * tileSize) - windowLeft) * scaleFactor);
                    destRect.top = (int) (((j * tileSize) - windowTop) * scaleFactor);
                    destRect.right = (int) (((i * tileSize) + tileSize - windowLeft) * scaleFactor);
                    destRect.bottom = (int) (((j * tileSize) + tileSize - windowTop) * scaleFactor);
                    this.zones_[i][j].drawBase(c, canvas, this.tileRect_, destRect, paint);
                }
            }
        }
    }

    public abstract void drawLayer(Context c, ViewPort p, float scaleFactor);

}
