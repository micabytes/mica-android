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
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * Convenience class to replace FragmentActivity. 
 * 
 * @author micabyte
 */
public abstract class BaseActivity extends FragmentActivity implements View.OnClickListener {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// No title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	public void restart() {
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}
	
	/**
	 * Removes the reference to the activity from every view in a view hierarchy
	 * (listeners, images etc.) in order to limit/eliminate memory leaks. This is
	 * a "fix" for memory problems on older versions of Android; it may not be
	 * necessary on newer versions.
     *
     * see http://code.google.com/p/android/issues/detail?id=8488
	 * 
	 * If used, this method should be called in the onDestroy() method of each
	 * activity.
	 * 
	 * @param viewID normally the id of the root layout
	 */
	protected static void unbindReferences(Activity activity, int viewID, int adViewId) {
		try {
			View view = activity.findViewById(viewID);
			if (view != null) {
				unbindViewReferences(view);
				if (view instanceof ViewGroup)
					unbindViewGroupReferences((ViewGroup) view);
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
			if (view instanceof ViewGroup)
				unbindViewGroupReferences((ViewGroup) view);
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
			((WebView) view).destroyDrawingCache();
			((WebView) view).destroy();
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
	    Log.d(clazz.getName(), "DEBUG_MEMORY allocated " + df.format(Double.valueOf(Runtime.getRuntime().totalMemory()/1048576)) + "/" + df.format(Double.valueOf(Runtime.getRuntime().maxMemory()/1048576))+ "MB (" + df.format(Double.valueOf(Runtime.getRuntime().freeMemory()/1048576)) +"MB free)");
	    System.gc();
	    System.gc();
	}

	// User Interface Elements
	private ProgressDialog progress_;

	protected void showProgressIndicator(final int messageId) {
		this.progress_ = ProgressDialog.show(this, null, getString(messageId));
	}

	protected void showProgressIndicator(final String message) {
		this.progress_ = ProgressDialog.show(this, null, message);
	}

	public void hideProgressIndicator() {
		this.progress_.hide();
		this.progress_ = null;
	}

	public TextView getTextView(int resId) {
		return (TextView) findViewById(resId);
	}

	public TextView setTextView(int resId, Typeface font) {
		TextView t = (TextView) findViewById(resId);
		if (t != null) {
			t.setTypeface(font);
			t.setOnClickListener(this);
		}
		return t;
	}	
	
	protected EditText getEditText(int resId) {
		return (EditText) findViewById(resId);
	}

	protected ImageView getImageView(int resId) {
		return (ImageView) findViewById(resId);
	}

	protected ImageView setImageView(int resId, Bitmap img) {
		if (img == null)
			throw new IllegalArgumentException("setting image view " + resId
					+ "with null bitmap");
		ImageView v = (ImageView) findViewById(resId);
		v.setImageBitmap(img);
		return v;
	}

	protected CheckBox getCheckBox(int resId_) {
		CheckBox b = (CheckBox) findViewById(resId_);
		return b;
	}

	protected ImageButton getImageButton(int resId) {
		return (ImageButton) findViewById(resId);
	}

	protected ImageButton setImageButton(int resId) {
		ImageButton b = (ImageButton) findViewById(resId);
		if (b != null) {
			b.setOnClickListener(this);
		}
		return b;
	}

	protected ImageButton setImageButton(int resId, Bitmap img) {
		if (img == null)
			throw new IllegalArgumentException("setting image button " + resId
					+ "with null bitmap");
		ImageButton b = (ImageButton) findViewById(resId);
		b.setOnClickListener(this);
		b.setImageBitmap(img);
		return b;
	}

	protected Button setButton(int resId) {
		Button b = (Button) findViewById(resId);
		if (b != null) {
			b.setOnClickListener(this);
		}
		return b;
	}

	protected Button setButton(int resId, Typeface font) {
		Button b = (Button) findViewById(resId);
		if (b != null) {
			if (font != null)
				b.setTypeface(font);
			b.setOnClickListener(this);
		}
		return b;
	}

	protected Button getButton(int resId) {
		return (Button) findViewById(resId);
	}

	protected ToggleButton setToggleButton(int resId) {
		ToggleButton b = (ToggleButton) findViewById(resId);
		if (b != null) {
			b.setOnClickListener(this);
		}
		return b;
	}

	protected ToggleButton getToggleButton(int resId) {
		return (ToggleButton) findViewById(resId);
	}
	
	protected Spinner setSpinner(int resId, int arrId) {
		Spinner s = (Spinner) findViewById(resId);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, arrId, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		return s;
	}

	protected Spinner getSpinner(int resId) {
		Spinner s = (Spinner) findViewById(resId);
		return s;
	}

	protected ListView setListView(int resId, BaseAdapter adapter) {
		ListView l = (ListView) findViewById(resId);
		l.setAdapter(adapter);
		return l;
	}

	protected RelativeLayout getRelativeLayout(int resId) {
		RelativeLayout l = (RelativeLayout) findViewById(resId);
		return l;
	}

	protected LinearLayout getLinearLayout(int resId) {
		LinearLayout l = (LinearLayout) findViewById(resId);
		return l;
	}

	protected ProgressBar getProgressBar(int resId) {
		ProgressBar p = (ProgressBar) findViewById(resId);
		return p;
	}

}
