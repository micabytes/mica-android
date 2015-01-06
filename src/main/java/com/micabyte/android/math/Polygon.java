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
    private final int[] polyX_;
    private final int[] polyY_;
    // Number of sides in the polygon.
    private final int polyN_;

    public Polygon(int[] px, int[] py, int ps) {
        polyX_ = px;
        polyY_ = py;
        polyN_ = ps;
    }

    /**
     * Checks if the Polygon contains the point x, y
     */
    public boolean contains(int x, int y) {
        boolean oddTransitions = false;
        for (int i = 0, j = polyN_ - 1; i < polyN_; j = i++) {
            if ((polyY_[i] < y && polyY_[j] >= y) || (polyY_[j] < y && polyY_[i] >= y)) {
                if (polyX_[i] + (y - polyY_[i]) / (polyY_[j] - polyY_[i]) * (polyX_[j] - polyX_[i]) < x) {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }

}
