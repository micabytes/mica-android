package com.micabytes.map

import java.lang.Math.abs
import java.lang.Math.round
import com.google.android.gms.common.util.Hex

fun cubeToOddR(cube: Triple<Int, Int, Int>): Pair<Int, Int> {
  val col = cube.first + (cube.third - (abs(cube.third) % 2)) / 2
  val row = cube.third
  return Pair(col, row)
}

fun oddRToCube(hex: Pair<Int, Int>): Triple<Int, Int, Int> {
  val x = hex.first - (hex.second - (abs(hex.second) % 2)) / 2
  val z = hex.second
  val y = -x - z
  return Triple(x, y, z)
}

fun cube_round(cube: Triple<Double, Double, Double>): Triple<Int, Int, Int> {
  var rx = round(cube.first)
  var ry = round(cube.second)
  var rz = round(cube.third)

  val x_diff = abs(rx - cube.first)
  val y_diff = abs(ry - cube.second)
  val z_diff = abs(rz - cube.third)

  if (x_diff > y_diff && x_diff > z_diff)
    rx = -ry - rz
  else if (y_diff > z_diff)
    ry = -rx - rz
  else
    rz = -rx - ry

  return Triple(rx.toInt(), ry.toInt(), rz.toInt())
}

fun lerp(a: Double, b: Double, t: Double): Double {
  return (a * (1 - t) + b * t)
}

/*
fun hex_lerp(a: Triple<Int, Int, Int>, b: Triple<Int, Int, Int>, t: Double): Triple<Double, Double, Double> {
  return Triple(
    lerp(a.first.toDouble(), b.first.toDouble(), t),
    lerp(a.second.toDouble(), b.second.toDouble(), t),
    lerp(a.third.toDouble(), b.third.toDouble(), t)
  )
}
*/

fun hex_lerp(a: Triple<Double, Double, Double>, b: Triple<Double, Double, Double>, t: Double): Triple<Double, Double, Double> {
  return Triple(
    lerp(a.first, b.first, t),
    lerp(a.second, b.second, t),
    lerp(a.third, b.third, t)
  )
}
