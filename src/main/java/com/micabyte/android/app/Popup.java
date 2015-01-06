package com.micabyte.android.app;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

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
        context_ = context;
        window_ = new PopupWindow(context);
        window_.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    window_.dismiss();
                    return true;
                }
                return false;
            }
        });
        windowManager_ = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
        window_.dismiss();
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
        if (rootView_ == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");
        onShow();
        if (background_ == null)
            window_.setBackgroundDrawable(new BitmapDrawable(context_.getResources(), ImageHandler.getInstance(context_).get(R.drawable.ic_blank)));
        else
            window_.setBackgroundDrawable(background_);
        window_.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        window_.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window_.setTouchable(true);
        window_.setFocusable(true);
        window_.setOutsideTouchable(true);
        window_.setContentView(rootView_);
    }

    /**
     * Set background drawable.
     *
     * @param background Background drawable
     */
    public void setBackgroundDrawable(Drawable background) {
        background_ = background;
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    void setContentView(View root) {
        rootView_ = root;
        window_.setContentView(root);
    }

    /**
     * Set content view.
     *
     * @param layoutResID Resource id
     */
    public void setContentView(int layoutResID) {
        final LayoutInflater inflater = (LayoutInflater) context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflater.inflate(layoutResID, null));
    }

    /**
     * Set listener on window dismissed.
     *
     * @param listener
     */
    void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        window_.setOnDismissListener(listener);
    }

}
