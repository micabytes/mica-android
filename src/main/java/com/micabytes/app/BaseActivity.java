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
package com.micabytes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.micabytes.R;
import com.micabytes.util.GameLog;
import com.micabytes.util.GameUtils;

@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class BaseActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
  private static final String TAG = BaseActivity.class.getName();
  private static final int RC_SIGN_IN = 9001;
  protected GoogleApiClient mGoogleApiClient;
  private boolean mResolvingConnectionFailure = false;
  private boolean mSignInClicked = false;
  private boolean mAutoStartSignInFlow = true;

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mGoogleApiClient.isConnected()) {
      mGoogleApiClient.disconnect();
    }
  }

  public void googleSignIn() {
    mSignInClicked = true;
    mGoogleApiClient.connect();
  }

  public void googleSignOut() {
    Games.signOut(mGoogleApiClient);
    mGoogleApiClient.disconnect();
    mSignInClicked = false;
    showSignInButton();
    showMessage((String) getText(R.string.signed_out));
  }

  public boolean isSignedIn() {
    return mGoogleApiClient.isConnected();
  }

  protected abstract void showSignInButton();

  protected abstract void showSignOutButton();

  @Override
  public void onConnected(@Nullable Bundle bundle) {
    showSignOutButton();
  }

  @Override
  public void onConnectionSuspended(int i) {
    GameLog.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
    mGoogleApiClient.connect();
  }

  @Override
  public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    GameLog.d(TAG, "onConnectionFailed() called, result: " + connectionResult);
    if (mResolvingConnectionFailure) {
      GameLog.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
      return;
    }
    if (mSignInClicked || mAutoStartSignInFlow) {
      mAutoStartSignInFlow = false;
      mSignInClicked = false;
      mResolvingConnectionFailure =
          GameUtils.resolveConnectionFailure(this, mGoogleApiClient, connectionResult, RC_SIGN_IN, getString(R.string.signin_other_error));
    }
    showSignInButton();
  }

  @Override
  public void startActivityForResult(Intent intent, int requestCode) {
    Intent sIntent = intent;
    if (sIntent == null) {
      sIntent = new Intent();
    }
    super.startActivityForResult(sIntent, requestCode);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      GameLog.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
          + resultCode + ", intent=" + data);
      mSignInClicked = false;
      mResolvingConnectionFailure = false;
      if (resultCode == RESULT_OK) {
        mGoogleApiClient.connect();
      } else {
        GameUtils.showActivityResultError(this, RC_SIGN_IN, resultCode, R.string.signin_other_error);
      }
    }
  }

  public abstract void setFragment();

  public abstract void openMenu();

  // Progress Dialog used to display loading messages.
  private ProgressDialog progressDialog;

  /**
   * Show a progress dialog for asynchronous operations.
   *
   * @param msg the message to display.
   */
  protected void showProgressDialog(String msg) {
    if (progressDialog == null) {
      progressDialog = new ProgressDialog(this);
      progressDialog.setIndeterminate(true);
    }
    progressDialog.setMessage(msg);
    progressDialog.show();
  }

  /**
   * Hide the progress dialog, if it was showing.
   */
  protected void dismissProgressDialog() {
    if (progressDialog != null && progressDialog.isShowing()) {
      try {
        progressDialog.dismiss();
      } catch (IllegalArgumentException e) {
        GameLog.logException(e);
      }
    }
  }

  /**
   * Display a status message for the last operation at the bottom of the screen.
   *
   * @param msg the message to display.
   */
  public void showMessage(String msg) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

}
