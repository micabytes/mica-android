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
package com.micabytes.gfx

import android.content.Context
import android.graphics.Point

import com.micabytes.map.HexMap

/**
 * HexMapSurfaceRenderer is a renderer that handles the rendering of a Tiled
 * (hexagonal) map to the screen. The game should subclass the renderer and
 * extend the drawing methods to add other game elements.
 */
class HexMapSurfaceRenderer(con: Context) : SurfaceRenderer(con) {
  // The HexMap object
  private var gameSurfaceTileMap: HexMap? = null

  private val renderWidth: Int
    get() = HexMap.mapWidth * HexMap.tileRect.width() - HexMap.tileRect.width() / 2

  private val renderHeight: Int
    get() = (HexMap.mapHeight - 2) * (HexMap.tileRect.height() - HexMap.tileSlope) + HexMap.tileSlope

  /**
   * Set the TileMap
   */
  fun setTileMap(map: HexMap) {
    gameSurfaceTileMap = map
    backgroundSize.set(renderWidth, renderHeight)
  }

  public override fun drawBase() {
    gameSurfaceTileMap!!.drawBase(context, viewPort)
  }

  override fun drawLayer() {
    gameSurfaceTileMap!!.drawLayer(context, viewPort)
  }

  override fun drawFinal() {
    gameSurfaceTileMap!!.drawFinal(context, viewPort)
  }

  override fun setMapPosition(x: Int, y: Int) {
    val p = gameSurfaceTileMap!!.getViewPortOrigin(x, y, viewPort)
    super.setMapPosition(p.x, p.y)
  }

  override fun start() {
    // NOOP
  }

  override fun stop() {
    // NOOP
  }

  override fun suspend() {
    // NOOP
  }

  override fun resume() {
    // NOOP
  }

}
