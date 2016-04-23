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

import com.micabytes.GameApplication;
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
 * used for formatting magic on strings using the AttributeInterface.
 */
@SuppressWarnings({"OverlyComplexClass", "UtilityClass", "unused"})
public final class StringHandler {
  private static final String TAG = StringHandler.class.getName();
  private static final int NOT_FOUND = -1;
  public static final char AT = '@';
  public static final char HASH_MARK = '#';
  public static final char PERCENT = '%';
  public static final String NULL = "null";
  public static final char PLUS = '+';
  public static final char UNDERSCORE = '_';
  @NonNls public static final char WHITESPACE = ' ';
  public static final char SQUARE_BRACE_LEFT = '[';
  public static final char SQUARE_BRACE_RIGHT = ']';
  public static final char BRACKET_LEFT = '(';
  public static final char BRACKET_RIGHT = ')';
  public static final char WAVY_BRACE_LEFT = '{';
  public static final char WAVY_BRACE_RIGHT = '}';
  @NonNls public static final String LINE_SEPARATOR = "line.separator";
  @NonNls public static final String EOL = System.getProperty(LINE_SEPARATOR);
  @SuppressWarnings("HardcodedLineSeparator")
  @NonNls public static final String SLASH_N = "\\n";
  @NonNls public static final String NUMBER_STRING = "%d";
  private static final Pattern AND_SPLITTER = Pattern.compile("[&]");
  private static final Pattern GEQ_SPLITTER = Pattern.compile("[>=]+");
  private static final Pattern LEQ_SPLITTER = Pattern.compile("[<=]+");
  private static final Pattern GT_SPLITTER = Pattern.compile("[>]+");
  private static final Pattern LT_SPLITTER = Pattern.compile("[<]+");
  private static final Pattern EQ_SPLITTER = Pattern.compile("[=]+");
  private static final Pattern DOT_SPLITTER = Pattern.compile("[.]");

