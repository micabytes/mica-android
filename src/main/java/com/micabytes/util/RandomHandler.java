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
package com.micabytes.util;

import java.util.Random;

/**
 * Game Utility Class
 *
 * This class provides a number of simple static utility functions. This
 * includes the RANDOM number generator, a dice roller, and a "wild dice" roller.
 */
@SuppressWarnings("UtilityClass")
public final class RandomHandler {
  // Random Generator
  private static final Random RANDOM = new Random();

  private RandomHandler() {
    // NOOP
  }

  /**
   * Returns a random double
   */
  public static double random() {
    return RANDOM.nextDouble();
  }

  /**
   * Returns a random integer between 0 and i-1 inclusive
   *
   * Returns 0 if the input is less than or equal to 0 (this would otherwise be an exception).
   */
  public static int random(int i) {
    if (i <= 0) return 0;
    return RANDOM.nextInt(i);
  }

  /**
   * Roll a number of dice of a given size, with a given toHit number
   *
   * @return Number of dice that beat (or equalled) the Hit number
   */
  public static int roll(int nDice, int dSize, int toHit) {
    if (nDice == 0) return 0;
    int ret = 0;
    for (int i = 0; i < nDice; i++) {
      int roll = random(dSize) + 1;
      if (roll >= toHit) {
        ret++;
      }
    }
    return ret;
  }

  /**
   * Roll a number of "wild" dice of a given size, with a given toHit number. Wild dice means that
   * the maximum roll counts as a hit and is rerolled (and can generate further hits).
   *
   * @return Number of dice that beat (or equalled) he Hit number
   */
  @SuppressWarnings({"MethodWithMultipleLoops", "StaticMethodOnlyUsedInOneClass"})
  public static int wRoll(int nDice, int dSize, int toHit) {
    if (nDice == 0) return 0;
    int ret = 0;
    int fail = 0;
    for (int i = 0; i < nDice; i++) {
      int roll = random(dSize) + 1;
      if (roll == 1)
        fail++;
      while (roll == dSize) { // Wild Dice
        ret++;
        roll = random(dSize) + 1;
      }
      if (roll >= toHit) {
        ret++;
      }
    }
    if ((ret == 0) && (fail >= (nDice / 2)))
      return -1;
    return ret;
  }

  public static boolean rollD100(int toHit) {
    if (random(GameConstants.HUNDRED) < toHit)
      return true;
    else
      return false;
  }
}
