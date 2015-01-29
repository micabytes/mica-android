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
package com.micabyte.android.math;


/**
 * Minimal Polygon class for Android.
 * <p/>
 * Basic polygon wrapper to handle simple point in polygon problems.
 */
class Polygon {
    // Polygon coordinates.
    private final int[] polyX;
    private final int[] polyY;
    // Number of sides in the polygon.
    private final int polyN;

    public Polygon(int[] px, int[] py, int ps) {
        polyX = px;
        polyY = py;
        polyN = ps;
    }

    /**
     * Checks if the Polygon contains the point x, y
     */
    public boolean contains(int x, int y) {
        boolean oddTransitions = false;
        for (int i = 0, j = polyN - 1; i < polyN; j = i++) {
            if (((polyY[i] < y) && (polyY[j] >= y)) || ((polyY[j] < y) && (polyY[i] >= y))) {
                if ((polyX[i] + (((y - polyY[i]) / (polyY[j] - polyY[i])) * (polyX[j] - polyX[i]))) < x) {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }

}
