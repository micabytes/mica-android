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

import android.content.Context;
import android.support.annotation.Nullable;

import com.micabytes.R;

import org.jetbrains.annotations.NonNls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * StringHandler is a wrapper around the standard Android getString functionality. It is primarily
 * used for formatting magic on strings using BaseObject code.
 *
 * @author micabyte
 */
@SuppressWarnings("ALL")
public class StringHandler {
  private static final String TAG = StringHandler.class.getName();
  private static final int NOT_FOUND = -1;
  private static final String punctuation = "(),;.!?\"";
  @NonNls public static final String WHITESPACE = " ";
  public static final char CHAR_WHITESPACE = ' ';
  public static final char HASH_MARK = '#';
  public static final char PERCENT = '%';
  @NonNls
  public static final char EOL = '\n';
  private static final char VAR_CHAR = '$';
  private static final Pattern AND_SPLITTER = Pattern.compile("[&]");
  private static final Pattern GEQ_SPLITTER = Pattern.compile("[>=]+");
  private static final Pattern LEQ_SPLITTER = Pattern.compile("[<=]+");
  private static final Pattern GT_SPLITTER = Pattern.compile("[>]+");
  private static final Pattern LT_SPLITTER = Pattern.compile("[<]+");
  private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");
  private static final Pattern DOT_SPLITTER = Pattern.compile("[.]");

  /**
   * This is the workhorse function of the class. It takes a string and strips out the formatting
   * code, replacing it with appropriate text from the variables. <p/> Formatting code: <p/>
   * [/TEXT0/TEXT1/TEXT2/.../] Randomly selects one text fragment to display. <p/> [# VARIABLE /
   * TEXT0 / TEXT1 / TEXT2 #] Selects a text fragment based on the variable. A value of 0 selects
   * the first variable, 1 selects the second, and any other value selects the third. This is useful
   * to handle text where you are not sure of the plural (e.g, [# $number /no cannon/1 cannon/many
   * cannons#] <p/> $VARIABLE The appropriate variable is selected; integer, doubles and strings are
   * substituted directly into the text; for BaseObject variables, the appropriate text is retrieved
   * using the getString methods. Dot notation is used (e.g., $MyObject.MyString - MyString is
   * passed to the getString function).
   *
   * @param c         Context object (usually an Application or Activity)
   * @param text      The text to be formatted
   * @param variables A hash map containing variables
   * @return String with all of the scripting code replaced appropriately
   */
  public static String format(Context c, String text, @Nullable HashMap<String, Object> variables) {
    String ret = resolveLineBreaks(text);
    // Markup Link Notation
    int start;
    int end;
    start = ret.indexOf('[');
    while (start != NOT_FOUND) {
      end = ret.indexOf(']', start);
      if (end != NOT_FOUND) {
        String opt = ret.substring(start + 1, end);
        String condition = null;
        if (ret.charAt(end + 1) == '(') {
          int i = ret.indexOf(')', end);
          condition = ret.substring(end + 2, i).trim();
          if (i != NOT_FOUND) end = i;
        }
        String replace = ret.substring(start, end + 1);
        String[] tokens = opt.split("[|]");
        if (tokens.length == 1)
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.length)]);
        else if ("?".equals(condition)) {
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.length)]);
        } else {
          condition = condition.replace("?", "");
          int nInt = evaluate(condition, variables);
          if (nInt > (tokens.length - 1)) nInt = tokens.length - 1;
          if (nInt < 0) nInt = 0;
          ret = ret.replace(replace, tokens[nInt]);
        }
        start = ret.indexOf("[");
      } else
        start = NOT_FOUND;
    }
    // Game variable substitution
    if (variables != null) {
      start = ret.indexOf('{');
      while (start != NOT_FOUND) {
        end = ret.indexOf('}', start);
        if (end != NOT_FOUND) {
          String variable = ret.substring(start + 1, end);
          String stringToReplace = ret.substring(start, end + 1);
          ret.replace(stringToReplace, getStringValue(variable, variables));
          start = ret.indexOf('{');
        } else
          start = NOT_FOUND;
      }
    }
    return ret;
  }

  private static String resolveLineBreaks(String text) {
    return text.replace("\\n", System.getProperty("line.separator"));
  }

  private static String getStringValue(String key, AbstractMap<String, Object> variables) {
    if (variables == null) {
      return GameConstants.ERROR;
    }
    String str = key.trim().toLowerCase(Locale.US);
    if (str.isEmpty())
      return GameConstants.ERROR;
    String[] tokens = DOT_SPLITTER.split(str, 2);
    if (tokens.length > 2) {
      GameLog.e(TAG, "Failed to get variable value for object " + str);
      return GameConstants.ERROR;
    }
    Object obj = variables.get(tokens[0]);
    if (obj == null) {
      return GameConstants.ERROR;
    }
    if (tokens.length == 1)
      return obj.toString();
    ObjectAttributes sObj = (ObjectAttributes) obj;
    return sObj.getString(tokens[1]);
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static String randomSplit(String str, String divisor) {
    if (str.contains(divisor)) {
      String[] tokens;
      tokens = str.split("[" + divisor + "]");
      if (tokens.length > 0) {
        return tokens[RandomHandler.random(tokens.length)];
      }
    }
    return str;
  }

  public static String list(Context c, ArrayList<String> list) {
    if (list.isEmpty()) return "";
    if (list.size() == 1) {
      return list.get(0);
    }
    if (list.size() == 2) {
      return new StringBuilder(list.get(0)).append(StringHandler.WHITESPACE).append(c.getString(R.string.stringhandler_and1)).append(' ').append(list.get(1)).toString();
    }
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < (list.size() - 1); i++) {
      ret.append(list.get(i));
      if (i < (list.size() - 2)) {
        ret.append(c.getString(R.string.stringhandler_comma));
        ret.append(' ');
      } else {
        ret.append(c.getString(R.string.stringhandler_and2));
        ret.append(' ');
      }
    }
    ret.append(list.get(list.size() - 1));
    return ret.toString();
  }

  public static String listString(Context context, ArrayList<String> list) {
    String ret = "";
    if (list.isEmpty()) return ret;
    if (list.size() == 1) {
      ret = list.get(0);
      return ret;
    }
    if (list.size() == 2) {
      ret = list.get(0) + context.getString(R.string.stringhandler_and1) + list.get(1);
      return ret;
    }
    for (int i = 0; i < (list.size() - 1); i++) {
      ret += list.get(i);
      ret += i < (list.size() - 2) ? context.getString(R.string.stringhandler_comma) : context.getString(R.string.stringhandler_and2);
    }
    ret += list.get(list.size() - 1);
    return ret;
  }

  public static String signedString(int i) {
    if (i < 0) return Integer.toString(i);
    return "+" + Integer.toString(i);
  }

  public static String get(Context c, int id) {
    return format(c, c.getString(id), null);
  }

  public static String get(Context c, int id, Object... args) {
    return format(c, c.getString(id, args), null);
  }

  public static String get(Context c, int id, HashMap<String, Object> variables, Object... args) {
    return format(c, c.getString(id, args), variables);
  }

  public static String getString(InputStream is) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();

    String line;
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line).append("\n");
      }
    } catch (IOException e) {
      GameLog.logException(e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        GameLog.logException(e);
      }
    }
    return sb.toString();
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
    ObjectAttributes gObj = (ObjectAttributes) obj;
    //if (tokens.length == 1) {
    //  return gObj.getValue();
    //}
    return gObj.getInteger(tokens[1]);
  }
}
