package com.micabyte.android.app;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.micabyte.android.R;
import com.micabyte.android.graphics.ImageHandler;

/**
 * Custom Popup Window
 */
@SuppressWarnings("JavaDoc")
class Popup {
    private final Context context_;
    final WindowManager windowManager_;
    final PopupWindow window_;
    View rootView_ = null;
    private Drawable background_ = null;

    /**
     * Constructor.
     *
     * @param context Context
     */
    Popup(Context context) {
        this.context_ = context;
        this.window_ = new PopupWindow(context);
        this.window_.setTouchInterceptor(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Popup.this.window_.dismiss();
                    return true;
                }
                return false;
            }
        });
        this.windowManager_ = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * On Dismiss
     */
    protected void onDismiss() {
        // NOOP
    }

    /**
     * Dismiss the popup window.
     */
    public void dismiss() {
        this.window_.dismiss();
    }

    /**
     * On show
     */
    @SuppressWarnings("EmptyMethod")
    void onShow() {
        // NOOP
    }

    /**
     * On pre show
     */
    void preShow() {
        if (this.rootView_ == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");
        onShow();
        if (this.background_ == null)
            this.window_.setBackgroundDrawable(new BitmapDrawable(this.context_.getResources(), ImageHandler.getInstance(this.context_).get(R.drawable.ic_blank)));
        else
            this.window_.setBackgroundDrawable(this.background_);
        this.window_.setWidth(LayoutParams.WRAP_CONTENT);
        this.window_.setHeight(LayoutParams.WRAP_CONTENT);
        this.window_.setTouchable(true);
        this.window_.setFocusable(true);
        this.window_.setOutsideTouchable(true);
        this.window_.setContentView(this.rootView_);
    }

    /**
     * Set background drawable.
     *
     * @param background Background drawable
     */
    public void setBackgroundDrawable(Drawable background) {
        this.background_ = background;
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    void setContentView(View root) {
        this.rootView_ = root;
        this.window_.setContentView(root);
    }

    /**
     * Set content view.
     *
     * @param layoutResID Resource id
     */
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) this.context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflater.inflate(layoutResID, null));
    }

    /**
     * Set listener on window dismissed.
     *
     * @param listener
     */
    void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        this.window_.setOnDismissListener(listener);
    }

}
