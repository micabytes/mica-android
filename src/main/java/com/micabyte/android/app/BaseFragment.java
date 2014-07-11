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
public abstract class BaseFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onActivityCreated(Bundle saved) {
        super.onActivityCreated(saved);
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
	
    public TextView getTextView(int resId) {
        return (TextView) getView().findViewById(resId);
    }

    public TextView setTextView(int resId, Typeface font) {
        TextView t = (TextView) getView().findViewById(resId);
        if (t != null) {
        	if (font != null)
        		t.setTypeface(font);
            t.setOnClickListener(this);
        }
        return t;
    }

    protected EditText getEditText(int resId) {
        return (EditText) getView().findViewById(resId);
    }

    protected ImageView getImageView(int resId) {
        return (ImageView) getView().findViewById(resId);
    }

    protected ImageView setImageView(int resId) {
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setOnClickListener(this);
        return v;
    }

    protected ImageView setImageView(int resId, Bitmap img) {
        if (img == null)
            throw new IllegalArgumentException("setting image view " + resId + "with null bitmap");
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setImageBitmap(img);
        return v;
    }

    protected CheckBox getCheckBox(int resId_) {
        CheckBox b = (CheckBox) getView().findViewById(resId_);
        return b;
    }

    protected RadioButton getRadioButton(int resId_) {
    	RadioButton b = (RadioButton) getView().findViewById(resId_);
        return b;
    }

    protected ImageButton getImageButton(int resId) {
        return (ImageButton) getView().findViewById(resId);
    }

    protected ImageButton setImageButton(int resId) {
        ImageButton b = (ImageButton) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

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

    protected Button setButton(int resId) {
        Button b = (Button) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    protected Button setButton(int resId, Typeface font) {
        Button b = (Button) getView().findViewById(resId);
        if (b != null) {
            if (font != null) b.setTypeface(font);
            b.setOnClickListener(this);
        }
        return b;
    }

    protected Button getButton(int resId) {
        return (Button) getView().findViewById(resId);
    }

    protected ToggleButton setToggleButton(int resId) {
        ToggleButton b = (ToggleButton) getView().findViewById(resId);
        if (b != null) {
            b.setOnClickListener(this);
        }
        return b;
    }

    public ToggleButton getToggleButton(int resId) {
        return (ToggleButton) getView().findViewById(resId);
    }

    protected Spinner setSpinner(int resId, int arrId) {
        Spinner s = (Spinner) getView().findViewById(resId);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        return s;
    }

    protected Spinner getSpinner(int resId) {
        Spinner s = (Spinner) getView().findViewById(resId);
        return s;
    }

    protected ListView getListView(int resId) {
        ListView l = (ListView) getView().findViewById(resId);
        return l;
    }

    protected ListView setListView(int resId, BaseAdapter adapter) {
        ListView l = (ListView) getView().findViewById(resId);
        l.setAdapter(adapter);
        return l;
    }

    protected RelativeLayout getRelativeLayout(int resId) {
        RelativeLayout l = (RelativeLayout) getView().findViewById(resId);
        return l;
    }

    protected LinearLayout getLinearLayout(int resId) {
        LinearLayout l = (LinearLayout) getView().findViewById(resId);
        return l;
    }

    protected ProgressBar getProgressBar(int resId) {
        ProgressBar p = (ProgressBar) getView().findViewById(resId);
        return p;
    }

    public SeekBar getSeekBar(int resId) {
        SeekBar p = (SeekBar) getView().findViewById(resId);
        return p;
    }

    protected ViewFlipper getViewFlipper(int resId) {
        ViewFlipper l = (ViewFlipper) getView().findViewById(resId);
        return l;
    }

    public void error(String tag, String message) {
        Log.e(tag, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public static void debug(String tag, String message) {
        Log.d(tag, message);
    }

    public void toast(String message) {
    	toast(message, Toast.LENGTH_LONG);
    }

    public void toast(String message, int length) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

	public class ContentDescriptionClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			Toast.makeText(getActivity(), v.getContentDescription(), Toast.LENGTH_LONG).show();
		}
	}
    
}
