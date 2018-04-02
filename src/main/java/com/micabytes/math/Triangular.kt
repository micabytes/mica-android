package com.micabytes.math

object Triangular {

  fun sum(n: Int): Int {
    return n * (n + 1) / 2
  }

  fun reverse(n: Int): Int {
    return Math.floor(0.5 * Math.sqrt((8 * n + 1).toDouble()) - 0.5).toInt()
  }

  fun threshold(n: Int): Int {
    val lev = reverse(n)
    if (lev <= -8) return -3
    if (lev <= -5) return -2
    if (lev <= -2) return -1
    if (lev <= 1) return 0
    if (lev <= 4) return 1
    return if (lev <= 7) 2 else 3
  }

}
