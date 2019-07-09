package com.micabytes

import com.micabytes.util.RpgUnit

interface WorldInterface {
  // This is simply a Marker interface with no methods or functions; created so that it is possible
  // to retrieve the game World object from the Game without needing to put game specific code into
  // this library.

  abstract fun get(id: String): RpgUnit?

  fun showMessage(msg: String, msgTime: Int)
}
