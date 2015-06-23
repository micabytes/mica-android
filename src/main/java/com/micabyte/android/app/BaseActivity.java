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
package com.micabyte.android.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.micabyte.android.R;
import com.micabyte.android.util.GameHelper;

import java.text.DecimalFormat;

/**
 * Base class for Game Activities.
 * <p/>
 * This implementation now also implements the GamesClient object from Google Play Games services
 * and manages its lifecycle. Subclasses should override the onSignInSucceeded
 *
 * @author micabyte
 */
@SuppressLint("Registered")
public class BaseActivity extends FragmentActivity implements GameHelper.GameHelperListener {
    // The game helper object. This class is mainly a wrapper around this object.
    private GameHelper gameHelper = null;

    // We expose these constants here because we don't want users of this class
    // to have to know about gameHelper at all.
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
    public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    public static final int CLIENT_SAVES = GameHelper.CLIENT_SNAPSHOT;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

    // Requested clients. By default, that's just the games client.
    private int requestedClients = CLIENT_GAMES & CLIENT_SAVES;

    private boolean debugLog;

    /**
     * Constructs a BaseGameActivity with default client (GamesClient).
     */
    protected BaseActivity() {
        // NOOP
    }

    /**
     * Constructs a BaseGameActivity with the requested clients.
     *
     * @param requestClients The requested clients (a combination of CLIENT_GAMES, CLIENT_PLUS and CLIENT_APPSTATE).
     */
    protected BaseActivity(int requestClients) {
        requestedClients = requestClients;
    }

    /**
     * Sets the requested clients. The preferred way to set the requested clients is
     * via the constructor, but this method is available if for some reason your code
     * cannot do this in the constructor. This must be called before onCreate or getGameHelper()
     * in order to have any effect. If called after onCreate()/getGameHelper(), this method
     * is a no-op.
     *
     * @param requestClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
     *                       and CLIENT_APPSTATE, or CLIENT_ALL to request all available clients.
     */
    protected void setRequestedClients(int requestClients) {
        requestedClients = requestClients;
    }

