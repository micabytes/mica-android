package com.micabytes

interface WorldInterface {
  // This is simply a Marker interface with no methods or functions; created so that it is possible
  // to retrieve the game World object from the Game without needing to put game specific code into
  // this library.
  fun report(msg: String, msgTime: Int)
}
