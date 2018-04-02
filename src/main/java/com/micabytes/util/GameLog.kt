/*
 * Copyright 2015 MicaByte Systems
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

package com.micabytes.util

import android.util.Log

import com.crashlytics.android.Crashlytics

import org.jetbrains.annotations.NonNls

import io.fabric.sdk.android.Fabric

/*
 * This GameLogger uses Fabric and Crashlytics to log exceptions and useful diagnostics information.
 */
@NonNls
object GameLog {
  var debug = true
  private const val t = "MicaBytes"

  fun i(tag: String, s: String) {
    //if (debug) Log.i(tag, s);
    if (Fabric.isInitialized())
      Crashlytics.log(Log.INFO, t, s)
  }

  fun v(tag: String, s: String) {
    if (debug) Log.v(t, s)
  }

  fun d(tag: String, s: String) {
    if (debug) Log.d(t, s)
  }

  fun w(tag: String, s: String) {
    //if (debug)
    //  Log.w(tag, s);
    if (Fabric.isInitialized())
      Crashlytics.log(Log.WARN, t, s)
  }

  fun e(tag: String, s: String?) {
    //if (debug)
    //  Log.e(tag, s);
    if (Fabric.isInitialized())
      Crashlytics.log(Log.ERROR, t, s)
  }

  fun logException(e: Exception) {
    if (debug) {
      e.printStackTrace();
    }
    if (Fabric.isInitialized()) {
      Crashlytics.logException(e)
    }
  }

}// NOOP