    public GameHelper getGameHelper() {
        if (gameHelper == null) {
            gameHelper = new GameHelper(this, requestedClients);
            gameHelper.enableDebugLog(debugLog);
        }
        return gameHelper;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getGameHelper().setup(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getGameHelper().onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getGameHelper().onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getGameHelper().onActivityResult(requestCode, resultCode, data);
    }

    public GoogleApiClient getApiClient() {
        return getGameHelper().getApiClient();
    }

    public boolean isSignedIn() {
        return getGameHelper().isSignedIn();
    }

    public void beginUserInitiatedSignIn() {
        getGameHelper().beginUserInitiatedSignIn();
    }

    public void signOut() {
        getGameHelper().signOut();
    }

    protected void showAlert(String message) {
        getGameHelper().makeSimpleDialog(message).show();
    }

    protected void showAlert(String title, String message) {
        getGameHelper().makeSimpleDialog(title, message).show();
    }

    protected void enableDebugLog(boolean enabled) {
        debugLog = true;
        getGameHelper().enableDebugLog(enabled);
    }

    protected String getInvitationId() {
        return getGameHelper().getInvitationId();
    }

    protected void reconnectClient() {
        getGameHelper().reconnectClient();
    }

    protected boolean hasSignInError() {
        return getGameHelper().hasSignInError();
    }

    protected GameHelper.SignInFailureReason getSignInError() {
        return getGameHelper().getSignInError();
    }

    public void showSpinner() {
        findViewById(R.id.ProgressLayout).setVisibility(View.VISIBLE);
    }

    public void dismissSpinner() {
        findViewById(R.id.ProgressLayout).setVisibility(View.GONE);
    }


    /**
     * Removes the reference to the activity from every view in a view hierarchy (listeners, images
     * etc.) in order to limit/eliminate memory leaks. This is a "fix" for memory problems on older
     * versions of Android; it may not be necessary on newer versions.
     * <p/>
     * see http://code.google.com/p/android/issues/detail?id=8488
     * <p/>
     * If used, this method should be called in the onDestroy() method of each activity.
     *
     * @param viewID normally the id of the root layout
     */
    @SuppressWarnings("CallToSystemGC")
    protected static void unbindReferences(Activity activity, int viewID) {
        try {
            View view = activity.findViewById(viewID);
            if (view != null) {
                unbindViewReferences(view);
                if (view instanceof ViewGroup) unbindViewGroupReferences((ViewGroup) view);
            }
        } catch (Throwable ignored) {
            // whatever exception is thrown just ignore it because a crash is
            // likely to be worse than this method not doing what it's supposed to do
            // e.printStackTrace();
        }
        System.gc();
    }

    private static void unbindViewGroupReferences(ViewGroup viewGroup) {
        int nrOfChildren = viewGroup.getChildCount();
        for (int i = 0; i < nrOfChildren; i++) {
            View view = viewGroup.getChildAt(i);
            unbindViewReferences(view);
            if (view instanceof ViewGroup) unbindViewGroupReferences((ViewGroup) view);
        }
        try {
            viewGroup.removeAllViews();
        } catch (Throwable ignored) {
            // AdapterViews, ListViews and potentially other ViewGroups don't
            // support the removeAllViews operation
        }
    }

    @SuppressWarnings("ChainOfInstanceofChecks")
    private static void unbindViewReferences(View view) {
        // set all listeners to null
        try {
            view.setOnClickListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnCreateContextMenuListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnFocusChangeListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnKeyListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnLongClickListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnClickListener(null);
        } catch (Throwable ignored) {
            // NOOP - not supported by all views/versions
        }
        // set background to null
        Drawable background = view.getBackground();
        if (background != null) {
            background.setCallback(null);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            Drawable imageViewDrawable = imageView.getDrawable();
            if (imageViewDrawable != null) {
                imageViewDrawable.setCallback(null);
            }
            imageView.setImageDrawable(null);
            imageView.setImageBitmap(null);
        }
        if (view instanceof ImageButton) {
            ImageButton imageButton = (ImageButton) view;
            Drawable imageButtonDrawable = imageButton.getDrawable();
            if (imageButtonDrawable != null) {
                imageButtonDrawable.setCallback(null);
            }
            imageButton.setImageDrawable(null);
        }
        // destroy WebView
        if (view instanceof WebView) {
            view.destroyDrawingCache();
            ((WebView) view).destroy();
        }
    }

    /*
     * Show Heap
     */
    @SuppressWarnings({"MagicNumber", "CallToSystemGC"})
    public static void logHeap(Class clazz) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        if (BuildConfig.DEBUG) Log.d(clazz.getName(),
                "DEBUG_MEMORY allocated " + df.format((double) (Runtime.getRuntime().totalMemory() / 1048576)) + '/'
                        + df.format((double) (Runtime.getRuntime().maxMemory() / 1048576)) + " MB ("
                        + df.format((double) (Runtime.getRuntime().freeMemory() / 1048576)) + " MB free)"
        );
        System.gc();
        System.gc();
    }

    @Override
    public void onSignInFailed() {
        // NOOP
    }

    @Override
    public void onSignInSucceeded() {
        // NOOP
    }

    public void setFragment() {
        // NOOP
    }

    public void openMenu() {
        // NOOP
    }

    protected int getRequestedClients() {
        return requestedClients;
    }

    // Progress Dialog used to display loading messages.
    private ProgressDialog progressDialog;

    /**
     * Show a progress dialog for asynchronous operations.
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
            progressDialog.dismiss();
        }
    }

    /**
     * Display a status message for the last operation at the bottom of the screen.
     * @param msg the message to display.
     * @param error true if an error occurred, false otherwise.
     */
    public void showMessage(String msg, boolean error) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public boolean isDebugLog() {
        return debugLog;
    }

    public void setDebugLog(boolean log) {
        debugLog = log;
    }

}
