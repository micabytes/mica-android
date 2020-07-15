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

import com.micabytes.Game
import com.micabytes.R
import com.micabytes.math.Expression
import org.jetbrains.annotations.NonNls
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.util.*
import java.util.regex.Pattern

const val AND_WS = " and "
const val OR_WS = " or "
const val TRUE_LC = "true"
const val FALSE_LC = "false"
internal const val BRACE_LEFT = '('
internal const val BRACE_RIGHT = ')'
const val CBRACE_LEFT = '{'
const val CBRACE_RIGHT = '}'
internal const val SBRACE_LEFT = '['
internal const val SBRACE_RIGHT = ']'
internal const val WHITESPACE = ' '
internal const val HEADER = '='
internal const val CHOICE_DOT = '*'
internal const val CHOICE_PLUS = '+'
internal const val COLON = ':'
internal const val COMMENT = "//"
internal const val DASH = '-'
internal const val DOT = '.'
internal const val EVENT = "event"
internal const val HASHMARK = '#'
internal const val INCLUDE = "INCLUDE"
internal const val GLUE = "<>"
internal const val DIVERT = "->"

//internal val DIVERT_END = "END"
internal const val THIS = "this"
internal const val FUNCTION = "function"
internal const val RETURN = "return"
internal const val RETURNEQ = "return ="
internal const val VAR_DECL = 'V'
internal const val VAR_STAT = '~'
internal const val NULL = "NULLs"
internal const val FALSE = "FALSE"
internal const val TRUE = "TRUE"
internal const val PI = "PI"
internal const val e = "e"

fun String.rpg(vararg args: Any): String {
  return String.format(this, args).evaluate()
}

fun String.mapVars(vars: Map<String, Any>): String {
  var ret = this
  while (ret.contains(CBRACE_LEFT)) {
    val start = ret.lastIndexOf(CBRACE_LEFT)
    val end = ret.indexOf(CBRACE_RIGHT, start)
    if (end < 0) {
      Timber.e(RuntimeException("Mismatched curly braces in text: $this"))
      return ret
    }
    val s = ret.substring(start, end + 1)
    val res = evaluateText(s, vars)
    ret = ret.replace(s, res)
  }
  return ret
}

private fun evaluateText(str: String, vars: Map<String, Any>): String {
  val s = str.replace(CBRACE_LEFT.toString(), "").replace(CBRACE_RIGHT.toString(), "")
  if (s.contains(":"))
    return evaluateConditionalText(s, vars)
  if (s.startsWith("~"))
    return evaluateShuffleText(s)
  return evaluateTextVariable(s, vars)
}

private fun evaluateConditionalText(str: String, vars: Map<String, Any>): String {
  //if (str.startsWith("when")) {
  //  return evaluateWhen(str, variables)
  //}
  if (str.startsWith("?")) {
    val condition = str.substring(1, str.indexOf(COLON)).trim({ it <= ' ' })
    val text = str.substring(str.indexOf(COLON) + 1)
    val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
    var v = 0
    try {
      val value = evaluate(condition, vars)
      v = if (value is Boolean) {
        if (value) 1 else 0
      } else if (value is BigDecimal) {
        value.toInt()
      } else {
        1
      }
    } catch (e: RuntimeException) {
      Timber.e(e)
    }

    if (v >= options.size)
      return options[options.size - 1]
    if (v < 0)
      return options[0]
    return options[v]
  }
  // Regular conditional
  val condition = str.substring(0, str.indexOf(COLON)).trim({ it <= ' ' })
  val text = str.substring(str.indexOf(COLON) + 1)
  val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  if (options.size > 2)
    Timber.e(RuntimeException("Too many options in a conditional text."))
  val ifText = options[0]
  val elseText = if (options.size == 1) "" else options[1]
  try {
    val obj = evaluate(condition, vars)
    if (obj is BigDecimal)
      return if ((obj as Number).toInt() > 0) ifText else elseText
    if (obj is Boolean)
      return if (obj) ifText else elseText
    Timber.e(RuntimeException("Condition in conditional text did not resolve into a number or boolean."))
    return elseText
  } catch (e: RuntimeException) {
    Timber.e(e)
    return elseText
  }
}

