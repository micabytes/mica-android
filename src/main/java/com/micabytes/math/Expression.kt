package com.micabytes.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.*

/**
 * A one-class expression evaluator
 * Originally based on https://github.com/uklimaschewski/EvalEx
 * Creates a new expression instance from an expression string with a given default match context
 * @param originalExpression The expression. E.g. `"2.4*sin(3)/(2-4)"` or `"sin(y)>0 & max(z, 3)>3"`
 * @param defaultMathContext The [MathContext] to use by default
 */
class Expression constructor(originalExpression: String,
                             private val defaultMathContext: MathContext = MathContext.DECIMAL32) {
  /**
   * Abstract definition of a supported operator. An operator is defined by its name (pattern), precedence and if it is
   * left- or right associative.
   */
  abstract class Operator(val oper: String, val precedence: Int, val isLeftAssoc: Boolean) {
    abstract fun eval(v1: Any, v2: Any): Any
  }

  /// The expression evaluators exception class.
  class ExpressionException(message: String) : RuntimeException(message)

  /// The [MathContext] to use for calculations.
  private var mc = defaultMathContext
  /// The cached RPN (Reverse Polish Notation) of the expression.
  private var rpn: List<String>? = null
  ///The characters (other than letters and digits) allowed as the first character in a variable.
  private val firstVarChars: String = "_"
  /// The characters (other than letters and digits) allowed as the second or subsequent characters in a variable.
  private val varChars: String = "_."
  /// All defined operators with name and implementation.
  private val operators = TreeMap<String, Operator>(String.CASE_INSENSITIVE_ORDER)
  /// The current infix expression, with optional variable substitutions.
  private var expression: String = originalExpression.trim { it <= ' ' }

  /**
   * Expression tokenizer that allows to iterate over a [String] expression token by token. Blank characters will be
   * skipped.
   */
  private inner class Tokenizer(input: String) : Iterator<String> {
    var pos = 0
      private set
    private val input: String = input.trim { it <= ' ' }
    private var previousToken: String? = null

    override fun hasNext(): Boolean {
      return pos < input.length
    }

    private fun peekNextChar(): Char {
      if (pos < input.length - 1) {
        return input[pos + 1]
      } else {
        return 0.toChar()
      }
    }

    override fun next(): String {
      val token = StringBuilder()
      if (pos >= input.length) {
        previousToken = null
        return ""
      }
      var ch = input[pos]
      while (Character.isWhitespace(ch) && pos < input.length) {
        ch = input[++pos]
      }
      if (Character.isDigit(ch)) {
        while ((Character.isDigit(ch) || ch == decimalSeparator
            || ch == 'e' || ch == 'E'
            || ch == minusSign && token.isNotEmpty()
            && ('e' == token[token.length - 1] || 'E' == token[token.length - 1])
            || ch == '+' && token.isNotEmpty()
            && ('e' == token[token.length - 1] || 'E' == token[token.length - 1])) && pos < input.length) {
          token.append(input[pos++])
          ch = if (pos == input.length) '0' else input[pos]
        }
      } else if (ch == minusSign
          && Character.isDigit(peekNextChar())
          && ("(" == previousToken
          || "," == previousToken
          || previousToken == null
          || operators.containsKey(previousToken!!))) {
        token.append(minusSign)
        pos++
        token.append(next())
      } else if (Character.isLetter(ch) || firstVarChars.indexOf(ch) >= 0 || ch == '_' ) { // || ch == '\"'
        while ((Character.isLetter(ch)
            || Character.isDigit(ch)
            || varChars.indexOf(ch) >= 0
            || ch == '_' || ch == '.' || ch == '\"'
            //|| token.isEmpty() && firstVarChars.indexOf(ch) >= 0
            ) && pos < input.length) {
          token.append(input[pos++])
          ch = if (pos == input.length) '0' else input[pos]
        }
      }
      else if (ch == '\"') {
        token.append(input[pos++])
        ch = if (pos == input.length) '0' else input[pos]
        while (ch != '\"' && pos < input.length) {
          token.append(ch)
          pos++
          ch = if (pos == input.length) '0' else input[pos]
        }
        if (ch == '\"' && pos < input.length) {
          token.append(ch)
          pos++
        }
      }
      else if (ch == '(' || ch == ')' || ch == ',') {
        token.append(ch)
        pos++
      } else {
        while (!Character.isLetter(ch) && !Character.isDigit(ch)
            && firstVarChars.indexOf(ch) < 0 && !Character.isWhitespace(ch)
            && ch != '(' && ch != ')' && ch != ','
            && pos < input.length) {
          token.append(input[pos])
          pos++
          ch = if (pos == input.length) '0' else input[pos]
          if (ch == minusSign) {
            break
          }
        }
        if (!operators.containsKey(token.toString())) {
          throw ExpressionException("Unknown operator '$token' at position " + (pos - token.length + 1))
        }
      }
      previousToken = token.toString()
      return previousToken as String
    }

  }

  init {
    this.mc = defaultMathContext
    this.expression = originalExpression
    addOperator(object : Operator("+", 20, true) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> v1.add(v2 as BigDecimal, mc)
          is String -> stripStringParameter(v1) + stripStringParameter(v2.toString())
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("-", 20, true) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> v1.subtract(v2 as BigDecimal, mc)
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("*", 30, true) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> v1.multiply(v2 as BigDecimal, mc)
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("/", 30, true) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> v1.divide(v2 as BigDecimal, mc)
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("%", 30, true) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> v1.remainder(v2 as BigDecimal, mc)
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("^", 40, false) {
      override fun eval(v1: Any, v2: Any): Any {
        if (!(v1 is BigDecimal && v2 is BigDecimal)) return BigDecimal.ZERO
        var v2m = v2
        val signOf2 = v2m.signum()
        val dn1 = v1.toDouble()
        v2m = v2m.multiply(BigDecimal(signOf2)) // n2 is now positive
        val remainderOf2 = v2m.remainder(BigDecimal.ONE)
        val n2IntPart = v2m.subtract(remainderOf2)
        val intPow = v1.pow(n2IntPart.intValueExact(), mc)
        val doublePow = BigDecimal(Math.pow(dn1,
            remainderOf2.toDouble()))
        var result = intPow.multiply(doublePow, mc)
        if (signOf2 == -1) {
          result = BigDecimal.ONE.divide(result, mc.precision,
              RoundingMode.HALF_UP)
        }
        return result
      }
    })
    addOperator(object : Operator("&&", 4, false) {
      override fun eval(v1: Any, v2: Any): Any {
        val b1 = v1 != BigDecimal.ZERO
        val b2 = v2 != BigDecimal.ZERO
        return if (b1 && b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("||", 2, false) {
      override fun eval(v1: Any, v2: Any): Any {
        val b1 = v1 != BigDecimal.ZERO
        val b2 = v2 != BigDecimal.ZERO
        return if (b1 || b2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator(">", 10, false) {
      override fun eval(v1: Any, v2: Any): Any {
        if (!(v1 is BigDecimal && v2 is BigDecimal)) return BigDecimal.ZERO
        return if (v1.compareTo(v2) == 1) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator(">=", 10, false) {
      override fun eval(v1: Any, v2: Any): Any {
        if (!(v1 is BigDecimal && v2 is BigDecimal)) return BigDecimal.ZERO
        return if (v1 >= v2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("<", 10, false) {
      override fun eval(v1: Any, v2: Any): Any {
        if (!(v1 is BigDecimal && v2 is BigDecimal)) return BigDecimal.ZERO
        return if (v1.compareTo(v2) == -1) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("<=", 10, false) {
      override fun eval(v1: Any, v2: Any): Any {
        if (!(v1 is BigDecimal && v2 is BigDecimal)) return BigDecimal.ZERO
        return if (v1 <= v2) BigDecimal.ONE else BigDecimal.ZERO
      }
    })
    addOperator(object : Operator("==", 7, false) {
      override fun eval(v1: Any, v2: Any): Any {
        return operators["="]!!.eval(v1, v2)
      }
    })
    addOperator(object : Operator("=", 7, false) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> if (v1.compareTo(v2 as BigDecimal) == 0) BigDecimal.ONE else BigDecimal.ZERO
          is String -> if (stripStringParameter(v1).compareTo(stripStringParameter(v2 as String)) == 0) BigDecimal.ONE else BigDecimal.ZERO
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("!=", 7, false) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v1) {
          is BigDecimal -> if (v1.compareTo(v2 as BigDecimal) != 0) BigDecimal.ONE else BigDecimal.ZERO
          is String -> if (stripStringParameter(v1).compareTo(stripStringParameter(v2 as String)) != 0) BigDecimal.ONE else BigDecimal.ZERO
          else -> BigDecimal.ZERO
        }
      }
    })
    addOperator(object : Operator("<>", 7, false) {
      override fun eval(v1: Any, v2: Any): Any {
        return operators["!="]!!.eval(v1, v2)
      }
    })
    addOperator(object : Operator("?:", 40, false) {
      override fun eval(v1: Any, v2: Any): Any {
        return when (v2) {
          is BigDecimal -> if (v1 is BigDecimal && v1 == BigDecimal.ZERO) return v2 else v1
          is String -> if (v1 is BigDecimal && v1 == BigDecimal.ZERO) return stripStringParameter(v2) else v1
          else -> if (v1 is BigDecimal && v1 == BigDecimal.ZERO) return v2 else v1
        }
      }
    })
    /*
    addFunction(object : Function("NOT", 1) {
      override fun eval(parameters: List<Any>): Any {
        val param = parameters[0]
        if (param is Boolean)
          return if (param) BigDecimal.ZERO else BigDecimal.ONE
        if (param is BigDecimal) {
          val zero = param.compareTo(BigDecimal.ZERO) == 0
          return if (zero) BigDecimal.ONE else BigDecimal.ZERO
        }
        return BigDecimal.ZERO
      }
    })
    addLazyFunction(object : LazyFunction("IF", 3) {
      override fun lazyEval(lazyParams: List<LazyNumber>): LazyNumber {
        val isTrue = lazyParams[0].eval() != BigDecimal.ZERO
        return if (isTrue) lazyParams[1] else lazyParams[2]
      }
    })
    addFunction(object : Function("RANDOM", 0) {
      override fun eval(parameters: List<Any>): Any {
        val d = Math.random()
        return BigDecimal(d, mc)
      }
    })
    addFunction(object : Function("MAX", -1) {
      override fun eval(parameters: List<Any>): Any {
        if (parameters.isEmpty()) {
          throw ExpressionException("MAX requires at least one parameter")
        }
        var max: BigDecimal? = null
        for (parameter in parameters) {
          when (parameter) {
            is BigDecimal -> {
              if (max == null || parameter > max) {
                max = parameter
              }
            }
            else -> {
              throw ExpressionException("MAX requires all parameters to be BigDecimal")
            }
          }
        }
        return max!!
      }
    })
    addFunction(object : Function("MIN", -1) {
      override fun eval(parameters: List<Any>): Any {
        if (parameters.isEmpty()) {
          throw ExpressionException("MIN requires at least one parameter")
        }
        var min: BigDecimal? = null
        for (parameter in parameters) {
          when (parameter) {
            is BigDecimal -> {
              if (min == null || parameter > min) {
                min = parameter
              }
            }
            else -> {
              throw ExpressionException("MIN requires all parameters to be BigDecimal")
            }
          }
        }
        return min!!
      }
    })
    addFunction(object : Function("ABS", 1) {
      override fun eval(parameters: List<Any>): Any {
        return ((parameters[0] as BigDecimal).abs(mc) as BigDecimal)
      }
    })
    addFunction(object : Function("ROUND", 2) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        val precision = (parameters[1] as BigDecimal).toInt()
        return toRound.setScale(precision, mc!!.roundingMode)
      }
    })
    addFunction(object : Function("FLOOR", 1) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        return toRound.setScale(0, RoundingMode.FLOOR)
      }
    })
    addFunction(object : Function("CEIL", 1) {
      override fun eval(parameters: List<Any>): Any {
        val toRound = parameters[0] as BigDecimal
        return toRound.setScale(0, RoundingMode.CEILING)
      }
    })
    */
    //values.put("e", e)
    //values.put("PI", PI)
    //values.put("TRUE", BigDecimal.ONE)
    //values.put("FALSE", BigDecimal.ZERO)
  }

  private fun shuntingYard(expression: String, vars: Map<String, Any>): List<String> {
    val outputQueue = ArrayList<String>()
    val stack = Stack<String>()
    val tokenizer = Tokenizer(expression)
    var lastFunction: String? = null
    var previousToken: String? = null
    while (tokenizer.hasNext()) {
      val token = tokenizer.next()
      if (isNumber(token)) {
        outputQueue.add(token)
      } else if (isStringParameter(token)) {
        outputQueue.add(token)
      } else if (vars.containsKey(token)) {
        outputQueue.add(token)
      } else if (Character.isLetter(token[0])) {
        stack.push(token)
      } else if ("," == token) {
        if (operators.containsKey(previousToken)) {
          throw ExpressionException("Missing parameter(s) for operator " + previousToken + " at character position " + (tokenizer.pos - 1 - previousToken!!.length))
        }
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw ExpressionException("Parse error for function '$lastFunction'")
        }
      } else if (operators.containsKey(token)) {
        if ("," == previousToken || "(" == previousToken) {
          throw ExpressionException("Missing parameter(s) for operator " + token + " at character position " + (tokenizer.pos - token.length))
        }
        val o1: Operator = operators[token]!!
        var token2: String? = if (stack.isEmpty()) null else stack.peek()
        while (token2 != null
            && operators.containsKey(token2)
            && (o1.isLeftAssoc && o1.precedence <= operators[token2]!!.precedence || o1.precedence < operators[token2]!!.precedence)) {
          outputQueue.add(stack.pop())
          token2 = if (stack.isEmpty()) null else stack.peek()
        }
        stack.push(token)
      } else if ("(" == token) {
        if (previousToken != null) {
          if (isNumber(previousToken)) {
            throw ExpressionException("Missing operator at character position " + tokenizer.pos)
          }
        }
        stack.push(token)
      } else if (")" == token) {
        if (operators.containsKey(previousToken)) {
          throw ExpressionException("Missing parameter(s) for operator " + previousToken + " at character position " + (tokenizer.pos - 1 - previousToken!!.length))
        }
        while (!stack.isEmpty() && "(" != stack.peek()) {
          outputQueue.add(stack.pop())
        }
        if (stack.isEmpty()) {
          throw ExpressionException("Mismatched parentheses")
        }
        stack.pop()
      }
      previousToken = token
    }
    while (!stack.isEmpty()) {
      val element = stack.pop()
      if ("(" == element || ")" == element) {
        throw ExpressionException("Mismatched parentheses")
      }
      if (!operators.containsKey(element)) {
        throw ExpressionException("Unknown operator or function: $element")
      }
      outputQueue.add(element)
    }
    return outputQueue
  }

  fun eval(vars: Map<String, Any>): Any {
    val stack = Stack<Any>()
    for (token in getRPN(vars)) {
      if (isNumber(token))
        stack.push(BigDecimal(token, mc))
      else if (operators.containsKey(token)) {
        val v1 = stack.pop()
        val v2 = stack.pop()
        stack.push(operators[token]!!.eval(v2, v1))
        //val number = object : LazyNumber {
        //  override fun eval(): BigDecimal {
        //    return operators[token]!!.eval(v2.eval(), v1.eval())
        //  }
        //}
        //stack.push(number)
      } else if (vars.containsKey(token)) {
        val obj = vars[token]
        when (obj) {
          is BigDecimal -> {
            stack.push(obj.round(mc))
          }
          else -> {
            stack.push(obj)
          }
        }
      } else if ("(" == token) {
        stack.push(PARAMS_START)
      } else {
        stack.push(token)
        //stack.push(object : LazyNumber {
        //  override fun eval(): BigDecimal {
        //    return BigDecimal(token, mc!!)
        //  }
        //})
      }
    }
    val obj: Any? = stack.pop()
    when (obj) {
      is BigDecimal ->
        return obj.stripTrailingZeros()
      is String ->
        if (isStringParameter(obj)) return stripStringParameter(obj)
    }
    return obj ?: 0
  }

  @Suppress("unused")
  fun setPrecision(precision: Int): Expression {
    this.mc = MathContext(precision)
    return this
  }

  @Suppress("unused")
  fun setRoundingMode(roundingMode: RoundingMode): Expression {
    this.mc = MathContext(mc.precision, roundingMode)
    return this
  }

  private fun addOperator(oper: Operator) {
    operators[oper.oper] = oper
  }

  private fun getRPN(vars: Map<String, Any>): List<String> {
    if (rpn == null) {
      rpn = shuntingYard(expression, vars)
      validate(rpn!!, vars)
    }
    return rpn!!
  }

  private fun validate(rpn: List<String>, vars: Map<String, Any>) {
    val stack = Stack<Int>()
    stack.push(0)
    for (token in rpn) {
      if (operators.containsKey(token)) {
        if (stack.peek() < 2) {
          throw ExpressionException("Missing parameter(s) for operator $token")
        }
        // pop the operator's 2 parameters and add the result
        stack[stack.size - 1] = stack.peek() - 2 + 1
      } else if (vars.containsKey(token)) {
        stack[stack.size - 1] = stack.peek() + 1
      } else if ("(" == token) {
        stack.push(0)
      } else {
        stack[stack.size - 1] = stack.peek() + 1
      }
    }
    if (stack.size > 1) {
      throw ExpressionException("Too many unhandled function parameter lists")
    } else if (stack.peek() > 1) {
      throw ExpressionException("Too many numbers or values")
    } else if (stack.peek() < 1) {
      throw ExpressionException("Empty expression")
    }
  }

  override fun toString(): String {
    return expression
  }

  interface LazyNumber {
    fun eval(): Any
  }

  companion object {
    /// Definition of PI as a constant, can be used in expressions as variable.
    val PI = BigDecimal("3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")
    /// Definition of e: "Euler's number" as a constant, can be used in expressions as variable.
    val e = BigDecimal("2.71828182845904523536028747135266249775724709369995957496696762772407663")
    /// What character to use for decimal separators.
    private const val decimalSeparator = '.'
    /// What character to use for minus sign (negative values).
    private const val minusSign = '-'
    /// The BigDecimal representation of the left parenthesis, used for parsing varying numbers of function parameters.
    private val PARAMS_START = object : LazyNumber {
      override fun eval(): BigDecimal {
        return BigDecimal(0)
      }
    }

    fun isNumber(st: String): Boolean {
      if (st[0] == minusSign && st.length == 1) return false
      if (st[0] == '+' && st.length == 1) return false
      if (st[0] == 'e' || st[0] == 'E') return false
      return st.toCharArray().none { !Character.isDigit(it) && it != minusSign && it != decimalSeparator && it != 'e' && it != 'E' && it != '+' }
    }

    private fun isStringParameter(st: String): Boolean {
      if (st.startsWith("\"") && st.endsWith("\""))
        return true
      return false
    }

    private fun stripStringParameter(st: String): String {
      if (st.startsWith("\"") && st.endsWith("\""))
        return st.substring(1, st.length - 1)
      if (st.startsWith("\'") && st.endsWith("\'"))
        return st.substring(1, st.length - 1)
      return st
    }

  }

}