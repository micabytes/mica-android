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
import com.micabyte.android.graphics.SurfaceRenderer;

/**
 * TileMap superclass
 *
 * @author micabyte
 */
public abstract class TileMap extends BaseObject {
    private static int mapWidth;
    private static int mapHeight;
    private TileMapZone[][] zones_;
    private Rect tileRect_;
    private final Point viewPortOrigin_ = new Point();
    private final Point viewPortSize_ = new Point();

    protected TileMap(String id, String name) {
        super(id, name, 0);
    }

    public void setTileMap(Context c, TileMapZone[][] map) {
        zones_ = map;
        mapHeight = map[0].length;
        mapWidth = map.length;
        tileRect_ = new Rect(0, 0, map[0][0].getWidth(c), map[0][0].getHeight(c));
    }

    public int getRenderHeight() {
        return (mapHeight * tileRect_.height());
    }

    public int getRenderWidth() {
        return (mapWidth * tileRect_.width());
    }

    public int getTileHeight() {
        return tileRect_.height();
    }

    public int getTileWidth() {
        return tileRect_.width();
    }

    public void drawBase(Context c, SurfaceRenderer.ViewPort p) {
        final Canvas canvas = new Canvas(p.bitmap_);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        final float scaleFactor = p.getZoom();
        final int tileSize = tileRect_.width();
        p.getOrigin(viewPortOrigin_);
        p.getSize(viewPortSize_);
        final int windowLeft = viewPortOrigin_.x;
        final int windowTop = viewPortOrigin_.y;
        final int windowRight = viewPortOrigin_.x + viewPortSize_.x;
        final int windowBottom = viewPortOrigin_.y + viewPortSize_.y;
        final Rect destRect = new Rect();
        // Clip tiles not in view
        int iMn = windowLeft / tileSize;
        if (iMn < 0) iMn = 0;
        int jMn = windowTop / tileSize;
        if (jMn < 0) jMn = 0;
        int iMx = (windowRight / tileSize) + 1;
        if (iMx >= mapWidth) iMx = mapWidth;
        int jMx = (windowBottom / tileSize) + 1;
        if (jMx >= mapHeight) jMx = mapHeight;
        // Draw Tiles
        for (int i = iMn; i < iMx; i++) {
            for (int j = jMn; j < jMx; j++) {
                if (zones_[i][j] != null) {
                    destRect.left = (int) (((i * tileSize) - windowLeft) / scaleFactor);
                    destRect.top = (int) (((j * tileSize) - windowTop) / scaleFactor);
                    destRect.right = (int) (((i * tileSize) + tileSize - windowLeft) / scaleFactor);
                    destRect.bottom = (int) (((j * tileSize) + tileSize - windowTop) / scaleFactor);
                    zones_[i][j].drawBase(c, canvas, tileRect_, destRect, paint);
                }
            }
        }
    }

    public abstract void drawLayer(Context c, SurfaceRenderer.ViewPort p);

    public abstract void drawFinal(Context c, SurfaceRenderer.ViewPort p);

}
