package com.micabyte.android.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.appcompat.BuildConfig;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.micabyte.android.R;

public class BalloonPopup extends Popup implements PopupWindow.OnDismissListener {
    private final ImageView arrowUp;
    private final ImageView arrowDown;
    private final TextView text;
    private OnDismissListener dismissListener;

    /**
     * Constructor.
     *
     * @param context Context
     */
    @SuppressLint("InflateParams")
    public BalloonPopup(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setRootView(inflater.inflate(R.layout.balloonpopup, null));
        text = (TextView) rootView.findViewById(R.id.BalloonPopupText);
        arrowDown = (ImageView) rootView.findViewById(R.id.arrow_down);
        arrowUp = (ImageView) rootView.findViewById(R.id.arrow_up);
        rootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(rootView);
    }

    public void setText(String txt) {
        text.setText(txt);
    }

    /**
     * Show popup window
     */
    public void show(View anchor) {
        preShow();
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
        rootView.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int rootWidth = rootView.getMeasuredWidth();
        int rootHeight = rootView.getMeasuredHeight();
        @SuppressWarnings("deprecation")
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int xPos = anchorRect.centerX() - (rootWidth / 2);
        if ((xPos + rootWidth) > screenWidth) xPos = screenWidth - rootWidth;
        if (xPos < 0) xPos = 0;
        int yPos = anchorRect.top - rootHeight;
        //boolean onTop = true;
        // display on bottom
        //if (rootHeight > anchor.getTop()) {
        //	yPos = anchorRect.bottom;
        //	onTop = false;
        //}
        //showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() - xPos);
        if (BuildConfig.DEBUG) Log.d("Balloon", xPos + " " + yPos + ' ' + location[0] + ' ' + location[1] + ' ' + rootWidth + ' ' + rootHeight);
        showArrow((R.id.arrow_down), anchorRect.centerX() - xPos);
        //setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
        window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
        //if (this.mAnimateTrack) this.text.startAnimation(this.trackAnim);
    }


    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        View showArrow = (whichArrow == R.id.arrow_up) ? arrowUp : arrowDown;
        View hideArrow = (whichArrow == R.id.arrow_up) ? arrowDown : arrowUp;
        int arrowWidth = arrowUp.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();
        param.leftMargin = requestedX - arrowWidth / 2;
        hideArrow.setVisibility(View.INVISIBLE);
    }

    /**
     * Set listener for window dismissed. This listener will only be fired if the quick action
     * dialog is dismissed by clicking outside the dialog or clicking on sticky item.
     */
    public void setOnDismissListener(OnDismissListener listener) {
        setOnDismissListener(this);
        dismissListener = listener;
    }

    @Override
    public void onDismiss() {
        if (dismissListener != null) {
            dismissListener.onDismiss();
            return;
        }
        super.onDismiss();
    }

    /**
     * Listener for item click
     */
    @SuppressWarnings("InterfaceNeverImplemented")
    protected interface OnActionItemClickListener {
        void onItemClick(BalloonPopup source, int pos, int actionId);
    }

    /**
     * Listener for window dismiss
     */
    @SuppressWarnings("InterfaceNeverImplemented")
    protected interface OnDismissListener {
        void onDismiss();
    }

}
