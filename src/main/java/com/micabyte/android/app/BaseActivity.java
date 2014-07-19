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

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.micabyte.android.util.GameHelper;

/**
 * Base class for Game Activities.
 * <p/>
 * This implementation now also implements the GamesClient object from Google Play Games services
 * and manages its lifecycle. Subclasses should override the @link{#onSignInSucceeded} and
 *
 * @author micabyte
 * @link{#onSignInFailed abstract methods.
 */
public abstract class BaseActivity extends FragmentActivity implements GameHelper.GameHelperListener {
    // The game helper object. This class is mainly a wrapper around this object.
    protected GameHelper mHelper;

    // We expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all.
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
    public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

    // Requested clients. By default, that's just the games client.
    protected int mRequestedClients = CLIENT_GAMES;

    private final static String TAG = "BaseGameActivity";
    protected boolean mDebugLog = false;

    public abstract void setFragment();

    public abstract void openMenu();

    /**
     * Constructs a BaseGameActivity with default client (GamesClient).
     */
    protected BaseActivity() {
        super();
    }

    /**
     * Constructs a BaseGameActivity with the requested clients.
     *
     * @param requestedClients The requested clients (a combination of CLIENT_GAMES,
     *                         CLIENT_PLUS and CLIENT_APPSTATE).
     */
    protected BaseActivity(int requestedClients) {
        super();
        setRequestedClients(requestedClients);
    }

    /**
     * Sets the requested clients. The preferred way to set the requested clients is
     * via the constructor, but this method is available if for some reason your code
     * cannot do this in the constructor. This must be called before onCreate or getGameHelper()
     * in order to have any effect. If called after onCreate()/getGameHelper(), this method
     * is a no-op.
     *
     * @param requestedClients A combination of the flags CLIENT_GAMES, CLIENT_PLUS
     *                         and CLIENT_APPSTATE, or CLIENT_ALL to request all available clients.
     */
    protected void setRequestedClients(int requestedClients) {
        this.mRequestedClients = requestedClients;
    }

    public GameHelper getGameHelper() {
        if (this.mHelper == null) {
            this.mHelper = new GameHelper(this, this.mRequestedClients);
            this.mHelper.enableDebugLog(this.mDebugLog);
        }
        return this.mHelper;
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (this.mHelper == null) {
            getGameHelper();
        }
        this.mHelper.setup(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        this.mHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.mHelper.onStop();
    }

    @Override
    protected void onActivityResult(int request, int response, Intent data) {
        super.onActivityResult(request, response, data);
        this.mHelper.onActivityResult(request, response, data);
    }

    protected GoogleApiClient getApiClient() {
        return this.mHelper.getApiClient();
    }

    protected boolean isSignedIn() {
        return this.mHelper.isSignedIn();
    }

    protected void beginUserInitiatedSignIn() {
        this.mHelper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        this.mHelper.signOut();
    }

    protected void showAlert(String message) {
        this.mHelper.makeSimpleDialog(message).show();
    }

    protected void showAlert(String title, String message) {
        this.mHelper.makeSimpleDialog(title, message).show();
    }

    protected void enableDebugLog(boolean enabled) {
        this.mDebugLog = true;
        if (this.mHelper != null) {
            this.mHelper.enableDebugLog(enabled);
        }
    }

    @Deprecated
    protected void enableDebugLog(boolean enabled, String tag) {
        Log.w(TAG, "BaseGameActivity.enabledDebugLog(bool,String) is " +
                "deprecated. Use enableDebugLog(boolean)");
        enableDebugLog(enabled);
    }

    protected String getInvitationId() {
        return this.mHelper.getInvitationId();
    }

    protected void reconnectClient() {
        this.mHelper.reconnectClient();
    }

    protected boolean hasSignInError() {
        return this.mHelper.hasSignInError();
    }

    protected GameHelper.SignInFailureReason getSignInError() {
        return this.mHelper.getSignInError();
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
    protected static void unbindReferences(Activity activity, int viewID, int adViewId) {
        try {
            View view = activity.findViewById(viewID);
            if (view != null) {
                unbindViewReferences(view);
                if (view instanceof ViewGroup) unbindViewGroupReferences((ViewGroup) view);
            }
        } catch (Throwable e) {
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
        } catch (Throwable mayHappen) {
            // AdapterViews, ListViews and potentially other ViewGroups don't
            // support the removeAllViews operation
        }
    }

    private static void unbindViewReferences(View view) {
        // set all listeners to null
        try {
            view.setOnClickListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnCreateContextMenuListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnFocusChangeListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnKeyListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnLongClickListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        try {
            view.setOnClickListener(null);
        } catch (Throwable mayHappen) {
            // NOOP - not supported by all views/versions
        }
        // set background to null
        Drawable d = view.getBackground();
        if (d != null) {
            d.setCallback(null);
        }
        if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            d = imageView.getDrawable();
            if (d != null) {
                d.setCallback(null);
            }
            imageView.setImageDrawable(null);
            imageView.setImageBitmap(null);
        }
        if (view instanceof ImageButton) {
            ImageButton imageB = (ImageButton) view;
            d = imageB.getDrawable();
            if (d != null) {
                d.setCallback(null);
            }
            imageB.setImageDrawable(null);
        }
        // destroy webview
        if (view instanceof WebView) {
            view.destroyDrawingCache();
            ((WebView)view).destroy();
        }
    }

    /*
     * Show Heap
     */
    @SuppressWarnings("rawtypes")
    public static void logHeap(Class clazz) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        Log.d(clazz.getName(),
                "DEBUG_MEMORY allocated " + df.format((double) (Runtime.getRuntime().totalMemory() / 1048576)) + "/"
                        + df.format((double) (Runtime.getRuntime().maxMemory() / 1048576)) + "MB ("
                        + df.format((double) (Runtime.getRuntime().freeMemory() / 1048576)) + "MB free)"
        );
        System.gc();
        System.gc();
    }

}