  private StringHandler() {
    // NOOP
  }

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
   * @param text      The text to be formatted
   * @param variables A hash map containing variables
   * @return String with all of the scripting code replaced appropriately
   */
  @SuppressWarnings({"MethodWithMoreThanThreeNegations", "OverlyComplexMethod"})
  public static String format(String text, @Nullable HashMap<String, Object> variables) {
    String ret = resolveLineBreaks(text);
    // Markup Link Notation
    int start = ret.indexOf(SQUARE_BRACE_LEFT);
    int end;
    while (start != NOT_FOUND) {
      end = ret.indexOf(SQUARE_BRACE_RIGHT, start);
      if (end == NOT_FOUND) start = NOT_FOUND;
      else {
        String opt = ret.substring(start + 1, end);
        @NonNls String condition = null;
        if (ret.charAt(end + 1) == BRACKET_LEFT) {
          int i = ret.indexOf(BRACKET_RIGHT, end);
          condition = ret.substring(end + 2, i).trim();
          if (i != NOT_FOUND) end = i;
        }
        String replace = ret.substring(start, end + 1);
        String[] tokens = opt.split("[|]");
        if (tokens.length == 1)
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.length)]);
        else if ("?".equals(condition)) {
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.length)]);
        } else if (condition != null){
          condition = condition.replace("?", "");
          int nInt = evaluate(condition, variables);
          if (nInt > (tokens.length - 1)) nInt = tokens.length - 1;
          if (nInt < 0) nInt = 0;
          ret = ret.replace(replace, tokens[nInt]);
        }
        start = ret.indexOf(SQUARE_BRACE_LEFT);
      }
    }
    // Game variable substitution
    if (variables != null) {
      int startV = ret.indexOf(WAVY_BRACE_LEFT);
      while (startV != NOT_FOUND) {
        end = ret.indexOf(WAVY_BRACE_RIGHT, startV);
        if (end == NOT_FOUND) startV = NOT_FOUND;
        else {
          String variable = ret.substring(startV + 1, end);
          String stringToReplace = ret.substring(startV, end + 1);
          String replaceWith = getStringValue(variable, variables);
          ret = ret.replace(stringToReplace, replaceWith);
          startV = ret.indexOf(WAVY_BRACE_LEFT);
        }
      }
    }
    return ret;
  }

  private static String resolveLineBreaks(String text) {
    return text.replace(SLASH_N, System.getProperty(LINE_SEPARATOR));
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
      GameLog.e(TAG, "Failed to getStringValue variable value for object " + str);
      return GameConstants.ERROR;
    }
    Object obj = variables.get(tokens[0]);
    if (obj == null) {
      return GameConstants.ERROR;
    }
    if (obj instanceof  String)
      return (String) obj;
    if (tokens.length == 1)
      return obj.toString();
    AttributeInterface sObj = (AttributeInterface) obj;
    //Object res = sObj.getAttribute(tokens[1]);
    //if (res instanceof String) return (String) res;
    //return res.toString();
    return "";
  }

  @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
  public static String randomSplit(String str, String divisor) {
    if (str.contains(divisor)) {
      String[] tokens = str.split(SQUARE_BRACE_LEFT + divisor + SQUARE_BRACE_RIGHT);
      if (tokens.length > 0) {
        return tokens[RandomHandler.random(tokens.length)];
      }
    }
    return str;
  }

  public static String list(ArrayList<String> list) {
    Context c = GameApplication.getInstance();
    if (list.isEmpty()) return "";
    if (list.size() == 1) {
      return list.get(0);
    }
    if (list.size() == 2)
      return list.get(0) + WHITESPACE + c.getString(R.string.stringhandler_and1) + WHITESPACE + list.get(1);
    StringBuilder ret = new StringBuilder();
    for (int i = 0; i < (list.size() - 1); i++) {
      ret.append(list.get(i));
      if (i < (list.size() - 2)) {
        ret.append(c.getString(R.string.stringhandler_comma));
        ret.append(WHITESPACE);
      } else {
        ret.append(c.getString(R.string.stringhandler_and2));
        ret.append(WHITESPACE);
      }
    }
    ret.append(list.get(list.size() - 1));
    return ret.toString();
  }

  @SuppressWarnings("StringContatenationInLoop")
  public static String listString(ArrayList<String> list) {
    Context c = GameApplication.getInstance();
    String ret = "";
    if (list.isEmpty()) return ret;
    if (list.size() == 1) {
      ret = list.get(0);
      return ret;
    }
    if (list.size() == 2) {
      ret = list.get(0) + c.getString(R.string.stringhandler_and1) + list.get(1);
      return ret;
    }
    for (int i = 0; i < (list.size() - 1); i++) {
      ret += list.get(i);
      ret += i < (list.size() - 2) ? c.getString(R.string.stringhandler_comma) : c.getString(R.string.stringhandler_and2);
    }
    ret += list.get(list.size() - 1);
    return ret;
  }

  public static String signedString(int i) {
    if (i < 0) return Integer.toString(i);
    return PLUS + Integer.toString(i);
  }

  public static String get(int id) {
    Context c = GameApplication.getInstance();
    return format(c.getString(id), null);
  }

  public static String get(int id, Object... args) {
    Context c = GameApplication.getInstance();
    return format(c.getString(id, args), null);
  }

  public static String get(int id, HashMap<String, Object> variables, Object... args) {
    Context c = GameApplication.getInstance();
    return format(c.getString(id, args), variables);
  }

  public static String getString(InputStream is) {
    BufferedReader reader = null;
    StringBuilder sb = new StringBuilder();
    try {
      //noinspection IOResourceOpenedButNotSafelyClosed
      reader = new  BufferedReader(new InputStreamReader(is, GameConstants.UTF_8));
      String line;
      //noinspection NestedAssignment
      while ((line = reader.readLine()) != null) {
        sb.append(line).append(EOL);
      }
    } catch (IOException e) {
      GameLog.logException(e);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        GameLog.logException(e);
      }
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

  @SuppressWarnings({"MethodWithMultipleReturnPoints", "OverlyComplexMethod", "FeatureEnvy"})
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
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException ignored) {
      // NOOP
    }
    String[] tokens = DOT_SPLITTER.split(str, 2);
    if (tokens.length > 2) {
      GameLog.e(TAG, "Failed to getVariableValue for object " + str);
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
    AttributeInterface gObj = (AttributeInterface) obj;
    /*Object attribute = gObj.getAttribute(tokens[1]);
    if (attribute instanceof Boolean) {
      return (Boolean) attribute ? 1 : 0;
    }
    if (attribute instanceof Integer)
      return (Integer) attribute;
    if (attribute instanceof Double)
      return ((Double) attribute).intValue();
    if (attribute instanceof String)
      return 1;
      */
    return 0;
  }




}
