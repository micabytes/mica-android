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
package com.micabytes.util

import java.util.*
import kotlin.math.floor

/** RandomHandler
 * This class provides a number of simple utilite functions for handling of randomness. It includes
 * the random number generator, a dice pool roller, and a "wild dice" pool roller.
 */
object RandomHandler {
  private val random = Random()

  fun random(): Double = random.nextDouble()

  fun random(i: Int): Int = if (i <= 0) 0 else random.nextInt(i)

  /** Roll a number of dice of a given size, with a given toHit number
   * @return Number of dice that beat (or equalled) the Hit number
   */
  fun roll(nDice: Int, dSize: Int, toHit: Int): Int {
    if (nDice == 0) return 0
    var ret = 0
    for (i in 0 until nDice) {
      val roll = random(dSize) + 1
      if (roll >= toHit) {
        ret++
      }
    }
    return ret
  }

  fun roll(nDice: Int) = roll(nDice, 10, 7)

  /** Roll a number of "wild" dice of a given size, with a given toHit number.
   * Wild dice means that he maximum roll counts as a hit and is rerolled (and can generate further
   * hits).
   * @return Number of dice that beat (or equalled) he Hit number
   */
  fun wRoll(nDice: Int, dSize: Int, toHit: Int): Int {
    if (nDice == 0) return 0
    var ret = 0
    var fail = 0
    for (i in 0 until nDice) {
      var roll = random(dSize) + 1
      if (roll == 1)
        fail++
      while (roll == dSize) { // Wild Dice
        ret++
        roll = random(dSize) + 1
      }
      if (roll >= toHit) {
        ret++
      }
    }
    return if (ret == 0 && fail >= nDice / 2) -1 else ret
  }

  fun random(min: Int, max: Int): Int {
    return min + random((max-min) + 1)
  }

}

fun randomRound(x: Float): Int {
  var ret = floor(x).toInt()
  if (x % 1 > RandomHandler.random()) ret++
  return ret
}