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
package com.micabyte.android;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.micabyte.android.util.StringHandler;

import org.jetbrains.annotations.NonNls;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * BaseObject is a generic Object that contains a number of frequently used attributes.
 *
 * @author micabyte
 */
@SuppressWarnings("HardCodedStringLiteral")
public class BaseObject {
    private static final String TAG = BaseObject.class.getName();
    private static final char VAR_CHAR = '$';
    private static final Pattern AND_SPLITTER = Pattern.compile("[&]");
    private static final Pattern GEQ_SPLITTER = Pattern.compile("[>=]+");
    private static final Pattern LEQ_SPLITTER = Pattern.compile("[<=]+");
    private static final Pattern GT_SPLITTER = Pattern.compile("[>]+");
    private static final Pattern LT_SPLITTER = Pattern.compile("[<]+");
    private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");
    private static final Pattern DOT_SPLITTER = Pattern.compile("[.]");
    // ID of Object
    @NonNls
    private String id = "";
    // Name of Object
    private String name = "";
    // Value of Object
    private int value;

    protected BaseObject() {
        // NOOP
    }

    protected BaseObject(String oid, String nam, int val) {
        id = oid;
        name = nam;
        value = val;
    }

    public String getId() {
        return id;
    }

    public void setId(String oid) {
        id = oid;
    }

    public boolean equalsId(String oid) {
        return id.equalsIgnoreCase(oid);
    }

    public String getName() {
        return name;
    }

    public void setName(String nam) {
        name = nam;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int val) {
        value = val;
    }

    /*
     * Basic methods for extracting various kinds of data from a BaseObject. Used for scripting and
     * text replacement in strings (see StringHandler) where it is useful to retrieve data from many
     * different types of game objects.
     */
    private enum ValueToken {
        ERROR, NAME, VALUE, THIS;

        public static ValueToken get(String str) {
            try {
                return valueOf(str.trim().toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                Crashlytics.logException(e);
                return ERROR;
            }
        }
    }

    public int getInteger(String str) {
        if (ValueToken.get(str) == ValueToken.VALUE)
            return value;
        return 0;
    }

    @NonNull
    public String getString(Context context, String str) {
        switch (ValueToken.get(str)) {
            case NAME:
                return getName();
            case VALUE:
                return Integer.toString(value);
            default:
                return StringHandler.get(context, R.string.default_error);
        }
    }

    @NonNull
    public BaseObject getObject(String str) {
        ValueToken token = ValueToken.get(str);
        return (token == ValueToken.THIS) ? this : new BaseObject();
    }

    public static int evaluate(String test, HashMap<String, Object> variables) {
        String[] tokens = AND_SPLITTER.split(test);
        if (tokens.length == 1)
            return evaluateStatement(test, variables);
        boolean ret = true;
        for (String s : tokens) {
            if (evaluateStatement(s, variables) <= 0) {
                ret = false;
                Log.d(TAG, "Evaluate of " + s + " is false");
            } else {
                Log.d(TAG, "Evaluate of " + s + " is true");
            }
        }
        return ret ? 1 : 0;
    }

    @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverlyComplexMethod"})
    private static int evaluateStatement(String str, AbstractMap<String, Object> variables) {
        String[] tokens;
        // Random Value
        // >=
        if (str.contains(">=")) {
            tokens = GEQ_SPLITTER.split(str);
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1, variables) >= getVariableValue(val2, variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // >=
        if (str.contains("<=")) {
            tokens = LEQ_SPLITTER.split(str);
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1, variables) <= getVariableValue(val2, variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // >
        if (str.contains(">")) {
            tokens = GT_SPLITTER.split(str);
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1, variables) > getVariableValue(val2, variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // <
        if (str.contains("<")) {
            tokens = LT_SPLITTER.split(str);
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1, variables) < getVariableValue(val2, variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // Set Last, as it will otherwise take precedence over all the others.
        // =
        if (str.contains("=")) {
            tokens = EQ_SPLITTER.split(str);
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1, variables) == getVariableValue(val2, variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // Retrieve
        return getVariableValue(str, variables);
    }

    @SuppressWarnings({"ChainOfInstanceofChecks", "MethodWithMultipleReturnPoints", "OverlyComplexMethod"})
    private static int getVariableValue(String key, AbstractMap<String, Object> variables) {
        String str = key.trim().toLowerCase(Locale.US);
        if (str.isEmpty()) return 0;
        if (str.charAt(0) != VAR_CHAR) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                Crashlytics.logException(e);
                return 0;
            }
        }
        String[] tokens = DOT_SPLITTER.split(str, 2);
        if (tokens.length > 2) {
            Crashlytics.log(Log.ERROR, TAG, "Failed to interpret object " + str);
            return 0;
        }
        if (variables == null)
            return 0;
        Object obj = variables.get(tokens[0]);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj ? 1 : 0;
        }
        if (obj instanceof Integer)
            return (Integer) obj;
        if (obj instanceof Double)
            return ((Double) obj).intValue();
        BaseObject gObj = (BaseObject) obj;
        if (tokens.length == 1) {
            return gObj.value;
        }
        return gObj.getInteger(tokens[1]);
    }

}
