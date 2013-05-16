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

import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;

import com.micabyte.android.BaseObject;
import com.micabyte.android.game.R;

/**
 * StringHandler is a wrapper around the standard Android getString functionality. It is primarily
 * used for formatting magic on strings using BaseObject code.
 * 
 * @author micabyte
 */
public class StringHandler {
	private final static int NOTFOUND = -1;
	private final static String punctuation = new String("(),;.!?");

	/**
	 * This is the workhorse function of the class. It takes a string and strips out the formatting
	 * code, replacing it with appropriate text from the variables.
	 * 
	 * Formatting code:
	 * 
	 * [/TEXT0/TEXT1/TEXT2/.../] Randomly selects one text fragment to display.
	 * 
	 * [# VARIABLE / TEXT0 / TEXT1 / TEXT2 #] Selects a text fragment based on the variable. A value
	 * of 0 selects the first variable, 1 selects the second, and any other value selects the third.
	 * This is useful to handle text where you are not sure of the plural (e.g, [# $number /no
	 * cannon/1 cannon/many cannons#]
	 * 
	 * $VARIABLE The appropriate variable is selected; integer, doubles and strings are substituted
	 * directly into the text; for BaseObject variables, the appropriate text is retrieved using the
	 * getString methods. Dot notation is used (e.g., $MyObject.MyString - MyString is passed to the
	 * getString function).
	 * 
	 * @param c Context object (usually an Application or Activity)
	 * @param text The text to be formatted
	 * @param variables A hash map containing variables
	 * @return String with all of the scripting code replaced appropriately
	 */
	public static String format(Context c, String text, HashMap<String, Object> variables) {
		String ret = text;
		int start, end;
		// Handle random choice
		start = ret.indexOf("[/");
		while (start != NOTFOUND) {
			end = ret.indexOf("/]", start);
			if (end != NOTFOUND) {
				String replace = ret.substring(start, end + 2);
				String sub = ret.substring(start + 2, end);
				String tokens[] = sub.split("[/]");
				ret = ret.replace(replace, tokens[DiceHandler.random(tokens.length)]);
				start = ret.indexOf("[/");
			}
			else
				start = NOTFOUND;
		}
		// Handle plurals
		start = ret.indexOf("[#");
		while (start != NOTFOUND) {
			end = ret.indexOf("#]", start);
			if (end == NOTFOUND) end = ret.length();
			String replace = ret.substring(start, end + 2);
			String sub = ret.substring(start + 2, end);
			String tokens[] = sub.split("[/]");
			if (tokens.length == 4) {
				String nStr = tokens[0];
				int nInt = 0;
				try {
					nInt = Integer.parseInt(nStr);
				}
				catch (NumberFormatException e) {
					if (variables != null) {
						String vars[] = nStr.split("[.]");
						Object obj = variables.get(vars[0].trim().toLowerCase(Locale.US));
						if (obj != null) {
							if (vars.length == 1) {
								if (obj instanceof Integer) {
									nInt = ((Integer) obj).intValue();
								}
								else if (obj instanceof Double) {
									nInt = ((Double) obj).intValue();
								}
								else if (obj instanceof BaseObject) {
									nInt = ((BaseObject) obj).getInteger("value");
								}
							}
							else {
								nInt = ((BaseObject) obj).getInteger(vars[1].trim().toLowerCase(Locale.US));
							}
						}
					}
				}
				if (nInt == 0) {
					ret = ret.replace(replace, tokens[1]);
				}
				else if (nInt == 1) {
					ret = ret.replace(replace, tokens[2]);
				}
				else {
					ret = ret.replace(replace, tokens[3]);
				}
			}
			else {
				ret = ret.replace(replace, "VariablePluralError:" + sub);
			}
			start = ret.indexOf("[#");
		}
		// Game variable substitution
		if (variables != null) {
			start = ret.indexOf('$');
			while (start != NOTFOUND) {
				// Regular Variable
				end = ret.indexOf(' ', start);
				if (end == NOTFOUND) end = ret.length();
				while (punctuation.indexOf(ret.charAt(end - 1)) != NOTFOUND)
					end--;
				String variable = ret.substring(start, end);
				String tokens[] = variable.split("[.]");
				if (tokens.length == 1) {
					Object obj = variables.get(tokens[0].trim().toLowerCase(Locale.US));
					if (obj != null) {
						if (obj instanceof Integer) {
							ret = ret.replace(variable, ((Integer) obj).toString());
						}
						else if (obj instanceof Double) {
							ret = ret.replace(variable, ((Double) obj).toString());
						}
						else if (obj instanceof String) {
							ret = ret.replace(variable, ((String) obj));
						}
						else if (obj instanceof BaseObject) {
							ret = ret.replace(variable, ((BaseObject) obj).getName());
						}
						else {
							ret = ret.replace(variable, "VariableTypeError:" + tokens[0].trim().toLowerCase(Locale.US));
						}
					}
					else {
						ret = ret.replace(variable, "VariableMissingError:" + tokens[0].trim().toLowerCase(Locale.US));
					}
				}
				else {
					Object obj = variables.get(tokens[0].trim().toLowerCase(Locale.US));
					if (obj != null) {
						if (obj instanceof BaseObject) {
							ret = ret.replace(variable, ((BaseObject) obj).getString(c, tokens[1].trim()));
						}
						else {
							ret = ret.replace(variable, "VariableTypeError:" + variable);
						}
					}
					else {
						ret = ret.replace(variable, "VariableMissingError:" + tokens[0].trim().toLowerCase(Locale.US));
					}
				}
				start = ret.indexOf('$');
			}
		}
		return ret;
	}

	public static String randomSplit(String str, String divisor) {
		if (str.contains(divisor)) {
			String tokens[];
			tokens = str.split("[/]");
			if (tokens.length > 0) {
				return tokens[DiceHandler.random(tokens.length)];
			}
		}
		return str;
	}

	public static String list(Context c, Vector<BaseObject> objs) {
		String ret = new String();
		if (objs.size() == 0) return ret;
		if (objs.size() == 1) {
			ret = objs.firstElement().getName();
			return ret;
		}
		if (objs.size() == 2) {
			ret = objs.get(0).getName() + c.getString(R.string.stringhandler_and1) + objs.get(1).getName();
			return ret;
		}
		for (int i = 0; i<objs.size() - 1; i++) {
			ret += objs.get(i).getName();
			if (i < objs.size() - 2)
				ret +=  c.getString(R.string.stringhandler_comma);
			else
				ret +=  c.getString(R.string.stringhandler_and2);
		}
		ret += objs.lastElement().getName();
		return ret;
	}

	public static String listString(Context c, Vector<String> objs) {
		String ret = new String();
		if (objs.size() == 0) return ret;
		if (objs.size() == 1) {
			ret = objs.firstElement();
			return ret;
		}
		if (objs.size() == 2) {
			ret = objs.get(0) + c.getString(R.string.stringhandler_and1) + objs.get(1);
			return ret;
		}
		for (int i = 0; i<objs.size() - 1; i++) {
			ret += objs.get(i);
			if (i < objs.size() - 2)
				ret +=  c.getString(R.string.stringhandler_comma);
			else
				ret +=  c.getString(R.string.stringhandler_and2);
		}
		ret += objs.lastElement();
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

}
