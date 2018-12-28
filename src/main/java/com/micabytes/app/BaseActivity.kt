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
import com.google.android.gms.games.AchievementsClient
import com.google.android.gms.games.Games
import com.google.android.gms.games.PlayersClient
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.micabytes.R
import com.micabytes.util.GameLog


abstract class BaseActivity : AppCompatActivity() {
  protected var mGoogleSignInClient: GoogleSignInClient? = null
  protected var mSnapshotsClient: SnapshotsClient? = null
  private var mAchievementsClient: AchievementsClient? = null
  //private var mLeaderboardsClient: LeaderboardsClient? = null
  //private var mEventsClient: EventsClient? = null
  private var mPlayersClient: PlayersClient? = null  //protected var googleApiClient: GoogleApiClient? = null
  private var resolvingConnectionFailure = false
  private var signInClicked = false
  private var autoStartSignInFlow = true
  //private val mOutbox = AccomplishmentsOutbox()

  val isSignedIn: Boolean
    get() = GoogleSignIn.getLastSignedInAccount(this) != null

  private fun signInSilently() {
    GameLog.d("signInSilently()")
    mGoogleSignInClient?.silentSignIn()?.addOnCompleteListener(this,
        OnCompleteListener<GoogleSignInAccount> {
          fun onComplete(task: Task<GoogleSignInAccount>) {
            if (task.isSuccessful) {
              GameLog.d("signInSilently(): success")
            } else {
              GameLog.logException(task.exception!!)
              onDisconnected()
            }
          }
        })
  }

  fun signIn() {
    GameLog.d("signIn()")
    startActivityForResult(mGoogleSignInClient?.signInIntent, RC_SIGN_IN)
  }

  fun signOut() {
    GameLog.d("signOut()")
    if (!isSignedIn) {
      GameLog.w("signOut() called, but was not signed in!")
      return
    }
    mGoogleSignInClient?.signOut()?.addOnCompleteListener(this) { task ->
      val successful = task.isSuccessful
      GameLog.d("signOut(): " + (if (successful) "success" else "failed"))
      onDisconnected()
    }
  }

  protected fun onConnected(googleSignInAccount: GoogleSignInAccount) {
    GameLog.d("onConnected(): connected to Google APIs")
    mSnapshotsClient = Games.getSnapshotsClient(this, googleSignInAccount)
    mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount)
    mPlayersClient = Games.getPlayersClient(this, googleSignInAccount)
    //mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount)
    //mEventsClient = Games.getEventsClient(this, googleSignInAccount)
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
    GameLog.d("onDisconnected()")
    mSnapshotsClient = null
    mAchievementsClient = null
    mPlayersClient = null
  }

  fun onSignInButtonClicked() {
    signIn()
  }

  fun onSignOutButtonClicked() {
    signOut()
  }



  // onResume?
  override fun onStart() {
    super.onStart()
    signInSilently()
  }

  override fun onStop() {
    super.onStop()
    if (isSignedIn) {
      onDisconnected()
    }
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
    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  protected fun hideProgressBar() {
    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  protected fun showAlertBar(resId: Int) {
    //(findViewById<View>(R.id.alert_bar) as TextView).text = getString(resId)
    //findViewById<View>(R.id.alert_bar).visibility = View.VISIBLE
  }

  protected fun hideAlertBar() {
    /*val alertBar = findViewById<View>(R.id.alert_bar)
    if (alertBar != null && alertBar.visibility != View.GONE) {
      alertBar.visibility = View.GONE
    }*/
  }


  companion object {
    private val TAG = BaseActivity::class.java.name
    private const val RC_SIGN_IN = 9001
  }

}
