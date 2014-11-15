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

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

/**
 * Convenience class to replace Fragment
 * 
 * @author micabyte
 */
@SuppressWarnings({"WeakerAccess"})
public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onStart() {
        super.onStart();
        createFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateFragment();
    }

    // Used to set up UI elements
    protected abstract void createFragment();
    // Used to update the UI elements
    public abstract void updateFragment();


    public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}

    @SuppressWarnings("ConstantConditions")
    protected TextView getTextView(int resId) {
        return (TextView) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    public TextView setTextView(int resId, Typeface font) {
        TextView t = (TextView) getView().findViewById(resId);
        if (t != null) {
        	if (font != null)
        		t.setTypeface(font);
            t.setOnClickListener(this);
        }
        return t;
    }

    @SuppressWarnings("ConstantConditions")
    protected EditText getEditText(int resId) {
        return (EditText) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageView getImageView(int resId) {
        return (ImageView) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageView setImageView(int resId) {
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setOnClickListener(this);
        return v;
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageView setImageView(int resId, Bitmap img) {
        if (img == null)
            throw new IllegalArgumentException("setting image view " + resId + "with null bitmap");
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setImageBitmap(img);
        return v;
    }

    @SuppressWarnings("ConstantConditions")
    protected CheckBox getCheckBox(int resId_) {
        return (CheckBox) getView().findViewById(resId_);
    }

    @SuppressWarnings("ConstantConditions")
    protected RadioButton getRadioButton(int resId_) {
    	return (RadioButton) getView().findViewById(resId_);
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageButton getImageButton(int resId) {
        return (ImageButton) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageButton setImageButton(int resId) {
        ImageButton b = (ImageButton) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    @SuppressWarnings("ConstantConditions")
    protected ImageButton setImageButton(int resId, Bitmap img) {
        if (img == null)
            throw new IllegalArgumentException("setting image button " + resId + "with null bitmap");
        ImageButton b = (ImageButton) getView().findViewById(resId);
        b.setOnClickListener(this);
        b.setImageBitmap(img);
        return b;
    }

    protected Button setButton(View v, int resId) {
        Button b = (Button) v.findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    @SuppressWarnings("ConstantConditions")
    protected Button setButton(int resId) {
        Button b = (Button) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    @SuppressWarnings("ConstantConditions")
    protected Button setButton(int resId, Typeface font) {
        Button b = (Button) getView().findViewById(resId);
        if (b != null) {
            if (font != null) b.setTypeface(font);
            b.setOnClickListener(this);
        }
        return b;
    }

    @SuppressWarnings("ConstantConditions")
    protected Button getButton(int resId) {
        return (Button) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ToggleButton setToggleButton(int resId) {
        ToggleButton b = (ToggleButton) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    @SuppressWarnings("ConstantConditions")
    public ToggleButton getToggleButton(int resId) {
        return (ToggleButton) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected Spinner setSpinner(int resId, int arrId) {
        Spinner s = (Spinner) getView().findViewById(resId);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        return s;
    }

    @SuppressWarnings("ConstantConditions")
    protected Spinner setSpinner(int resId, int arrId, int sp_it, int sp_dd) {
        Spinner s = (Spinner) getView().findViewById(resId);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId, sp_it);
        adapter.setDropDownViewResource(sp_dd);
        s.setAdapter(adapter);
        return s;
    }

    @SuppressWarnings("ConstantConditions")
    protected Spinner setSpinner(int resId, int arrId, ArrayAdapter adapter) {
        Spinner s = (Spinner) getView().findViewById(resId);
        s.setAdapter(adapter);
        return s;
    }

    @SuppressWarnings("ConstantConditions")
    protected Spinner getSpinner(int resId) {
        return (Spinner) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ListView getListView(int resId) {
        return (ListView) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ListView setListView(int resId, BaseAdapter adapter) {
        ListView l = (ListView) getView().findViewById(resId);
        l.setAdapter(adapter);
        return l;
    }

    @SuppressWarnings("ConstantConditions")
    protected RelativeLayout getRelativeLayout(int resId) {
        return (RelativeLayout) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected LinearLayout getLinearLayout(int resId) {
        return (LinearLayout) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ProgressBar getProgressBar(int resId) {
        return (ProgressBar) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    public SeekBar getSeekBar(int resId) {
        return (SeekBar) getView().findViewById(resId);
    }

    @SuppressWarnings("ConstantConditions")
    protected ViewFlipper getViewFlipper(int resId) {
        return (ViewFlipper) getView().findViewById(resId);
    }

    public void error(String tag, String message) {
        Log.e(tag, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public static void debug(String tag, String message) {
        Log.d(tag, message);
    }

    public void toast(String message) {
    	toast(message, Toast.LENGTH_SHORT);
    }

    public void toast(String message, int length) {
        Toast.makeText(getActivity(), message, length).show();
    }

	protected class ContentDescriptionClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			toast((String) v.getContentDescription(), Toast.LENGTH_LONG);
		}
	}
    
}

