package com.micabytes.util

interface RpgUnit {
  val id: String
  val name: String
  val description: String
  val typeId: String
  val value: Int
  val size: Int
  //val groupSize: Int
  val capacity: Int
  val wealth: Int
  fun increaseWealth(n: Int)
  fun decreaseWealth(n: Int)
  fun hasItem(it: String): Boolean
  val factionId : String
}