package com.micabytes;

@SuppressWarnings("MarkerInterface")
public interface WorldInterface {
  // This is simply a Marker interface with no methods or functions; created so that it is possible
  // to retrieve the game World object from the GameApplication without needing to put game
  // specific code into this library.
  void report(String msg, int msgTime);
}
