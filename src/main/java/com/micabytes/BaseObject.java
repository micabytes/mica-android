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
package com.micabytes;

import android.support.annotation.NonNull;

import com.micabytes.util.GameConstants;
import com.micabytes.util.GameLog;
import com.micabytes.util.StringHandler;

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
public class BaseObject {
  private static final String TAG = BaseObject.class.getName();
  protected static final BaseObject ERROR_OBJECT = new BaseObject();
  protected static final BaseObject NO_OBJECT = new BaseObject();
  private static final char VAR_CHAR = '$';
  private static final Pattern AND_SPLITTER = Pattern.compile("[&]");
  private static final Pattern GEQ_SPLITTER = Pattern.compile("[>=]+");
  private static final Pattern LEQ_SPLITTER = Pattern.compile("[<=]+");
  private static final Pattern GT_SPLITTER = Pattern.compile("[>]+");
  private static final Pattern LT_SPLITTER = Pattern.compile("[<]+");
  private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");
  private static final Pattern DOT_SPLITTER = Pattern.compile("[.]");

  @NonNls private String id = "";
  private String name = "";
  private int value;

  protected BaseObject() {
    // NOOP
  }

  protected BaseObject(String oid, String nam) {
    id = oid;
    name = nam;
  }

  protected BaseObject(String oid, String nam, int val) {
    id = oid;
    name = nam;
    value = val;
  }

  @Override
  public String toString() {
    return StringHandler.HASH_MARK + id;
  }

  public String getId() {
    return id;
  }

  public void setId(String oid) {
    id = oid;
  }

  public boolean equalsId(@NonNls String oid) {
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

  public boolean isEmpty() {
    return id.isEmpty() && name.isEmpty() && value == 0;
  }

  /*
   * Basic methods for extracting various kinds of data from a BaseObject. Used for scripting and
   * text replacement in strings (see StringHandler) where it is useful to retrieve data from many
   * different types of game objects.
   */
  private enum ValueToken {
    ERROR,
    NAME,
    VALUE,
    ME;

    public static ValueToken get(String str) {
      try {
        String str1 = str.trim();
        String str2 = str1.toUpperCase(Locale.US);
        return valueOf(str2);
      } catch (IllegalArgumentException ignored) {
        return ERROR;
      }
    }
  }

  public int getInteger(@NonNls String s) {
    if (ValueToken.get(s) == ValueToken.VALUE)
      return value;
    return Integer.MIN_VALUE;
  }

  @NonNull
  public String getString(@NonNls String s) {
    switch (ValueToken.get(s)) {
      case NAME:
        return getName();
      case VALUE:
        return Integer.toString(value);
      default:
        return GameConstants.ERROR;
    }
  }

  @NonNull
  public BaseObject getObject(@NonNls String s) {
    //ValueToken token = ValueToken.get(s);
    return GameConstants.THIS.equalsIgnoreCase(id) ? this : ERROR_OBJECT;
  }

  public static int evaluate(String test, HashMap<String, Object> variables) {
    String[] tokens = AND_SPLITTER.split(test);
    if (tokens.length == 1)
      return evaluateStatement(test, variables);
    boolean ret = true;
    for (String s : tokens) {
      if (evaluateStatement(s, variables) <= 0)
        ret = false;
    }
    return ret ? 1 : 0;
  }

  @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverlyComplexMethod", "OverlyLongMethod", "FeatureEnvy"})
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
      GameLog.e(TAG, "Could not parse statement fragment GEQ:" + str);
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
      GameLog.e(TAG, "Could not parse statement fragment LEQ:" + str);
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
      GameLog.e(TAG, "Could not parse statement fragment GT:" + str);
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
      GameLog.e(TAG, "Could not parse statement fragment LT:" + str);
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
      GameLog.e(TAG, "Could not parse statement fragment " + str);
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
        GameLog.logException(e);
        return 0;
      }
    }
    String[] tokens = DOT_SPLITTER.split(str, 2);
    if (tokens.length > 2) {
      GameLog.e(TAG, "Failed to get variable value for object " + str);
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
    if (obj instanceof String)
      return 1;
    BaseObject gObj = (BaseObject) obj;
    if (tokens.length == 1) {
      return gObj.value;
    }
    return gObj.getInteger(tokens[1]);
  }

}
