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
package com.micabytes.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

import com.micabytes.gfx.SurfaceRenderer;
import com.micabytes.util.GameLog;

/**
 * HexMap superclass <p/> This implementation works for pointy-side up HexMaps. Needs to be adjusted
 * if it is going to be used for flat-side up maps.
 */
@SuppressWarnings("unused")
public abstract class HexMap {
  private static final String TAG = HexMap.class.getName();
  protected static boolean standardOrientation = true;
  protected static int mapWidth = 0;
  protected static int mapHeight = 0;
  protected static int tileSlope = 0;
  protected static Rect tileRect = new Rect();
  protected TileMapZone[][] zones;
  protected float scaleFactor;
  protected final Point viewPortOrigin = new Point();
  protected final Point viewPortSize = new Point();
  protected final Rect destRect = new Rect();
  protected int windowLeft;
  protected int windowTop;
  protected int windowRight;
  protected int windowBottom;
  protected final Paint tilePaint = new Paint();
  protected final Paint tileText = new Paint();
  // Draw
  protected final Canvas canvas = new Canvas();

  protected HexMap() {
    tilePaint.setAntiAlias(true);
    tilePaint.setFilterBitmap(true);
    tilePaint.setDither(true);
    Paint select = new Paint();
    select.setStyle(Paint.Style.STROKE);
    select.setColor(Color.RED);
    select.setStrokeWidth(2);

  }

  @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
  public void setHexMap(TileMapZone[][] map) {
    zones = new TileMapZone[map.length][map[0].length];
    for (int i = 0; i < map.length; i++) {
      System.arraycopy(map[i], 0, zones[i], 0, map[i].length);
    }
    mapHeight = map[0].length;
    mapWidth = map.length;
    tileRect = new Rect(0, 0, map[0][0].getWidth(), map[0][0].getHeight());
    tileSlope = tileRect.height() / 4;
  }

  @SuppressWarnings({"MethodWithMultipleLoops", "OverlyComplexMethod", "OverlyLongMethod", "NumericCastThatLosesPrecision"})
  public void drawBase(Context con, SurfaceRenderer.ViewPort p) {
    if (p.getBitmap() == null) {
      GameLog.e(TAG, "Viewport bitmap is null");
      return;
    }
    canvas.setBitmap(p.getBitmap());
    scaleFactor = p.getZoom();
    int yOffset = tileRect.height() - tileSlope;
    p.getOrigin(viewPortOrigin);
    p.getSize(viewPortSize);
    windowLeft = viewPortOrigin.x;
    windowTop = viewPortOrigin.y;
    windowRight = viewPortOrigin.x + viewPortSize.x;
    windowBottom = viewPortOrigin.y + viewPortSize.y;
    int xOffset;
    if (standardOrientation) {
      // Clip tiles not in view
      int iMn = (windowLeft / tileRect.width()) - 1;
      if (iMn < 0) iMn = 0;
      int jMn = (windowTop / (tileRect.height() - tileSlope)) - 1;
      if (jMn < 0) jMn = 0;
      int iMx = (windowRight / tileRect.width()) + 2;
      if (iMx >= mapWidth) iMx = mapWidth;
      int jMx = (windowBottom / (tileRect.height() - tileSlope)) + 2;
      if (jMx >= mapHeight) jMx = mapHeight;
      // Draw Tiles
      for (int i = iMn; i < iMx; i++) {
        for (int j = jMn; j < jMx; j++) {
          if (zones[i][j] != null) {
            xOffset = (j % 2) == 0 ? tileRect.width() / 2 : 0;
            destRect.left = (int) (((i * tileRect.width()) - windowLeft - xOffset) / scaleFactor);
            destRect.top = (int) (((j * (tileRect.height() - tileSlope)) - windowTop - yOffset) / scaleFactor);
            destRect.right = (int) ((((i * tileRect.width()) + tileRect.width()) - windowLeft - xOffset) / scaleFactor);
            destRect.bottom = (int) ((((j * (tileRect.height() - tileSlope)) + tileRect.height()) - windowTop - yOffset) / scaleFactor);
            zones[i][j].drawBase(canvas, tileRect, destRect, tilePaint);
          }
        }
      }
    } else {
      // Clip tiles not in view
      int iMn = mapWidth - (windowRight / tileRect.width()) - 2;
      if (iMn < 0) iMn = 0;
      int jMn = mapHeight - ((windowBottom / (tileRect.height() - tileSlope)) + 2);
      if (jMn < 0) jMn = 0;
      int iMx = mapWidth - ((windowLeft / tileRect.width()) + 1);
      if (iMx >= mapWidth) iMx = mapWidth - 1;
      int jMx = mapHeight - ((windowTop / (tileRect.height() - tileSlope)) + 1);
      if (jMx >= mapHeight) jMx = mapHeight - 1;
      // Draw Tiles
      for (int i = iMx; i >= iMn; i--) {
        for (int j = jMx; j >= jMn; j--) {
          if (zones[i][j] != null) {
            xOffset = (j % 2) == 1 ? tileRect.width() / 2 : 0;
            destRect.left = (int) ((((mapWidth - i - 1) * tileRect.width()) - windowLeft - xOffset) / scaleFactor);
            destRect.top = (int) ((((mapHeight - j - 1) * (tileRect.height() - tileSlope)) - windowTop - yOffset) / scaleFactor);
            destRect.right = (int) (((((mapWidth - i - 1) * tileRect.width()) + tileRect.width()) - windowLeft - xOffset) / scaleFactor);
            destRect.bottom = (int) (((((mapHeight - j - 1) * (tileRect.height() - tileSlope)) + tileRect.height()) - windowTop - yOffset) / scaleFactor);
            zones[i][j].drawBase(canvas, tileRect, destRect, tilePaint);
          }
        }
      }
    }
  }

  public abstract void drawLayer(Context context, SurfaceRenderer.ViewPort p);

  public abstract void drawFinal(Context context, SurfaceRenderer.ViewPort p);

  public abstract Point getViewPortOrigin(int x, int y, SurfaceRenderer.ViewPort p);

  public static boolean isStandardOrientation() {
    return standardOrientation;
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static void setStandardOrientation(boolean orient) {
    standardOrientation = orient;
  }

  public static int getMapWidth() {
    return mapWidth;
  }

  public static void setMapWidth(int i) {
    mapWidth = i;
  }

  public static int getMapHeight() {
    return mapHeight;
  }

  public static void setMapHeight(int i) {
    mapHeight = i;
  }

  public static int getTileSlope() {
    return tileSlope;
  }

  public static void setTileSlope(int i) {
    tileSlope = i;
  }

  public static Rect getTileRect() {
    return tileRect;
  }

  public static void setTileRect(Rect rect) {
    tileRect = rect;
  }

}
