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
package com.micabytes.math

/**
 * Minimal Polygon class for Android.
 *
 * Basic polygon wrapper to handle simple point in polygon
 * problems.
 */
@Suppress("unused")
class Polygon(px: IntArray, py: IntArray, private val polyN: Int) {
  private val polyX: IntArray = IntArray(px.size)
  private val polyY: IntArray = IntArray(py.size)

  init {
    System.arraycopy(px, 0, polyX, 0, px.size)
    System.arraycopy(py, 0, polyY, 0, py.size)
  }

  /**
   * Checks if the Polygon contains the point x, y
   */
  fun contains(x: Int, y: Int): Boolean {
    var oddTransitions = false
    var i = 0
    var j = polyN - 1
    while (i < polyN) {
      if (polyY[i] < y && polyY[j] >= y || polyY[j] < y && polyY[i] >= y) {
        if (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x) {
          oddTransitions = !oddTransitions
        }
      }
      j = i++
    }
    return oddTransitions
  }

}
