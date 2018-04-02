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
package com.micabytes.media

import android.media.MediaPlayer
import android.util.SparseArray
import com.micabytes.Game

/**
 * MusicHandler is a simple, dumb wrapper around the Android MediaPlayer. It plays music files
 * placed in the raw resource directory.
 *
 * General usage: Start it in the onResume of an
 * activity, and stop the music in the onPause. release should be called in onDestroy of the root
 * activity.
 */
object MusicHandler {
  private const val INVALID_NUMBER = 0
  private const val VOLUME_MAX = 1.0f
  private const val DEFAULT_VOLUME = 0.25f
  private val PLAYERS = SparseArray<MediaPlayer>()
  private var volume = DEFAULT_VOLUME
  var currentMusic = INVALID_NUMBER
    private set
  private var nextMusic = INVALID_NUMBER
  private var pausedMusic = INVALID_NUMBER
  private var mutedMusic = false

  /**
   * Start playing a music resource
   * @param music   The resource id of the music file
   * @param forced  Force the player to the front
   */
  @JvmOverloads
  fun start(music: Int, forced: Boolean = false) {
    if (currentMusic == music) {
      // already playing this music
      return
    }
    if (shouldQueue(forced)) {
      queueMusic(music)
      return
    }
    if (currentMusic != INVALID_NUMBER) {
      // playing some other music, pause it and change
      pause()
    }
    currentMusic = music
    val mp = PLAYERS.get(music)
    if (mp != null) {
      if (!mp.isPlaying) {
        // Note: This continues the piece where it last let off
        mp.isLooping = true
        mp.setOnCompletionListener(null)
        mp.start()
      }
    } else {
      val context = Game.instance
      val mediaPlayer = MediaPlayer.create(context, music)
      if (mediaPlayer != null) {
        mediaPlayer.setVolume(if (mutedMusic) 0.0f else volume, if (mutedMusic) 0.0f else volume)
        PLAYERS.put(music, mediaPlayer)
        mediaPlayer.isLooping = true
        mediaPlayer.setOnCompletionListener(null)
        mediaPlayer.start()
      }
    }
  }

  // already playing some music and not forced to change immediately
  private fun queueMusic(music: Int) {
    if (music != INVALID_NUMBER) {
      nextMusic = music
      val mp = PLAYERS.get(music)
      if (mp == null) {
        val mediaPlayer = MediaPlayer.create(Game.instance, music)
        if (mediaPlayer != null)
          PLAYERS.put(music, mediaPlayer)
      }
      val cp = PLAYERS.get(currentMusic)
      if (cp != null) {
        cp.setOnCompletionListener { next() }
        cp.isLooping = false
      }
    }
  }

  private fun shouldQueue(forced: Boolean): Boolean {
    return !forced && currentMusic != INVALID_NUMBER
  }

  /**
   * Pause all media PLAYERS
   */
  fun pause() {
    for (i in 0 until PLAYERS.size()) {
      val p = PLAYERS.valueAt(i)
      if (p != null && p.isPlaying) {
        p.pause()
      }
    }
    pausedMusic = currentMusic
    currentMusic = INVALID_NUMBER
  }

  /**
   * Pause all media players
   */
  fun resume() {
    start(pausedMusic)
    pausedMusic = INVALID_NUMBER
  }

  /**
   * Stop all media players
   */
  private fun stop() {
    for (i in 0 until PLAYERS.size()) {
      val p = PLAYERS.valueAt(i)
      if (p.isPlaying) {
        p.stop()
      }
    }
    currentMusic = INVALID_NUMBER
  }

  /**
   * Advance to the next resource
   */
  private operator fun next() {
    if (nextMusic == INVALID_NUMBER) {
      return
    }
    currentMusic = nextMusic
    nextMusic = INVALID_NUMBER
    val p = PLAYERS.get(currentMusic)
    if (p != null) {
      if (!p.isPlaying) {
        p.isLooping = true
        p.setOnCompletionListener(null)
        p.setVolume(if (mutedMusic) 0.0f else volume, if (mutedMusic) 0.0f else volume)
        p.start()
      }
    }
  }

  /**
   * Release the media players.
   */
  fun release() {
    for (i in 0 until PLAYERS.size()) {
      val p = PLAYERS.valueAt(i)
      if (p.isPlaying) {
        p.stop()
      }
      p.release()
    }
    PLAYERS.clear()
    currentMusic = INVALID_NUMBER
  }

  fun setVolume(v: Float) {
    volume = if (v >= VOLUME_MAX) VOLUME_MAX else v
    if (mutedMusic) return
    for (i in 0 until PLAYERS.size()) {
      val p = PLAYERS.valueAt(i)
      p.setVolume(volume, volume)
    }
  }

  fun mute(isChecked: Boolean) {
    mutedMusic = isChecked
    if (mutedMusic) {
      for (i in 0 until PLAYERS.size()) {
        val p = PLAYERS.valueAt(i)
        p.setVolume(0.0f, 0.0f)
      }
    } else {
      for (i in 0 until PLAYERS.size()) {
        val p = PLAYERS.valueAt(i)
        p.setVolume(volume, volume)
      }
    }
  }

}