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

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.micabyte.android.util.StringHandler;

/**
 * BaseObject is a generic Object that contains a number of frequently used attributes.
 *
 * @author micabyte
 */
public abstract class BaseObject {
    private static final String TAG = BaseObject.class.getName();
    // ID of Object
    private String id_ = null;
    // Name of Object
    private String name_ = null;
    // Value of Object
    private int value_ = 0;

    protected BaseObject() {
        // NOOP
    }

    protected BaseObject(String id, String name, int v) {
        this.id_ = id;
        this.name_ = name;
        this.value_ = v;
    }

    public String getId() {
        return this.id_;
    }

    public void setId(String s) {
        this.id_ = s;
    }

    public boolean equalsId(String id) {
        return this.id_.equalsIgnoreCase(id);
    }

    public String getName() {
        return this.name_;
    }

    public void setName(String name) {
        this.name_ = name;
    }

    public int getValue() {
        return this.value_;
    }

    public void setValue(int v) {
        this.value_ = v;
    }

    /*
     * Basic methods for extracting various kinds of data from a BaseObject. Used for scripting and
     * text replacement in strings (see StringHandler) where it is useful to retrieve data from many
     * different types of game objects.
     */
    private enum ValueToken {
        error, name, value;

        public static ValueToken get(String str) {
            try {
                return valueOf(str.trim().toLowerCase(Locale.US));
            } catch (Exception ex) {
                return error;
            }
        }
    }

    public boolean getBoolean(String id) {
        switch (ValueToken.get(id)) {
            case value:
                return getValue() > 0;
            default:
                return false;
        }
    }

    public int getInteger(String id) {
        switch (ValueToken.get(id)) {
            case value:
                return getValue();
            default:
                return 0;
        }
    }

    public String getString(Context c, String id) {
        switch (ValueToken.get(id)) {
            case name:
                return getName();
            case value:
                return Integer.toString(getValue());
            default:
                return StringHandler.get(c, R.string.default_error);
        }
    }

    @SuppressWarnings({"static-method", "UnusedParameters"})
    public BaseObject getObject(String id) {
        return null;
    }

    public static int evaluate(String test, HashMap<String, Object> variables) {
        String tokens[];
        tokens = test.split("[&]");
        if (tokens.length == 1)
            return evaluateStatement(test, variables);
        boolean ret = true;
        for (String s : tokens) {
            Log.d("TAG", "Evaluate of " + s);
            if (evaluateStatement(s, variables) <= 0) {
                ret = false;
                Log.d(TAG, "False");
            }
            else {
                Log.d(TAG, "True");
            }
        }
        return ret ? 1 : 0;
    }

    private static int evaluateStatement(String str, HashMap<String, Object> variables) {
        String tokens[];
        // Random Value
        // >=
        if (str.contains(">=")) {
            tokens = str.split("[>=]+");
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1,variables) >= getVariableValue(val2,variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // >=
        if (str.contains("<=")) {
            tokens = str.split("[<=]+");
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1,variables) <= getVariableValue(val2,variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " +  str);
            return 0;
        }
        // >
        if (str.contains(">")) {
            tokens = str.split("[>]+");
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1,variables) > getVariableValue(val2,variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " +  str);
            return 0;
        }
        // <
        if (str.contains("<")) {
            tokens = str.split("[<]+");
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1,variables) < getVariableValue(val2,variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " + str);
            return 0;
        }
        // Set Last, as it will otherwise take precedence over all the others.
        // =
        if (str.contains("=")) {
            tokens = str.split("[=]+");
            if (tokens.length == 2) {
                String val1 = tokens[0].trim().toLowerCase(Locale.US);
                String val2 = tokens[1].trim().toLowerCase(Locale.US);
                return (getVariableValue(val1,variables) == getVariableValue(val2,variables)) ? 1 : 0;
            }
            Crashlytics.log(Log.ERROR, TAG, "Could not parse statement fragment " +  str);
            return 0;
        }
        // Retrieve
        return getVariableValue(str, variables);
    }

    private static int getVariableValue(String key, HashMap<String, Object> variables) {
        String var = key.trim().toLowerCase(Locale.US);
        if (var.isEmpty()) return 0;
        if (var.charAt(0) != '$') {
            try {
                return Integer.parseInt(var);
            } catch (NumberFormatException e) {
                Crashlytics.log(Log.ERROR, TAG, var + " is not a valid variable");
                return 0;
            }
        }
        String tokens[] = var.split("[.]", 2);
        if (tokens.length > 2) {
            Crashlytics.log(Log.ERROR, TAG, "Failed to interpret object " + var);
            return 0;
        }
        if (variables == null)
            return 0;
        Object obj = variables.get(tokens[0]);
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Boolean) {
            if (((Boolean) obj) == true)
                return 1;
            else
                return 0;
        }
        if (obj instanceof Integer)
            return (Integer) obj;
        if (obj instanceof Double)
            return ((Double) obj).intValue();
        BaseObject gObj = (BaseObject) obj;
        if (tokens.length == 1) {
            return gObj.getValue();
        }
        return gObj.getInteger(tokens[1]);
    }

}
