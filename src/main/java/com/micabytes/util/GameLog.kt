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
import kotlin.reflect.KClass

/** Wrapper over [Log.i] */
inline fun <reified T> T.logI(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) {
  log(onlyInDebugMode) { Log.i(getClassSimpleName(enclosingClass), message) }
}

/** Lazy wrapper over [Log.i] */
inline fun <reified T> T.logI(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
  log(onlyInDebugMode) { Log.i(getClassSimpleName(enclosingClass), lazyMessage.invoke()) }
}

/** Wrapper over [Log.d] */
inline fun <reified T> T.logD(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) {
  log(onlyInDebugMode) {
    if (Fabric.isInitialized()) Crashlytics.log(Log.DEBUG, getClassSimpleName(enclosingClass), message)
    Log.d(getClassSimpleName(enclosingClass), message)
  }
}

/** Lazy wrapper over [Log.d] */
inline fun <reified T> T.logD(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
  log(onlyInDebugMode) {
    if (Fabric.isInitialized()) Crashlytics.log(Log.DEBUG, getClassSimpleName(enclosingClass), lazyMessage.invoke())
    Log.d(getClassSimpleName(enclosingClass), lazyMessage.invoke())
  }
}

/** Wrapper over [Log.w] */
inline fun <reified T> T.logW(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) {
  log(onlyInDebugMode) {
    if (Fabric.isInitialized())
      Crashlytics.log(Log.WARN, getClassSimpleName(enclosingClass), message)
  }
}

/** Lazy wrapper over [Log.w] */
inline fun <reified T> T.logW(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
  log(onlyInDebugMode) {
    if (Fabric.isInitialized())
      Crashlytics.log(Log.WARN, getClassSimpleName(enclosingClass), lazyMessage.invoke())
  }
}

/** Wrapper over [Log.e] */
inline fun <reified T> T.logE(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) {
  log(onlyInDebugMode) {
    if (Fabric.isInitialized())
      Crashlytics.log(Log.WARN, getClassSimpleName(enclosingClass), message)
  }
}

/** Lazy wrapper over [Log.e] */
inline fun <reified T> T.logE(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
  log(onlyInDebugMode) { Log.e(getClassSimpleName(enclosingClass), lazyMessage.invoke()) }
}

inline fun <reified T> T.logX(e: Exception) {
  if (GameLog.debug) {
    e.printStackTrace()
  }
  if (Fabric.isInitialized()) {
    Crashlytics.logException(e)
  }
}

inline fun log(onlyInDebugMode: Boolean, logger: () -> Unit) {
  when {
    onlyInDebugMode && GameLog.debug -> logger()
    !onlyInDebugMode -> logger()
  }
}

/**
 * Utility that returns the name of the class from within it is invoked.
 * It allows to handle invocations from anonymous classes given that the string returned by `T::class.java.simpleName`
 * in this case is an empty string.
 *
 * @throws IllegalArgumentException if `enclosingClass` is `null` and this function is invoked within an anonymous class
 */
inline fun <reified T> T.getClassSimpleName(enclosingClass: KClass<*>?): String =
    if (T::class.java.simpleName.isNotBlank()) {
      T::class.java.simpleName
    } else { // Enforce the caller to pass a class to retrieve its simple name
      enclosingClass?.simpleName
          ?: throw IllegalArgumentException("enclosingClass cannot be null when invoked from an anonymous class")
    }


/*
 * This GameLogger uses Fabric and Crashlytics to log exceptions and useful diagnostics information.
 */
@NonNls
object GameLog {
  var debug = true
  private const val t = "MicaBytes"

  fun i(tag: String, s: String) {
    if (debug) Log.i(tag, s);
  }

  fun v(tag: String, s: String) {
    if (debug) Log.v(t, s)
  }

  fun d(s: String) = d(t, s)

  fun d(tag: String = "MicaBytes", s: String) {
    if (debug && Fabric.isInitialized()) Crashlytics.log(Log.DEBUG, t, s)
    if (debug) Log.d(t, s)
  }

  fun w(s: String) = w(t, s)

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

  fun e(s: String?) {
    if (Fabric.isInitialized())
      Crashlytics.log(Log.ERROR, t, s)
  }

  fun logException(e: Exception) {
    if (debug) {
      e.printStackTrace()
    }
    if (Fabric.isInitialized()) {
      Crashlytics.logException(e)
    }
  }

}// NOOP
