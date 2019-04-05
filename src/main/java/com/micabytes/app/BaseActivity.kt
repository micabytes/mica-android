/*
 * Copyright 2013 MicaBytes
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

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.PlayersClient
import com.google.android.gms.games.SnapshotsClient
import com.micabytes.R
import timber.log.Timber


abstract class BaseActivity : AppCompatActivity() {
  protected val RC_SIGN_IN = 9001
  protected val RC_LIST_SAVED_GAMES = 9002
  protected var mGoogleSignInClient: GoogleSignInClient? = null
  protected val mSnapshotsClient: SnapshotsClient?
    get() {
      if (isSignedIn) {
        val account = GoogleSignIn.getLastSignedInAccount(this) ?: return null
        return Games.getSnapshotsClient(this, account)
      }
      showMessage(getString(R.string.err_txt_not_signed_in))
      return null
    }
  private var mAchievementsClient: AchievementsClient? = null
  private var mPlayersClient: PlayersClient? = null
  //protected var googleApiClient: GoogleApiClient? = null

  val isSignedIn: Boolean
    get() = GoogleSignIn.getLastSignedInAccount(this) != null

  private fun signInSilently() {
    Timber.d("signInSilently()")
    mGoogleSignInClient?.silentSignIn()?.addOnCompleteListener { task ->
      if (task.isSuccessful) {
        Timber.d("signInSilently(): success")
        onConnected(task.result!!)
      } else {
        Timber.d("signInSilently(): ${task.exception?.message}. Code: ${(task.exception as ApiException).statusCode}")
        Timber.d("Silent signIn failed. Starting manual sign in!")
        onDisconnected()
        signIn()
      }
    }
  }

  fun signIn() {
    Timber.d("signIn()")
    startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
  }

  fun signOut() {
    Timber.d("signOut()")
    if (!isSignedIn) {
      Timber.w("signOut() called, but was not signed in!")
      onDisconnected()
      return
    }
    mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) { task ->
      val successful = task.isSuccessful
      Timber.d("signOut(): " + (if (successful) "success" else "failed"))
      showMessage(getString(R.string.signed_out))
      onDisconnected()
    }
  }

  protected fun onConnected(googleSignInAccount: GoogleSignInAccount) {
    Timber.d("onConnected(): connected to Google APIs")
    Games.getGamesClient(this, googleSignInAccount).setViewForPopups(findViewById(android.R.id.content))
    //mSnapshotsClient = Games.getSnapshotsClient(this, googleSignInAccount)
    mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
    mPlayersClient = Games.getPlayersClient(this, googleSignInAccount)
    // if we have accomplishments to push, push them
    /*
    if (!mOutbox.isEmpty()) {
      pushAccomplishments()
      Toast.makeText(this, getString(R.string.your_progress_will_be_uploaded),
          Toast.LENGTH_LONG).show()
    }
    loadAndPrintEvents()
    */
  }

  protected fun onDisconnected() {
    Timber.d("onDisconnected()")
    //mSnapshotsClient = null
    mAchievementsClient = null
    mPlayersClient = null
  }

  override fun onResume() {
    super.onResume()
    //if (!isSignedIn)
    //  signInSilently()
  }

  override fun startActivityForResult(intent: Intent?, requestCode: Int) {
    var sIntent: Intent? = intent
    if (sIntent == null) {
      sIntent = Intent()
    }
    super.startActivityForResult(sIntent, requestCode)
  }

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

  protected fun showProgressBar() {
    //TODO("not implemented")
  }

  protected fun hideProgressBar() {
    //TODO("not implemented")
  }

}
