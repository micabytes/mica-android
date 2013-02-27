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
 * 
 * Basic polygon wrapper to handle simple point in polygon problems.
 */
public class GamePolygon {
    // Polygon coodinates.
    private int[] polyX_, polyY_;
    // Number of sides in the polygon.
    private int polyN_;

    public GamePolygon(int[] px, int[] py, int ps) {
        this.polyX_ = px;
        this.polyY_ = py;
        this.polyN_ = ps;
    }

    /** Checks if the Polygon contains the point x, y */
    public boolean contains(int x, int y) {
        boolean oddTransitions = false;
        for (int i = 0, j = this.polyN_ - 1; i < this.polyN_; j = i++) {
            if ((this.polyY_[i] < y && this.polyY_[j] >= y) || (this.polyY_[j] < y && this.polyY_[i] >= y)) {
                if (this.polyX_[i] + (y - this.polyY_[i]) / (this.polyY_[j] - this.polyY_[i]) * (this.polyX_[j] - this.polyX_[i]) < x) {
                    oddTransitions = !oddTransitions;
                }
            }
        }
        return oddTransitions;
    }

}