fun evaluate(str: String, vars: Map<String, Any>): Any {
  if (str.isEmpty()) return BigDecimal.ONE
  // Note that this means that spacing will mess up expressions; needs to be fixed
  var ev: String = ""
  try {
    ev = str
      .replace(AND_WS.toRegex(), " && ")
      .replace(OR_WS.toRegex(), " || ")
      .replace(TRUE_LC.toRegex(), TRUE)
      .replace(FALSE_LC.toRegex(), FALSE)
    val ex = Expression(ev)
    return ex.eval(vars)
  } catch (e: Expression.ExpressionException) {
    throw RuntimeException("Error evaluating expression " + ev + ". " + e.message, e)
  }
}

private fun evaluateTextVariable(s: String, vars: Map<String, Any>): String {
  try {
    val obj = evaluate(s, vars)
    if (obj is BigDecimal)
    // We don't want BigDecimal canonical form
      return obj.toPlainString()
    return obj.toString()
  } catch (e: RuntimeException) {
    Timber.e(e)
    return "ERROR:" + s + BRACE_RIGHT
  }
}


fun String.evaluate(): String {
  var ret = this
  while (ret.contains(CBRACE_LEFT)) {
    val start = ret.lastIndexOf(CBRACE_LEFT)
    val end = ret.indexOf(CBRACE_RIGHT, start)
    if (end < 0) {
      Timber.e(RuntimeException("Mismatched curly braces in text: $this"))
      return ret
    }
    val s = ret.substring(start, end + 1)
    val res = evaluateText(s)
    ret = ret.replace(s, res)
  }
  return ret
}

private fun evaluateText(str: String): String {
  val s = str.replace(CBRACE_LEFT.toString(), "").replace(CBRACE_RIGHT.toString(), "")
  if (s.contains(":"))
    return evaluateConditionalText(s)
  if (s.startsWith("~"))
    return evaluateShuffleText(s)
  return evaluateTextVariable(s)
}

private fun evaluateTextVariable(s: String): String {
  try {
    val obj = evaluate(s)
    if (obj is BigDecimal)
    // We don't want BigDecimal canonical form
      return obj.toPlainString()
    return obj.toString()
  } catch (e: RuntimeException) {
    Timber.e(e)
    return "ERROR:" + s + BRACE_RIGHT
  }
}

private fun evaluateShuffleText(str: String): String {
  val s = str.substring(1)
  val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  val i = Random().nextInt(tokens.size)
  return tokens[i]
}

private fun evaluateOnceOnlyText(str: String, count: Int): String {
  val s = str.substring(1)
  val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  return if (count < tokens.size) tokens[count] else ""
}

private fun evaluateCycleText(str: String, count: Int): String {
  val s = str.substring(1)
  val tokens = s.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  val i = count % tokens.size
  return tokens[i]
}

private fun evaluateConditionalText(str: String): String {
  if (str.startsWith("?")) {
    val condition = str.substring(1, str.indexOf(COLON)).trim({ it <= ' ' })
    val text = str.substring(str.indexOf(COLON) + 1)
    val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
    var v = 0
    try {
      val value = evaluate(condition)
      v = if (value is Boolean) {
        if (value) 1 else 0
      } else if (value is BigDecimal) {
        value.toInt()
      } else {
        1
      }
    } catch (e: RuntimeException) {
      Timber.e(e)
    }

    if (v >= options.size)
      return options[options.size - 1]
    if (v < 0)
      return options[0]
    return options[v]
  }
  // Regular conditional
  val condition = str.substring(0, str.indexOf(COLON)).trim({ it <= ' ' })
  val text = str.substring(str.indexOf(COLON) + 1)
  val options = text.split("[|]".toRegex()).dropLastWhile(String::isEmpty).toTypedArray()
  if (options.size > 2)
    Timber.e(RuntimeException("Too many options in a conditional text."))
  val ifText = options[0]
  val elseText = if (options.size == 1) "" else options[1]
  try {
    val obj = evaluate(condition)
    if (obj is BigDecimal)
      return if ((obj as Number).toInt() > 0) ifText else elseText
    if (obj is Boolean)
      return if (obj) ifText else elseText
    Timber.e(RuntimeException("Condition in conditional text did not resolve into a number or boolean."))
    return elseText
  } catch (e: RuntimeException) {
    Timber.e(e)
    return elseText
  }

}

fun evaluate(str: String): Any {
  if (str.isEmpty()) return BigDecimal.ONE
  //  Note that this means that spacing will mess up expressions; needs to be fixed
  var ev: String = ""
  try {
    ev = str
      .replace(AND_WS.toRegex(), " && ")
      .replace(OR_WS.toRegex(), " || ")
      .replace(TRUE_LC.toRegex(), TRUE)
      .replace(FALSE_LC.toRegex(), FALSE)
    val ex = Expression(ev)
    return ex.eval(HashMap())
  } catch (e: Expression.ExpressionException) {
    throw RuntimeException("Error evaluating expression " + ev + ". " + e.message, e)
  }
}


