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

import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.micabyte.android.BaseObject;
import com.micabyte.android.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;

/**
 * StringHandler is a wrapper around the standard Android getString functionality. It is primarily
 * used for formatting magic on strings using BaseObject code.
 *
 * @author micabyte
 */
public class StringHandler {
    private static final int NOT_FOUND = -1;
    private static final String punctuation = "(),;.!?\"";

    /**
     * This is the workhorse function of the class. It takes a string and strips out the formatting
     * code, replacing it with appropriate text from the variables.
     * <p/>
     * Formatting code:
     * <p/>
     * [/TEXT0/TEXT1/TEXT2/.../] Randomly selects one text fragment to display.
     * <p/>
     * [# VARIABLE / TEXT0 / TEXT1 / TEXT2 #] Selects a text fragment based on the variable. A value
     * of 0 selects the first variable, 1 selects the second, and any other value selects the third.
     * This is useful to handle text where you are not sure of the plural (e.g, [# $number /no
     * cannon/1 cannon/many cannons#]
     * <p/>
     * $VARIABLE The appropriate variable is selected; integer, doubles and strings are substituted
     * directly into the text; for BaseObject variables, the appropriate text is retrieved using the
     * getString methods. Dot notation is used (e.g., $MyObject.MyString - MyString is passed to the
     * getString function).
     *
     * @param c         Context object (usually an Application or Activity)
     * @param text      The text to be formatted
     * @param variables A hash map containing variables
     * @return String with all of the scripting code replaced appropriately
     */
    @SuppressWarnings({"WeakerAccess", "InstanceofInterfaces", "ChainOfInstanceofChecks"})
    public static String format(Context c, String text, HashMap<String, Object> variables) {
        int start;
        int end;
        String ret = text;
        // Insert Line breaks
        //noinspection AccessOfSystemProperties
        ret = ret.replace("\\n", System.getProperty("line.separator"));
        // Handle random choice
        start = ret.indexOf("[/");
        while (start != NOT_FOUND) {
            end = ret.indexOf("/]", start);
            if (end != NOT_FOUND) {
                String replace = ret.substring(start, end + 2);
                String sub = ret.substring(start + 2, end);
                String[] tokens = sub.split("[/]");
                ret = ret.replace(replace, tokens[RandomHandler.random(tokens.length)]);
                start = ret.indexOf("[/");
            } else
                start = NOT_FOUND;
        }
        // Handle plurals
        start = ret.indexOf("[#");
        while (start != NOT_FOUND) {
            end = ret.indexOf("#]", start);
            if (end == NOT_FOUND) end = ret.length();
            String replace = ret.substring(start, end + 2);
            String sub = ret.substring(start + 2, end);
            String[] tokens = sub.split("[/]");
            if (tokens.length == 4) {
                String nStr = tokens[0];
                int nInt = 0;
                try {
                    nInt = Integer.parseInt(nStr.trim());
                } catch (NumberFormatException e) {
                    if (variables != null) {
                        String[] vars = nStr.split("[.]");
                        Object obj = variables.get(vars[0].trim().toLowerCase(Locale.US));
                        if (obj != null) {
                            if (vars.length == 1) {
                                if (obj instanceof Integer) {
                                    nInt = (Integer) obj;
                                } else if (obj instanceof Double) {
                                    nInt = ((Double) obj).intValue();
                                } else if (obj instanceof BaseObject) {
                                    nInt = ((BaseObject) obj).getInteger("value");
                                }
                            } else {
                                nInt = ((BaseObject) obj).getInteger(vars[1].trim().toLowerCase(Locale.US));
                            }
                        }
                    }
                }
                if (nInt == 0) {
                    ret = ret.replace(replace, tokens[1]);
                } else
                    ret = nInt == 1 ? ret.replace(replace, tokens[2]) : ret.replace(replace, tokens[3]);
            } else {
                ret = ret.replace(replace, "VariablePluralError:" + sub);
            }
            start = ret.indexOf("[#");
        }
        // Markup Link Notation
        start = ret.indexOf("[");
        while (start != NOT_FOUND) {
            end = ret.indexOf("]", start);
            if (end != NOT_FOUND) {
                String opt = ret.substring(start + 1, end);
                String condition = null;
                if (ret.charAt(end + 1) == '(') {
                    int i = ret.indexOf(")", end);
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
                    int nInt = BaseObject.evaluate(condition, variables);
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
            start = ret.indexOf('$');
            while (start != NOT_FOUND) {
                // Regular Variable
                end = ret.indexOf(' ', start);
                if (end == NOT_FOUND) end = ret.length();
                while (punctuation.indexOf(ret.charAt(end - 1)) != NOT_FOUND)
                    end--;
                String variable = ret.substring(start, end);
                String[] tokens = variable.split("[.]");
                if (tokens.length == 1) {
                    Object obj = variables.get(tokens[0].trim().toLowerCase(Locale.US));
                    if (obj != null) {
                        if (obj instanceof Integer) {
                            ret = ret.replace(variable, obj.toString());
                        } else if (obj instanceof Double) {
                            ret = ret.replace(variable, obj.toString());
                        } else if (obj instanceof String) {
                            ret = ret.replace(variable, ((String) obj));
                        } else
                            ret = obj instanceof BaseObject ? ret.replace(variable, ((BaseObject) obj).getName()) : ret.replace(variable, "VariableTypeError:" + tokens[0].trim().toLowerCase(Locale.US).replace('$', ' '));
                    } else {
                        ret = ret.replace(variable, "VariableMissingError:" + tokens[0].trim().toLowerCase(Locale.US).replace('$', ' '));
                    }
                } else {
                    Object obj = variables.get(tokens[0].trim().toLowerCase(Locale.US));
                    if (obj != null) {
                        ret = obj instanceof BaseObject ? ret.replace(variable, ((BaseObject) obj).getString(c, tokens[1].trim())) : ret.replace(variable, "VariableTypeError:" + variable);
                    } else {
                        ret = ret.replace(variable, "VariableMissingError:" + tokens[0].trim().toLowerCase(Locale.US).replace('$', ' '));
                    }
                }
                start = ret.indexOf('$');
            }
        }
        return ret;
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static String randomSplit(String str, String divisor) {
        if (str.contains(divisor)) {
            String[] tokens;
            tokens = str.split("[/]");
            if (tokens.length > 0) {
                return tokens[RandomHandler.random(tokens.length)];
            }
        }
        return str;
    }

    public static String list(Context c, ArrayList<BaseObject> list) {
        String ret = "";
        if (list.isEmpty()) return ret;
        if (list.size() == 1) {
            ret = list.get(0).getName();
            return ret;
        }
        if (list.size() == 2) {
            ret = list.get(0).getName() + " " + c.getString(R.string.stringhandler_and1) + " " + list.get(1).getName();
            return ret;
        }
        for (int i = 0; i < (list.size() - 1); i++) {
            ret += list.get(i).getName();
            if (i < (list.size() - 2)) {
                ret += c.getString(R.string.stringhandler_comma);
                ret += " ";
            } else {
                ret += c.getString(R.string.stringhandler_and2);
                ret += " ";
            }
        }
        ret += list.get(list.size()-1).getName();
        return ret;
    }

    public static String listString(Context c, ArrayList<String> list) {
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
            Crashlytics.logException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        }
        return sb.toString();
    }
}
