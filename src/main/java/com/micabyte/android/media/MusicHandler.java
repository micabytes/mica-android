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
package com.micabyte.android.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;
import android.util.SparseArray;

/**
 * MusicHandler is a simple, dumb wrapper around the Android mediaplayer. It plays music files placed
 * in the raw resource directory.
 * <p/>
 * General usage: Start it in the onResume of an activity, and stop the music in the onPause.
 * release should be called in onDestroy of the root activity.
 *
 * @author micabyte
 */
class MusicHandler {
    private static final String TAG = MusicHandler.class.getName();
    private static final int INVALID_NUMBER = 0;
    private static SparseArray<MediaPlayer> players_ = new SparseArray<MediaPlayer>();
    private static int nextMusic_ = INVALID_NUMBER;
    private static int currentMusic_ = INVALID_NUMBER;

    /**
     * Start playing a music resource
     */
    public static void start(Context c, int music) {
        start(c, music, false);
    }

    /**
     * Start playing a music resource
     *
     * @param c     The context (application or activity)
     * @param music The resource id of the music file
     * @param force Force-start playing this file
     */
    private static void start(Context c, int music, boolean force) {
        if ((!force) && (currentMusic_ != INVALID_NUMBER)) {
            // already playing some music and not forced to change immediately
            if (music != INVALID_NUMBER) {
                nextMusic_ = music;
                MediaPlayer mp = players_.get(music);
                if (mp == null) {
                    mp = MediaPlayer.create(c, music);
                    players_.put(music, mp);
                }
                MediaPlayer cp = players_.get(currentMusic_);
                if (cp != null) {
                    cp.setOnCompletionListener(new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer p) {
                            MusicHandler.next();
                        }
                    });
                    cp.setLooping(false);
                }
            }
            return;
        }
        if (currentMusic_ == music) {
            // already playing this music
            return;
        }
        if (currentMusic_ != INVALID_NUMBER) {
            // playing some other music, pause it and change
            pause();
        }
        currentMusic_ = music;
        MediaPlayer mp = players_.get(music);
        if (mp != null) {
            if (!mp.isPlaying()) {
                // Note: This continues the piece where it last let off
                mp.setLooping(true);
                mp.setOnCompletionListener(null);
                mp.start();
            }
        } else {
            mp = MediaPlayer.create(c, music);
            players_.put(music, mp);
            if (mp == null) {
                // Log an error, but don't do anthing (we do not want to risk f/c the app)
                Log.e(TAG, "player was not created successfully");
            } else {
                mp.setLooping(true);
                mp.setOnCompletionListener(null);
                mp.start();
            }
        }
    }

    /**
     * Pause all media players
     */
    private static void pause() {
        for (int i = 0; i < players_.size(); i++) {
            MediaPlayer p = players_.valueAt(i);
            if (p != null) {
                if (p.isPlaying()) {
                    p.pause();
                }
            }
        }
        currentMusic_ = INVALID_NUMBER;
    }

    /**
     * Advance to the next resource
     */
    private static void next() {
        if (nextMusic_ == INVALID_NUMBER) {
            return;
        }
        currentMusic_ = nextMusic_;
        nextMusic_ = INVALID_NUMBER;
        MediaPlayer p = players_.get(currentMusic_);
        if (p != null) {
            if (!p.isPlaying()) {
                p.setLooping(true);
                p.setOnCompletionListener(null);
                p.start();
            }
        }
    }

    /**
     * Release the media players.
     */
    public static void release() {
        for (int i = 0; i < players_.size(); i++) {
            MediaPlayer p = players_.valueAt(i);
            if (p != null) {
                if (p.isPlaying()) {
                    p.stop();
                }
                p.release();
            }
        }
        players_.clear();
        currentMusic_ = INVALID_NUMBER;
    }

}