/* Deprecate most of this functionality. */
/**
 * StringHandler is a wrapper around the standard Android getString functionality. It is primarily
 * used for formatting magic on strings using the GameObjectInterface.
 */
object StringHandler {
  private val TAG = StringHandler::class.java.name
  private val NOT_FOUND = -1

  @NonNls
  val AT = '@'
  val HASH_MARK = '#'
  val PERCENT = '%'
  val NULL = "null"
  val PLUS = '+'
  val UNDERSCORE = '_'

  @NonNls
  val WHITESPACE = ' '

  @NonNls
  val SQUARE_BRACE_LEFT = '['
  val SQUARE_BRACE_RIGHT = ']'
  val BRACKET_LEFT = '('
  val BRACKET_RIGHT = ')'

  @NonNls
  val DOT = "."

  @NonNls
  val WAVY_BRACE_LEFT = "{"

  @NonNls
  val WAVY_BRACE_RIGHT = "}"

  @NonNls
  val LINE_SEPARATOR = "line.separator"

  @NonNls
  val EOL: String = System.getProperty(LINE_SEPARATOR)

  @NonNls
  val SLASH = "/"

  @NonNls
  val SLASH_N = "\\n"

  @NonNls
  val NUMBER_STRING = "%d"
  private val AND_SPLITTER = Pattern.compile("[&]")
  private val GEQ_SPLITTER = Pattern.compile("[>=]+")
  private val LEQ_SPLITTER = Pattern.compile("[<=]+")
  private val GT_SPLITTER = Pattern.compile("[>]+")
  private val LT_SPLITTER = Pattern.compile("[<]+")
  private val EQ_SPLITTER = Pattern.compile("[=]+")
  private val DOT_SPLITTER = Pattern.compile("[.]")
  val UNDERSCORE_SPLITTER: Pattern = Pattern.compile("[_]")

  operator fun get(id: Int): String {
    val c = Game.instance
    return c.getString(id)
  }

  operator fun get(id: Int, vararg args: Any): String {
    val c = Game.instance
    return c.getString(id, *args)
  }

  fun list(list: List<String>): String {
    val c = Game.instance
    if (list.isEmpty()) return ""
    if (list.size == 1)
      return list[0]
    if (list.size == 2)
      return list[0] + WHITESPACE + c.getString(R.string.stringhandler_and1) + WHITESPACE + list[1]
    val ret = StringBuilder()
    for (i in 0 until list.size - 1) {
      ret.append(list[i])
      if (i < list.size - 2) {
        ret.append(c.getString(R.string.stringhandler_comma))
        ret.append(WHITESPACE)
      } else {
        ret.append(c.getString(R.string.stringhandler_and2))
        ret.append(WHITESPACE)
      }
    }
    ret.append(list[list.size - 1])
    return ret.toString()
  }


