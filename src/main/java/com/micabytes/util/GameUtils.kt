/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.micabytes.util

import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.games.GamesActivityResultCodes
import com.micabytes.R
import timber.log.Timber

object GameUtils {

  /**
   * Show an [android.app.AlertDialog] with an 'OK' button and a message.
   *
   * @param activity the Activity in which the Dialog should be displayed.
   * @param message  the message to display in the Dialog.
   */
  private fun showAlert(activity: androidx.appcompat.app.AppCompatActivity, message: String) {
    if (!activity.isFinishing) {
      androidx.appcompat.app.AlertDialog.Builder(activity).setMessage(message)
          .setNeutralButton(android.R.string.ok, null).create().show()
    }
  }

  /**
   * Show a [android.app.Dialog] with the correct message for a connection error.
   *
   * @param activity         the Activity in which the Dialog should be displayed.
   * @param requestCode      the request code from onActivityResult.
   * @param actResp          the response code from onActivityResult.
   * @param errorDescription the resource id of a String for a generic error message.
   */
  fun showActivityResultError(activity: androidx.appcompat.app.AppCompatActivity?, requestCode: Int, actResp: Int, errorDescription: Int) {
    if (activity == null) {
      Timber.e("*** No Activity. Can't show failure dialog!")
      return
    }
    var errorDialog: Dialog?

    when (actResp) {
      GamesActivityResultCodes.RESULT_APP_MISCONFIGURED -> errorDialog = makeSimpleDialog(activity,
          activity.getString(R.string.app_misconfigured))
      GamesActivityResultCodes.RESULT_SIGN_IN_FAILED -> errorDialog = makeSimpleDialog(activity,
          activity.getString(R.string.sign_in_failed))
      GamesActivityResultCodes.RESULT_LICENSE_FAILED -> errorDialog = makeSimpleDialog(activity,
          activity.getString(R.string.license_failed))
      else -> {
        // No meaningful Activity response code, so generate default Google
        // Play services dialog
        val errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity)
        errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode,
            activity, requestCode, null)
        if (errorDialog == null) {
          // get fallback dialog
          Timber.e("No standard error dialog available. Making fallback dialog.")
          errorDialog = makeSimpleDialog(activity, activity.getString(errorDescription))
        }
      }
    }

    errorDialog.show()
  }

  /**
   * Create a simple [Dialog] with an 'OK' button and a message.
   *
   * @param activity the Activity in which the Dialog should be displayed.
   * @param text     the message to display on the Dialog.
   * @return an instance of [android.app.AlertDialog]
   */
  private fun makeSimpleDialog(activity: androidx.appcompat.app.AppCompatActivity, text: String): Dialog {
    return androidx.appcompat.app.AlertDialog.Builder(activity).setMessage(text)
        .setNeutralButton(android.R.string.ok, null).create()
  }

  /**
   * Create a simple [Dialog] with an 'OK' button, a title, and a message.
   *
   * @param activity the Activity in which the Dialog should be displayed.
   * @param title    the title to display on the dialog.
   * @param text     the message to display on the Dialog.
   * @return an instance of [android.app.AlertDialog]
   */
  fun makeSimpleDialog(activity: androidx.appcompat.app.AppCompatActivity, title: String, text: String): Dialog {
    return androidx.appcompat.app.AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(text)
        .setNeutralButton(android.R.string.ok, null)
        .create()
  }

}
