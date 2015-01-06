package com.micabyte.android.app;

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

public class BalloonPopup extends Popup implements PopupWindow.OnDismissListener {
    private ImageView arrowUp;
    private ImageView arrowDown;
    private final LayoutInflater inflater;
    private TextView text_;
    private OnDismissListener dismissListener;

    /**
     * Constructor.
     *
     * @param context Context
     */
    public BalloonPopup(Context context) {
        super(context);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setRootViewId(R.layout.balloonpopup);
    }

    public void setText(String txt) {
        text_.setText(txt);
    }

    /**
     * Set root view.
     *
     * @param id Layout resource id
     */
    void setRootViewId(int id) {
        rootView_ = inflater.inflate(id, null);
        text_ = (TextView) rootView_.findViewById(R.id.BalloonPopupText);
        arrowDown = (ImageView) rootView_.findViewById(R.id.arrow_down);
        arrowUp = (ImageView) rootView_.findViewById(R.id.arrow_up);
        rootView_.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(rootView_);
    }

    /**
     * Show popup window_
     */
    public void show(View anchor) {
        preShow();
        final int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        final Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
        rootView_.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int rootWidth = rootView_.getMeasuredWidth();
        final int rootHeight = rootView_.getMeasuredHeight();
        @SuppressWarnings("deprecation") final
        int screenWidth = windowManager_.getDefaultDisplay().getWidth();
        int xPos = anchorRect.centerX() - (rootWidth / 2);
        if (xPos + rootWidth > screenWidth) xPos = screenWidth - rootWidth;
        if (xPos < 0) xPos = 0;
        final int yPos = anchorRect.top - rootHeight;
        //boolean onTop = true;
        // display on bottom
        //if (rootHeight > anchor.getTop()) {
        //	yPos = anchorRect.bottom;
        //	onTop = false;
        //}
        //showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() - xPos);
        if (BuildConfig.DEBUG) Log.d("Balloon", xPos + " " + yPos + " " + location[0] + " " + location[1] + " " + rootWidth + " " + rootHeight);
        showArrow((R.id.arrow_down), anchorRect.centerX() - xPos);
        //setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
        window_.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
        //if (this.mAnimateTrack) this.text_.startAnimation(this.trackAnim_);
    }


    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? arrowUp : arrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? arrowDown : arrowUp;
        final int arrowWidth = arrowUp.getMeasuredWidth();
        showArrow.setVisibility(View.VISIBLE);
        final ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();
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
        }
    }

    /**
     * Listener for item click
     */
    public interface OnActionItemClickListener {
        void onItemClick(BalloonPopup source, int pos, int actionId);
    }

    /**
     * Listener for window dismiss
     */
    public interface OnDismissListener {
        void onDismiss();
    }

}
