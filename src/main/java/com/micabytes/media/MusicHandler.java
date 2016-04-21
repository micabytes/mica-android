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
package com.micabytes.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.SparseArray;

import com.micabytes.GameApplication;
import com.micabytes.util.GameLog;

/**
 * MusicHandler is a simple, dumb wrapper around the Android MediaPlayer. It plays music files
 * placed in the raw resource directory. <p/> General usage: Start it in the onResume of an
 * activity, and stop the music in the onPause. release should be called in onDestroy of the root
 * activity.
 */
@SuppressWarnings("UtilityClass")
public final class MusicHandler {
  private static final String TAG = MusicHandler.class.getName();
  private static final int INVALID_NUMBER = 0;
  private static final SparseArray<MediaPlayer> PLAYERS = new SparseArray<>();
  private static final float VOLUME_MAX = 1.0f;
  private static float volume = 0.25f;
  private static int currentMusic = INVALID_NUMBER;
  private static int nextMusic = INVALID_NUMBER;
  private static int pausedMusic = INVALID_NUMBER;
  private static boolean mutedMusic = false;

  private MusicHandler() {
    // NOOP
  }

  /**
   * Start playing a music resource
   *
   * @param music   The resource id of the music file
   */
  public static void start(int music) {
    start(music, false);
  }

  /**
   * Start playing a music resource
   *
   * @param context The context (application or activity)
   * @param music   The resource id of the music file
   */
  public static void start(int music, boolean forced) {
    if (currentMusic == music) {
      // already playing this music
      return;
    }
    if (shouldQueue(forced)) {
      queueMusic(music);
      return;
    }
    if (currentMusic != INVALID_NUMBER) {
      // playing some other music, pause it and change
      pause();
    }
    currentMusic = music;
    MediaPlayer mp = PLAYERS.get(music);
    if (mp != null) {
      if (!mp.isPlaying()) {
        // Note: This continues the piece where it last let off
        mp.setLooping(true);
        mp.setOnCompletionListener(null);
        mp.start();
      }
    } else {
      Context context = GameApplication.getInstance();
      MediaPlayer mediaPlayer = MediaPlayer.create(context, music);
      mediaPlayer.setVolume(mutedMusic ? 0f : volume, mutedMusic ? 0f : volume);
      PLAYERS.put(music, mediaPlayer);
      if (mediaPlayer == null) {
        // Log an ERROR, but don't do anything (we do not want to risk f/context the app)
        GameLog.e(TAG, "player was not created successfully");
      } else {
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnCompletionListener(null);
        mediaPlayer.start();
      }
    }
  }

  // already playing some music and not forced to change immediately
  private static void queueMusic(int music) {
    if (music != INVALID_NUMBER) {
      nextMusic = music;
      MediaPlayer mp = PLAYERS.get(music);
      if (mp == null) {
        Context context = GameApplication.getInstance();
        PLAYERS.put(music, MediaPlayer.create(context, music));
      }
      MediaPlayer cp = PLAYERS.get(currentMusic);
      if (cp != null) {
        cp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mp) {
            next();
          }
        });
        cp.setLooping(false);
      }
    }
  }

  public static boolean shouldQueue(boolean forced) {
    return !forced && currentMusic != INVALID_NUMBER;
  }

  /**
   * Pause all media PLAYERS
   */
  public static void pause() {
    for (int i = 0; i < PLAYERS.size(); i++) {
      MediaPlayer p = PLAYERS.valueAt(i);
      if (p.isPlaying()) {
        p.pause();
      }
    }
    pausedMusic = currentMusic;
    currentMusic = INVALID_NUMBER;
  }

  /**
   * Pause all media PLAYERS
   */
  public static void resume() {
    start(pausedMusic);
    pausedMusic = INVALID_NUMBER;
  }

  /**
   * Resume media PLAYERS
   */
  private static void stop() {
    for (int i = 0; i < PLAYERS.size(); i++) {
      MediaPlayer p = PLAYERS.valueAt(i);
      if (p.isPlaying()) {
        p.stop();
      }
    }
    currentMusic = INVALID_NUMBER;
  }

  /**
   * Advance to the next resource
   */
  private static void next() {
    if (nextMusic == INVALID_NUMBER) {
      return;
    }
    currentMusic = nextMusic;
    nextMusic = INVALID_NUMBER;
    MediaPlayer p = PLAYERS.get(currentMusic);
    if (p != null) {
      if (!p.isPlaying()) {
        p.setLooping(true);
        p.setOnCompletionListener(null);
        p.setVolume(mutedMusic ? 0f : volume, mutedMusic ? 0f : volume);
        p.start();
      }
    }
  }

  /**
   * Release the media PLAYERS.
   */
  public static void release() {
    for (int i = 0; i < PLAYERS.size(); i++) {
      MediaPlayer p = PLAYERS.valueAt(i);
      if (p.isPlaying()) {
        p.stop();
      }
      p.release();
    }
    PLAYERS.clear();
    currentMusic = INVALID_NUMBER;
  }

  public static void setVolume(float v) {
    volume = v >= VOLUME_MAX ? VOLUME_MAX : v;
    if (mutedMusic) return;
    for (int i = 0; i < PLAYERS.size(); i++) {
      MediaPlayer p = PLAYERS.valueAt(i);
      p.setVolume(volume, volume);
    }
  }

  public static void mute(boolean isChecked) {
    mutedMusic = isChecked;
    if (mutedMusic) {
      for (int i = 0; i < PLAYERS.size(); i++) {
        MediaPlayer p = PLAYERS.valueAt(i);
        p.setVolume(0f, 0f);
      }
    }
    else {
      for (int i = 0; i < PLAYERS.size(); i++) {
        MediaPlayer p = PLAYERS.valueAt(i);
        p.setVolume(volume, volume);
      }
    }
  }

  public static int getCurrentMusic() {
    return currentMusic;
  }

}
