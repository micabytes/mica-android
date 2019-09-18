package com.micabytes.util

interface RpgUnit {
  val id: String
  val name: String
  val description: String
  val typeId: String
  val value: Int
  val size: Int
  val capacity: Int
  val wealth: Int
  fun increaseWealth(n: Int)
  fun decreaseWealth(n: Int)
  fun hasItem(itm: String): Boolean
  //val groupSize: Int
  //val factionId : String
}