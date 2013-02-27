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
package com.micabyte.android.util;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Random;

/**
 * Game Utility Class
 * 
 * This class provides a number of simple static utility functions. This includes the random number
 * generator, a dice roller, and a "wild dice" roller.
 * 
 * @author micabyte 
 */
public class DiceHandler {
    // Random Generator
    private static Random random = null;
    // Dice Rolling
    public static final int WDICE_DEFAULT_HIT = 4;
    public static final int BASE_DICE_THRESHOLD = 4;
    // Date Formater
    private static SimpleDateFormat dateParser = null;

    /** Returns a random double */
    public static double random() {
        if (random == null) random = new Random();
        return random.nextDouble();
    }

    /**
     * Returns a random integer between 0 and i-1 inclusive
     * 
     * Returns 0 if the input is less than or equal to 0 (this would otherwise be an error).
     */
    public static int random(int i) {
        if (i <= 0) return 0;
        if (random == null) random = new Random();
        return random.nextInt(i);
    }

    /**
     * Roll a number of dice of a given size, with a given toHit number
     * 
     * @return Number of dice that beat the Hit number
     */
    public static int roll(int nDice, int dSize, int toHit) {
        if (nDice == 0) return 0;
        int ret = 0;
        int roll;
        for (int i = 0; i < nDice; i++) {
            roll = DiceHandler.random(dSize) + 1;
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
     * @return Number of dice that beat the Hit number
     */
    public static int wRoll(int nDice, int dSize, int toHit) {
        if (nDice == 0) return 0;
        int ret = 0;
        int roll;
        for (int i = 0; i < nDice; i++) {
            roll = DiceHandler.random(dSize) + 1;
            while (roll == dSize) { // Wild Dice
                ret++;
                roll = DiceHandler.random(dSize) + 1;
            }
            if (roll >= toHit) {
                ret++;
            }
        }
        return ret;
    }
    
    /** Returns a default simple date parser */
    public static SimpleDateFormat dateParser() {
        if (dateParser == null) dateParser = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        return dateParser;
    }

}
