package com.micabyte.android.app;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.micabyte.android.game.R;

public class BalloonPopup extends Popup implements OnDismissListener {
	private ImageView arrowUp_;
	private ImageView arrowDown_;
	private LayoutInflater inflater_;
	private TextView text_;
	private OnDismissListener mDismissListener;

	/**
	 * Constructor.
	 * 
	 * @param context Context
	 */
	public BalloonPopup(Context context) {
		super(context);
		this.inflater_ = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		setRootViewId(R.layout.balloonpopup);
	}
	
	public void setText(String txt) {
		this.text_.setText(txt);
	}

	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	public void setRootViewId(int id) {
		this.rootView_ = this.inflater_.inflate(id, null);
		this.text_ = (TextView) this.rootView_.findViewById(R.id.BalloonPopupText);
		this.arrowDown_ = (ImageView) this.rootView_.findViewById(R.id.arrow_down);
		this.arrowUp_ = (ImageView) this.rootView_.findViewById(R.id.arrow_up);
		this.rootView_.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setContentView(this.rootView_);
	}

	/**
	 * Show popup window_
	 */
	public void show(View anchor) {
		preShow();
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);
		Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
		this.rootView_.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int rootWidth = this.rootView_.getMeasuredWidth();
		int rootHeight = this.rootView_.getMeasuredHeight();
		@SuppressWarnings("deprecation")
		int screenWidth = this.windowManager_.getDefaultDisplay().getWidth();
		int xPos = anchorRect.centerX() - (rootWidth/2);
		if (xPos + rootWidth > screenWidth) xPos = screenWidth - rootWidth;
		if (xPos < 0) xPos = 0;
		int yPos = anchorRect.top - rootHeight;
		boolean onTop = true;
		// display on bottom
		//if (rootHeight > anchor.getTop()) {
		//	yPos = anchorRect.bottom;
		//	onTop = false;
		//}
		Log.d("Balloon", xPos + " " + yPos + " " + location[0] + " " + location[1] + " " + rootWidth + " " + rootHeight);
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() - xPos);
		//setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		this.window_.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
		//if (this.mAnimateTrack) this.text_.startAnimation(this.trackAnim_);
	}


	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
		final View showArrow = (whichArrow == R.id.arrow_up) ? this.arrowUp_ : this.arrowDown_;
		final View hideArrow = (whichArrow == R.id.arrow_up) ? this.arrowDown_ : this.arrowUp_;
		final int arrowWidth = this.arrowUp_.getMeasuredWidth();
		showArrow.setVisibility(View.VISIBLE);
		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();
		param.leftMargin = requestedX - arrowWidth / 2;
		hideArrow.setVisibility(View.INVISIBLE);
	}

	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog
	 * is dismissed by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(BalloonPopup.OnDismissListener listener) {
		setOnDismissListener(this);
		this.mDismissListener = listener;
	}

	@Override
	public void onDismiss() {
		if (this.mDismissListener != null) {
			this.mDismissListener.onDismiss();
		}
	}

	/**
	 * Listener for item click
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(BalloonPopup source, int pos, int actionId);
	}

	/**
	 * Listener for window dismiss
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}

}
