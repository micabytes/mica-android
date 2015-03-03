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

import android.R;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
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

import com.crashlytics.android.Crashlytics;
import com.micabyte.android.util.StringHandler;

import org.jetbrains.annotations.NonNls;

/**
 * Convenience class to replace Fragment
 *
 * @author micabyte
 */
public class BaseFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onClick(View v) {

    }

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
    protected void createFragment() {

    }

    // Used to update the UI elements
    public void updateFragment() {

    }


    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    @Nullable
    protected TextView getTextView(int resId) {
        if (getView() == null) return null;
        return (TextView) getView().findViewById(resId);
    }

    @Nullable
    public TextView setTextView(int resId, Typeface font) {
        if (getView() == null) return null;
        TextView t = (TextView) getView().findViewById(resId);
        if (t != null) {
            if (font != null)
                t.setTypeface(font);
            t.setOnClickListener(this);
        }
        return t;
    }

    @Nullable
    protected EditText getEditText(int resId) {
        if (getView() == null) return null;
        return (EditText) getView().findViewById(resId);
    }

    @Nullable
    protected ImageView getImageView(int resId) {
        if (getView() == null) return null;
        return (ImageView) getView().findViewById(resId);
    }

    @Nullable
    protected ImageView setImageView(int resId) {
        if (getView() == null) return null;
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setOnClickListener(this);
        return v;
    }

    @Nullable
    protected ImageView setImageView(int resId, Bitmap img) {
        if (getView() == null) return null;
        if (img == null)
            throw new IllegalArgumentException("setting image view " + resId + " with null bitmap");
        ImageView v = (ImageView) getView().findViewById(resId);
        v.setImageBitmap(img);
        return v;
    }

    @Nullable
    protected CheckBox getCheckBox(int resId) {
        if (getView() == null) return null;
        return (CheckBox) getView().findViewById(resId);
    }

    @Nullable
    protected RadioButton getRadioButton(int resId) {
        if (getView() == null) return null;
        return (RadioButton) getView().findViewById(resId);
    }

    @Nullable
    protected ImageButton getImageButton(int resId) {
        if (getView() == null) return null;
        return (ImageButton) getView().findViewById(resId);
    }

    @Nullable
    protected ImageButton setImageButton(int resId) {
        if (getView() == null) return null;
        ImageButton button = (ImageButton) getView().findViewById(resId);
        if (button != null) {
            button.setOnClickListener(this);
        }
        return button;
    }

    @Nullable
    protected ImageButton setImageButton(int resId, Bitmap img) {
        if (getView() == null) return null;
        if (img == null)
            throw new IllegalArgumentException("setting image button " + resId + " with null bitmap");
        ImageButton button = (ImageButton) getView().findViewById(resId);
        button.setOnClickListener(this);
        button.setImageBitmap(img);
        return button;
    }

    @Nullable
    protected Button setButton(View v, int resId) {
        if (v == null) return null;
        Button button = (Button) v.findViewById(resId);
        if (button != null) {
            button.setOnClickListener(this);
        }
        return button;
    }

    @Nullable
    protected Button setButton(int resId) {
        if (getView() == null) return null;
        Button button = (Button) getView().findViewById(resId);
        if (button != null) {
            button.setOnClickListener(this);
        }
        return button;
    }

    @Nullable
    protected Button setButton(int resId, Typeface font) {
        if (getView() == null) return null;
        Button button = (Button) getView().findViewById(resId);
        if (button != null) {
            if (font != null) button.setTypeface(font);
            button.setOnClickListener(this);
        }
        return button;
    }

    @Nullable
    protected Button getButton(int resId) {
        if (getView() == null) return null;
        return (Button) getView().findViewById(resId);
    }

    @Nullable
    protected ToggleButton setToggleButton(int resId) {
        if (getView() == null) return null;
        ToggleButton button = (ToggleButton) getView().findViewById(resId);
        if (button != null) {
            button.setOnClickListener(this);
        }
        return button;
    }

    @Nullable
    public ToggleButton getToggleButton(int resId) {
        if (getView() == null) return null;
        return (ToggleButton) getView().findViewById(resId);
    }

    @Nullable
    protected Spinner setSpinner(int resId, int arrId) {
        if (getView() == null) return null;
        Spinner spinner = (Spinner) getView().findViewById(resId);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId,
                        R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Nullable
    protected Spinner setSpinner(int resId, int arrId, int spIt, int spDd) {
        if (getView() == null) return null;
        Spinner spinner = (Spinner) getView().findViewById(resId);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId, spIt);
        adapter.setDropDownViewResource(spDd);
        spinner.setAdapter(adapter);
        return spinner;
    }

    @Nullable
    protected Spinner getSpinner(int resId) {
        if (getView() == null) return null;
        return (Spinner) getView().findViewById(resId);
    }

    @Nullable
    protected ListView getListView(int resId) {
        if (getView() == null) return null;
        return (ListView) getView().findViewById(resId);
    }

    @Nullable
    protected ListView setListView(int resId, BaseAdapter adapter) {
        if (getView() == null) return null;
        ListView listView = (ListView) getView().findViewById(resId);
        listView.setAdapter(adapter);
        return listView;
    }

    @Nullable
    protected RelativeLayout getRelativeLayout(int resId) {
        if (getView() == null) return null;
        return (RelativeLayout) getView().findViewById(resId);
    }

    @Nullable
    protected LinearLayout getLinearLayout(int resId) {
        if (getView() == null) return null;
        return (LinearLayout) getView().findViewById(resId);
    }

    @Nullable
    protected ProgressBar getProgressBar(int resId) {
        if (getView() == null) return null;
        return (ProgressBar) getView().findViewById(resId);
    }

    @Nullable
    public SeekBar getSeekBar(int resId) {
        if (getView() == null) return null;
        return (SeekBar) getView().findViewById(resId);
    }

    @Nullable
    protected ViewFlipper getViewFlipper(int resId) {
        if (getView() == null) return null;
        return (ViewFlipper) getView().findViewById(resId);
    }

    public void error(String tag, String message) {
        Crashlytics.log(Log.ERROR, tag, message);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    public static void debug(String tag, String message) {
        Log.d(tag, message);
    }

    public void toast(@NonNls String message) {
        toast(message, Toast.LENGTH_SHORT);
    }

    public void toast(String message, int length) {
        Toast.makeText(getActivity(), message, length).show();
    }

    @SuppressWarnings("PublicInnerClass")
    public class ContentDescriptionClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            toast((String) v.getContentDescription(), Toast.LENGTH_LONG);
        }
    }

    public String text(int id) {
        return StringHandler.get(getActivity(), id);
    }

    @SuppressWarnings("OverloadedVarargsMethod")
    public String text(int id, Object... args) {
        return StringHandler.get(getActivity(), id, args);
    }

}
