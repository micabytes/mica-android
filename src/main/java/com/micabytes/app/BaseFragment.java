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
package com.micabytes.app;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.makeramen.roundedimageview.RoundedImageView;
import com.micabytes.gui.LinearListView;

/**
 * Convenience class to replace Fragment
 */
@SuppressWarnings({"unused", "ClassWithTooManyMethods"})
public class BaseFragment extends Fragment implements View.OnClickListener {

  @Override
  public void onClick(View v) {
    // NOOP
  }

  @Override
  public void onResume() {
    super.onResume();
    createFragment();
    updateFragment();
  }

  // Used to set up UI elements
  protected void createFragment() {
    // NOOP
  }

  // Used to update the UI elements
  public void updateFragment() {
    // NOOP
  }

  public void resetFragment() {
    getBaseActivity().setFragment();
  }

  @NonNull
  protected BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  protected View getView(int resId) {
    View root = getView();
    assert root != null;
    View v = root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getView(int)");
    return v;
  }

  protected void setView(int resId) {
    View root = getView();
    assert root != null;
    View v = root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("setView(int)");
    v.setOnClickListener(this);
  }

  protected TextView getTextView(int resId) {
    View root = getView();
    assert root != null;
    TextView v = (TextView) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getTextView");
    return v;
  }

  @Nullable
  protected TextView setTextView(int resId, Typeface font) {
    View root = getView();
    assert root != null;
    TextView t = (TextView) root.findViewById(resId);
    if (t == null)
      throw new Resources.NotFoundException("setTextView");
    if (font != null)
      t.setTypeface(font);
    t.setOnClickListener(this);
    return t;
  }

  protected EditText getEditText(int resId) {
    View root = getView();
    assert root != null;
    EditText v = (EditText) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getEditText");
    return v;
  }

  protected ImageView getImageView(int resId) {
    View root = getView();
    assert root != null;
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getImageView");
    return v;
  }

  protected ImageView setImageView(int resId) {
    View root = getView();
    assert root != null;
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("setImageView");
    v.setOnClickListener(this);
    return v;
  }

  protected ImageView setImageView(int resId, Bitmap img) {
    View root = getView();
    assert root != null;
    if (img == null)
      throw new IllegalArgumentException("setting image view " + resId + " with no or null bitmap");
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("setImageView(int, Bitmap)");
    v.setImageBitmap(img);
    return v;
  }

  protected RoundedImageView getRoundedImageView(int resId) {
    View root = getView();
    assert root != null;
    RoundedImageView v = (RoundedImageView) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getRoundedImageView");
    return v;
  }

  protected CheckBox getCheckBox(int resId) {
    View root = getView();
    assert root != null;
    CheckBox v = (CheckBox) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getCheckBox");
    return v;
  }

  @Nullable
  protected RadioButton getRadioButton(int resId) {
    View root = getView();
    assert root != null;
    return (RadioButton) root.findViewById(resId);
  }

  @NonNull
  protected ImageButton getImageButton(int resId) {
    View root = getView();
    assert root != null;
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("getImageButton");
    return button;
  }

