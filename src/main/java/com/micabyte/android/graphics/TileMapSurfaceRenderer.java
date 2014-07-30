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

import com.micabyte.android.map.TileMap;

/**
 * TileMapSurfaceRenderer is a renderer that handles the rendering of a Tiled
 * (square) map to the screen. The game should subclass the renderer and extend
 * the drawing methods to add other game elements.
 */
public class TileMapSurfaceRenderer extends SurfaceRenderer {
    public static final String TAG = TileMapSurfaceRenderer.class.getName();
    // The low resolution version of the background image
    private TileMap GameSurfaceTileMap_ = null;

    private TileMapSurfaceRenderer(Context c) {
        super(c);
    }

    /**
     * Set the TileMap
     */
    public void setTileMap(TileMap map) {
        this.GameSurfaceTileMap_ = map;
        this.backgroundSize_.set(this.GameSurfaceTileMap_.getRenderWidth(), this.GameSurfaceTileMap_.getRenderHeight());
    }

    @Override
    public void drawBase() {
        this.GameSurfaceTileMap_.drawBase(this.context_, this.viewPort_);
    }

    @Override
    protected void drawLayer() {
        this.GameSurfaceTileMap_.drawLayer(this.context_, this.viewPort_);
    }

    @Override
    protected void drawFinal() {
        this.GameSurfaceTileMap_.drawFinal(this.context_, this.viewPort_);
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
