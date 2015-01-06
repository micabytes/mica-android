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
import android.graphics.Point;

import com.micabyte.android.map.HexMap;

/**
 * HexMapSurfaceRenderer is a renderer that handles the rendering of a Tiled
 * (hexagonal) map to the screen. The game should subclass the renderer and
 * extend the drawing methods to add other game elements.
 */
public class HexMapSurfaceRenderer extends SurfaceRenderer {
    // The HexMap object
    private HexMap GameSurfaceTileMap_ = null;

    public HexMapSurfaceRenderer(Context c) {
        super(c);
    }

    /**
     * Set the TileMap
     */
    public void setTileMap(HexMap map) {
        GameSurfaceTileMap_ = map;
        backgroundSize_.set(HexMap.getRenderWidth(), HexMap.getRenderHeight());
    }

    @Override
    public void drawBase() {
        GameSurfaceTileMap_.drawBase(context_, viewPort_);
    }

    @Override
    protected void drawLayer() {
        GameSurfaceTileMap_.drawLayer(context_, viewPort_);
    }

    @Override
    protected void drawFinal() {
        GameSurfaceTileMap_.drawFinal(context_, viewPort_);
    }

    @Override
    public void setViewPosition(int x, int y)
    {
        viewPort_.setOrigin(x, y);
    }


    @Override
    public void setMapPosition(int x, int y)
    {
        final Point p = GameSurfaceTileMap_.getViewPortOrigin(x, y, viewPort_);
        viewPort_.setOrigin(p.x, p.y);
    }

    @Override
    public void start() {
        // NOOP
    }

    @Override
    public void stop() {
        // NOOP
    }

    @Override
    public void suspend(boolean suspend) {
        // NOOP
    }

}