  @NonNull
  protected ImageButton setImageButton(int resId) {
    View root = getView();
    assert root != null;
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setImageButton(int)");
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected ImageButton setImageButton(int resId, Bitmap img) {
    View root = getView();
    assert root != null;
    if (img == null)
      throw new IllegalArgumentException("setting image button " + resId + " with null bitmap");
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setImageButton(int, Bitmap)");
    button.setOnClickListener(this);
    button.setImageBitmap(img);
    return button;
  }

  @NonNull
  protected Button setButton(@NonNull View v, int resId) {
    Button button = (Button) v.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setButton(View, int)");
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected Button setButton(int resId) {
    View root = getView();
    assert root != null;
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setButton(int)");
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected Button setButton(int resId, Typeface font) {
    View root = getView();
    assert root != null;
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setButton(int, Typeface)");
    if (font != null) button.setTypeface(font);
    button.setOnClickListener(this);
    return button;
  }

  protected Button getButton(int resId) {
    View root = getView();
    assert root != null;
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("getButton");
    return button;
  }

  @NonNull
  protected ToggleButton setToggleButton(int resId) {
    View root = getView();
    assert root != null;
    ToggleButton button = (ToggleButton) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("setToggleButton");
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  public ToggleButton getToggleButton(int resId) {
    View root = getView();
    assert root != null;
    ToggleButton button = (ToggleButton) root.findViewById(resId);
    if (button == null)
      throw new Resources.NotFoundException("getToggleButton");
    return button;
  }

  @NonNull
  protected Spinner setSpinner(int resId, int arrId, int spIt, int spDd) {
    View root = getView();
    assert root != null;
    Spinner spinner = (Spinner) root.findViewById(resId);
    if (spinner == null)
      throw new Resources.NotFoundException("setSpinner");
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId, spIt);
    adapter.setDropDownViewResource(spDd);
    spinner.setAdapter(adapter);
    return spinner;
  }

  @NonNull
  protected Spinner getSpinner(int resId) {
    View root = getView();
    assert root != null;
    Spinner spinner = (Spinner) root.findViewById(resId);
    if (spinner == null)
      throw new Resources.NotFoundException("getSpinner");
    return spinner;
  }

  @NonNull
  protected ListView getListView(int resId) {
    View root = getView();
    assert root != null;
    ListView listView = (ListView) root.findViewById(resId);
    if (listView == null)
      throw new Resources.NotFoundException("setListView(int)");
    return listView;
  }

  @NonNull
  protected ListView setListView(int resId, BaseAdapter adapter) {
    View root = getView();
    assert root != null;
    ListView listView = (ListView) root.findViewById(resId);
    if (listView == null)
      throw new Resources.NotFoundException("setListView(int, BaseAdapter)");
    listView.setAdapter(adapter);
    return listView;
  }

  @NonNull
  protected LinearListView getLinearListView(int resId) {
    View root = getView();
    assert root != null;
    LinearListView listView = (LinearListView) root.findViewById(resId);
    if (listView == null)
      throw new Resources.NotFoundException("getLinearListView()");
    return listView;
  }

  @NonNull
  protected LinearListView setLinearListView(int resId, BaseAdapter adapter) {
    View root = getView();
    assert root != null;
    LinearListView listView = (LinearListView) root.findViewById(resId);
    if (listView == null)
      throw new Resources.NotFoundException("setLinearListView");
    listView.setAdapter(adapter);
    return listView;
  }

  @NonNull
  protected RelativeLayout getRelativeLayout(int resId) {
    View root = getView();
    assert root != null;
    RelativeLayout v = (RelativeLayout) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getRelativeLayout");
    return v;
  }

  @NonNull
  protected LinearLayout getLinearLayout(int resId) {
    View root = getView();
    assert root != null;
    LinearLayout v = (LinearLayout) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getLinearLayout");
    return v;
  }

  @NonNull
  protected ProgressBar getProgressBar(int resId) {
    View root = getView();
    assert root != null;
    ProgressBar v = (ProgressBar) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getProgressBar");
    return v;
  }

  @NonNull
  public SeekBar getSeekBar(int resId) {
    View root = getView();
    assert root != null;
    SeekBar v = (SeekBar) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getSeekBar");
    return v;
  }

  @NonNull
  protected ViewFlipper getViewFlipper(int resId) {
    View root = getView();
    assert root != null;
    ViewFlipper v = (ViewFlipper) root.findViewById(resId);
    if (v == null)
      throw new Resources.NotFoundException("getViewFlipper");
    return v;
  }

  public class ContentDescriptionClickListener implements View.OnClickListener {

    private void toast(String message, int length) {
      Toast.makeText(getActivity(), message, length).show();
    }

    @Override
    public void onClick(View v) {
      toast((String) v.getContentDescription(), Toast.LENGTH_LONG);
    }

  }

}
