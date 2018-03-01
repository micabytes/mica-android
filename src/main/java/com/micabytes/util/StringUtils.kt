package com.micabytes.util

import com.micabytes.Game
import com.micabytes.R
import java.util.*

fun ArrayList<String>.toText(): String {
  val c = Game.instance
  if (isEmpty()) return ""
  if (size == 1)
    return get(0)
  if (size == 2)
    return get(0) + GameConstants.WHITESPACE + c.getString(R.string.stringhandler_and1) + GameConstants.WHITESPACE + get(1)
  val ret = StringBuilder()
  for (i in 0 until size - 1) {
    ret.append(get(i))
    if (i < size - 2) {
      ret.append(c.getString(R.string.stringhandler_comma))
      ret.append(GameConstants.WHITESPACE)
    } else {
      ret.append(c.getString(R.string.stringhandler_and2))
      ret.append(GameConstants.WHITESPACE)
    }
  }
  ret.append(get(size - 1))
  return ret.toString()
}