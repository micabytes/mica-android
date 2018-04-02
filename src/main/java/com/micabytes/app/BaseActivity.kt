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
package com.micabytes.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.games.Games
import com.micabytes.R
import com.micabytes.util.GameLog
import com.micabytes.util.GameUtils

abstract class BaseActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
  protected var googleApiClient: GoogleApiClient? = null
  private var resolvingConnectionFailure = false
  private var signInClicked = false
  private var autoStartSignInFlow = true

  val isSignedIn: Boolean
    get() = googleApiClient!!.isConnected

  override fun onStart() {
    super.onStart()
    googleApiClient!!.connect()
  }

  override fun onStop() {
    super.onStop()
    if (googleApiClient!!.isConnected) {
      googleApiClient!!.disconnect()
    }
  }

  fun googleSignIn() {
    signInClicked = true
    googleApiClient!!.connect()
  }

  fun googleSignOut() {
    Games.signOut(googleApiClient!!)
    googleApiClient!!.disconnect()
    signInClicked = false
    showSignInButton()
    showMessage(getText(R.string.signed_out) as String)
  }

  protected abstract fun showSignInButton()

  protected abstract fun showSignOutButton()

  override fun onConnected(bundle: Bundle?) {
    showSignOutButton()
  }

  override fun onConnectionSuspended(i: Int) {
    GameLog.d(TAG, "onConnectionSuspended() called. Trying to reconnect.")
    googleApiClient!!.connect()
  }

  override fun onConnectionFailed(connectionResult: ConnectionResult) {
    GameLog.d(TAG, "onConnectionFailed() called, result: " + connectionResult)
    if (resolvingConnectionFailure) {
      GameLog.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.")
      return
    }
    if (signInClicked || autoStartSignInFlow) {
      autoStartSignInFlow = false
      signInClicked = false
      resolvingConnectionFailure = GameUtils.resolveConnectionFailure(this, googleApiClient!!, connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error))
    }
    showSignInButton()
  }

  override fun startActivityForResult(intent: Intent?, requestCode: Int) {
    var sIntent: Intent? = intent
    if (sIntent == null) {
      sIntent = Intent()
    }
    super.startActivityForResult(sIntent, requestCode)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == RC_SIGN_IN) {
      GameLog.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
          + resultCode + ", intent=" + data)
      signInClicked = false
      resolvingConnectionFailure = false
      if (resultCode == Activity.RESULT_OK) {
        googleApiClient!!.connect()
      } else {
        GameUtils.showActivityResultError(this, RC_SIGN_IN, resultCode, R.string.signin_other_error)
      }
    }
  }

  abstract fun setFragment()

  abstract fun openMenu()

  /**
   * Display a status message for the last operation at the bottom of the screen.
   *
   * @param msg the message to display.
   */
  open fun showMessage(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
  }

  fun alert(message: String) {
    val bld = AlertDialog.Builder(this)
    bld.setMessage(message)
    bld.setNeutralButton(getString(R.string.common_ok), null)
    bld.create().show()
  }

  companion object {
    private val TAG = BaseActivity::class.java.name
    private val RC_SIGN_IN = 9001
  }

}
