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

import com.micabyte.android.R;
import com.micabyte.android.graphics.ImageHandler;

/**
 * Custom Popup Window
 */
class Popup {
    private final Context context;
    protected View rootView;
    final PopupWindow window;
    final WindowManager windowManager;
    private Drawable background;

    /**
     * Constructor.
     *
     * @param context Context
     */
    Popup(Context con) {
        context = con;
        rootView = null;
        window = new PopupWindow(context);
        window.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    window.dismiss();
                    return true;
                }
                return false;
            }
        });
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        background = null;
    }

    protected void setRootView(View r) {
        rootView = r;
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
        window.dismiss();
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
        if (rootView == null)
            throw new IllegalStateException("setContentView was not called with a view to display.");
        onShow();
        if (background == null)
            window.setBackgroundDrawable(new BitmapDrawable(context.getResources(), ImageHandler.getInstance(context).get(R.drawable.ic_blank)));
        else
            window.setBackgroundDrawable(background);
        window.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setTouchable(true);
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.setContentView(rootView);
    }

    /**
     * Set background drawable.
     *
     * @param background Background drawable
     */
    public void setBackgroundDrawable(Drawable bkg) {
        background = bkg;
    }

    /**
     * Set content view.
     *
     * @param root Root view
     */
    void setContentView(View root) {
        rootView = root;
        window.setContentView(root);
    }

    /**
     * Set content view.
     *
     * @param layoutResID Resource id
     */
    public void setContentView(int layoutResID) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(inflater.inflate(layoutResID, null));
    }

    /**
     * Set listener on window dismissed.
     *
     * @param listener Listener to trigger when the popup is dismissed
     */
    void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        window.setOnDismissListener(listener);
    }

}
