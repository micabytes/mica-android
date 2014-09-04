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
import android.graphics.Rect;

import com.micabyte.android.BaseObject;
import com.micabyte.android.graphics.ImageHandler;

/**
 * Zone superclass for TileMap and HexMap
 *
 * @author micabyte
 */
public abstract class TileMapZone extends BaseObject {

    protected TileMapZone(String id, String name, int bmp) {
        super(id, name, bmp);
    }

    /**
     * Get the width of a tile
     */
    public int getWidth(Context c) {
        return ImageHandler.getInstance(c).get(getValue()).getWidth();
    }

    /**
     * Get the height of a tile
     */
    public int getHeight(Context c) {
        return ImageHandler.getInstance(c).get(getValue()).getHeight();
    }

    /**
     * Draw the base bitmap of the tile on a canvas
     */
    public void drawBase(Context c, Canvas canvas, Rect tileRect, Rect destRect, Paint paint) {
        canvas.drawBitmap(ImageHandler.getInstance(c).get(getValue(), true), tileRect, destRect, paint);
    }


}