  /**
   * This is the workhorse function of the class. It takes a string and strips out the formatting
   * code, replacing it with appropriate text from the variables.
   *
   * Formatting code:
   *
   *
   * [/TEXT0/TEXT1/TEXT2/.../] Randomly selects one text fragment to display.
   *
   * [# VARIABLE /
   * TEXT0 / TEXT1 / TEXT2 #] Selects a text fragment based on the variable. A value of 0 selects
   * the first variable, 1 selects the second, and any other value selects the third. This is useful
   * to handle text where you are not sure of the plural (e.g, [# $number /no cannon/1 cannon/many
   * cannons#]
   *
   * $VARIABLE The appropriate variable is selected; integer, doubles and strings are
   * substituted directly into the text; for BaseObject variables, the appropriate text is retrieved
   * using the getString methods. Dot notation is used (e.g., $MyObject.MyString - MyString is
   * passed to the getString function).
   *
   * @param text      The text to be formatted
   * @param variables A hash map containing variables
   * @return String with all of the scripting code replaced appropriately
   */
  fun format(text: String, variables: HashMap<String, Any>?): String {
    var ret = resolveLineBreaks(text)
    // Markup Link Notation
    var start = ret.indexOf(SQUARE_BRACE_LEFT)
    var end: Int
    while (start != NOT_FOUND) {
      end = ret.indexOf(SQUARE_BRACE_RIGHT, start)
      if (end == NOT_FOUND)
        start = NOT_FOUND
      else {
        val opt = ret.substring(start + 1, end)
        @NonNls var condition: String? = null
        if (ret[end + 1] == BRACKET_LEFT) {
          val i = ret.indexOf(BRACKET_RIGHT, end)
          condition = ret.substring(end + 2, i).trim { it <= ' ' }
          if (i != NOT_FOUND) end = i
        }
        val replace = ret.substring(start, end + 1)
        val tokens = opt.split("[|]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (tokens.size == 1)
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.size)])
        else if ("?" == condition) {
          ret = ret.replace(replace, tokens[RandomHandler.random(tokens.size)])
        } else if (condition != null) {
          condition = condition.replace("?", "")
          var nInt = evaluate(condition, variables)
          if (nInt > tokens.size - 1) nInt = tokens.size - 1
          if (nInt < 0) nInt = 0
          ret = ret.replace(replace, tokens[nInt])
        }
        start = ret.indexOf(SQUARE_BRACE_LEFT)
      }
    }
    // Game variable substitution
    if (variables != null) {
      var startV = ret.indexOf(WAVY_BRACE_LEFT)
      while (startV != NOT_FOUND) {
        end = ret.indexOf(WAVY_BRACE_RIGHT, startV)
        if (end == NOT_FOUND)
          startV = NOT_FOUND
        else {
          val variable = ret.substring(startV + 1, end)
          val stringToReplace = ret.substring(startV, end + 1)
          val replaceWith = getStringValue(variable, variables)
          ret = ret.replace(stringToReplace, replaceWith)
          startV = ret.indexOf(WAVY_BRACE_LEFT)
        }
      }
    }
    return ret
  }

  private fun resolveLineBreaks(text: String): String {

    return text.replace(SLASH_N, System.getProperty(LINE_SEPARATOR)!!)
  }

  private fun getStringValue(key: String, variables: AbstractMap<String, Any>?): String {
    if (variables == null) {
      return GameConstants.ERROR
    }
    val str = key.trim { it <= ' ' }.toLowerCase(Locale.US)
    if (str.isEmpty())
      return GameConstants.ERROR
    val tokens = DOT_SPLITTER.split(str, 2)
    if (tokens.size > 2) {
      Timber.e(TAG, "Failed to getStringValue variable value for object $str")
      return GameConstants.ERROR
    }
    val obj = variables[tokens[0]] ?: return GameConstants.ERROR
    if (obj is String)
      return obj
    return if (tokens.size == 1) obj.toString() else ""
    //GameObjectInterface sObj = (GameObjectInterface) obj;
    //Object res = sObj.getAttribute(tokens[1]);
    //if (res instanceof String) return (String) res;
    //return res.toString();
  }

  fun randomSplit(str: String, divisor: String): String {
    if (str.contains(divisor)) {
      val tokens = str.split((SQUARE_BRACE_LEFT + divisor + SQUARE_BRACE_RIGHT).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      if (tokens.size > 0) {
        return tokens[RandomHandler.random(tokens.size)]
      }
    }
    return str
  }

  fun listString(list: ArrayList<String>): String {
    val c = Game.instance
    @NonNls var ret = ""
    if (list.isEmpty()) return ret
    if (list.size == 1) {
      ret = list[0]
      return ret
    }
    if (list.size == 2) {
      ret = list[0] + c.getString(R.string.stringhandler_and1) + list[1]
      return ret
    }
    for (i in 0 until list.size - 1) {
      ret += list[i]
      ret += if (i < list.size - 2) c.getString(R.string.stringhandler_comma) else c.getString(R.string.stringhandler_and2)
    }
    ret += list[list.size - 1]
    return ret
  }

  fun signedString(i: Int): String {
    return if (i < 0) Integer.toString(i) else PLUS + Integer.toString(i)
  }

  operator fun get(id: Int, variables: HashMap<String, Any>, vararg args: Any): String {
    val c = Game.instance
    return format(c.getString(id, *args), variables)
  }

  fun getString(`is`: InputStream): String {
    var reader: BufferedReader? = null
    val sb = StringBuilder()
    try {
      //noinspection IOResourceOpenedButNotSafelyClosed,resource
      reader = BufferedReader(InputStreamReader(`is`, GameConstants.UTF_8))
      var line = reader.readLine()
      while (line != null) {
        sb.append(line).append(EOL)
        line = reader.readLine()
      }
    } catch (e: IOException) {
      Timber.e(e)
    } finally {
      try {
        reader?.close()
      } catch (e: IOException) {
        Timber.e(e)
      }

      try {
        `is`.close()
      } catch (e: IOException) {
        Timber.e(e)
      }

    }
    return sb.toString()
  }

  private fun evaluate(test: String, variables: HashMap<String, Any>?): Int {
    val tokens = AND_SPLITTER.split(test)
    if (tokens.size == 1)
      return evaluateStatement(test, variables)
    var ret = true
    for (s in tokens) {
      if (evaluateStatement(s, variables) <= 0)
        ret = false
    }
    return if (ret) 1 else 0
  }

  private fun evaluateStatement(str: String, variables: AbstractMap<String, Any>?): Int {
    val tokens: Array<String>
    // Random Value
    // >=
    if (str.contains(">=")) {
      tokens = GEQ_SPLITTER.split(str)
      if (tokens.size == 2) {
        val val1 = tokens[0].trim { it <= ' ' }.toLowerCase(Locale.US)
        val val2 = tokens[1].trim { it <= ' ' }.toLowerCase(Locale.US)
        return if (getVariableValue(val1, variables) >= getVariableValue(val2, variables)) 1 else 0
      }
      Timber.e(TAG, "Could not parse statement fragment GEQ:$str")
      return 0
    }
    // >=
    if (str.contains("<=")) {
      tokens = LEQ_SPLITTER.split(str)
      if (tokens.size == 2) {
        val val1 = tokens[0].trim { it <= ' ' }.toLowerCase(Locale.US)
        val val2 = tokens[1].trim { it <= ' ' }.toLowerCase(Locale.US)
        return if (getVariableValue(val1, variables) <= getVariableValue(val2, variables)) 1 else 0
      }
      Timber.e(TAG, "Could not parse statement fragment LEQ:$str")
      return 0
    }
    // >
    if (str.contains(">")) {
      tokens = GT_SPLITTER.split(str)
      if (tokens.size == 2) {
        val val1 = tokens[0].trim { it <= ' ' }.toLowerCase(Locale.US)
        val val2 = tokens[1].trim { it <= ' ' }.toLowerCase(Locale.US)
        return if (getVariableValue(val1, variables) > getVariableValue(val2, variables)) 1 else 0
      }
      Timber.e(TAG, "Could not parse statement fragment GT:$str")
      return 0
    }
    // <
    if (str.contains("<")) {
      tokens = LT_SPLITTER.split(str)
      if (tokens.size == 2) {
        val val1 = tokens[0].trim { it <= ' ' }.toLowerCase(Locale.US)
        val val2 = tokens[1].trim { it <= ' ' }.toLowerCase(Locale.US)
        return if (getVariableValue(val1, variables) < getVariableValue(val2, variables)) 1 else 0
      }
      Timber.e(TAG, "Could not parse statement fragment LT:$str")
      return 0
    }
    // Set Last, as it will otherwise take precedence over all the others.
    // =
    if (str.contains("=")) {
      tokens = EQ_SPLITTER.split(str)
      if (tokens.size == 2) {
        val val1 = tokens[0].trim { it <= ' ' }.toLowerCase(Locale.US)
        val val2 = tokens[1].trim { it <= ' ' }.toLowerCase(Locale.US)
        return if (getVariableValue(val1, variables) == getVariableValue(val2, variables)) 1 else 0
      }
      Timber.e(TAG, "Could not parse statement fragment $str")
      return 0
    }
    // Retrieve
    return getVariableValue(str, variables)
  }

  private fun getVariableValue(key: String, variables: AbstractMap<String, Any>?): Int {
    val str = key.trim { it <= ' ' }.toLowerCase(Locale.US)
    if (str.isEmpty()) return 0
    try {
      return Integer.parseInt(str)
    } catch (ignored: NumberFormatException) {
      // NOOP
    }

    val tokens = DOT_SPLITTER.split(str, 2)
    if (tokens.size > 2) {
      Timber.e(TAG, "Failed to getVariableValue for object $str")
      return 0
    }
    if (variables == null)
      return 0
    val obj = variables[tokens[0]] ?: return 0
    if (obj is Boolean) {
      return if (obj) 1 else 0
    }
    if (obj is Int)
      return obj
    if (obj is Double)
      return obj.toInt()
    return if (obj is String) 1 else 0
    //GameObjectInterface gObj = (GameObjectInterface) obj;
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
  }

}
