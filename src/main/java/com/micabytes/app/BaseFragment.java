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

import android.content.Context;
import android.content.Intent;
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

import com.micabytes.GameApplication;
import com.micabytes.gui.LinearListView;
import com.micabytes.util.GameLog;
import com.micabytes.util.UIObjectNotFoundException;

import org.jetbrains.annotations.NonNls;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Convenience class to replace Fragment
 */
@SuppressWarnings({"unused", "ClassWithTooManyMethods", "OverlyComplexClass"})
public class BaseFragment extends Fragment implements View.OnClickListener {
  @NonNls public static final String COULD_NOT_FIND_THE_ROOT_VIEW = "Could not find the root view";
  @NonNls public static final String COULD_NOT_FIND_RES_ID = "Could not find resId ";
  @NonNls public static final String IN_FIND_VIEW_BY_ID = " in findViewById";


  @Override
  public void onClick(View view) {
    // NOOP
  }

  @Override
  public void onResume() {
    super.onResume();
    createFragment();
    updateFragment();
  }

  @SuppressWarnings("ProhibitedExceptionCaught")
  @Override
  public void startActivityForResult(Intent intent, int requestCode) {
    try {
      super.startActivityForResult(intent, requestCode);
    } catch (NullPointerException e) {
      String pkg = intent.getPackage();
      if (pkg != null)
        GameLog.e("BaseFragment", "Ignoring startActivityForResult exception on intent " + pkg);
      GameLog.logException(e);
    }
  }

  // Create Fragment UI
  protected void createFragment() {
    // NOOP
  }

  // Update Fragment UI
  public void updateFragment() {
    // NOOP
  }

  // Change Fragment
  public void resetFragment() {
    getBaseActivity().setFragment();
  }

  protected BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }

  protected void showErrorMessage(String msg) {
    Toast.makeText(GameApplication.getInstance(), msg, Toast.LENGTH_LONG).show();
  }

  @NonNull
  protected View getView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    View v = root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  protected void setView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    View v = root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    v.setOnClickListener(this);
  }

  @NonNull
  protected TextView getTextView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    TextView v = (TextView) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected TextView setTextView(int resId, Typeface font) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    TextView t = (TextView) root.findViewById(resId);
    if (t == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    if (font != null)
      t.setTypeface(font);
    t.setOnClickListener(this);
    return t;
  }

  @NonNull
  protected EditText getEditText(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    EditText v = (EditText) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected ImageView getImageView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected ImageView setImageView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    v.setOnClickListener(this);
    return v;
  }

  @NonNull
  protected ImageView setImageView(int resId, Bitmap img) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    if (img == null)
      throw new IllegalArgumentException("setting image view " + resId + " with no or null bitmap");
    ImageView v = (ImageView) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    v.setImageBitmap(img);
    return v;
  }

  @NonNull
  protected CircleImageView getRoundedImageView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    CircleImageView v = (CircleImageView) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected CheckBox getCheckBox(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    CheckBox v = (CheckBox) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @Nullable
  protected RadioButton getRadioButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    RadioButton v = (RadioButton) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected ImageButton getImageButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return button;
  }

  @NonNull
  protected ImageButton setImageButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected ImageButton setImageButton(int resId, Bitmap img) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    if (img == null)
      throw new IllegalArgumentException("setting image button " + resId + " with null bitmap");
    ImageButton button = (ImageButton) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    button.setOnClickListener(this);
    button.setImageBitmap(img);
    return button;
  }

  @NonNull
  protected Button setButton(@NonNull View v, int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected Button setButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException("No getView in setButton(int)");
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected Button setButton(int resId, Typeface font) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    if (font != null) button.setTypeface(font);
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  protected Button getButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    Button button = (Button) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return button;
  }

  @NonNull
  protected ToggleButton setToggleButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ToggleButton button = (ToggleButton) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    button.setOnClickListener(this);
    return button;
  }

  @NonNull
  public ToggleButton getToggleButton(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ToggleButton button = (ToggleButton) root.findViewById(resId);
    if (button == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return button;
  }

  @NonNull
  protected Spinner setSpinner(int resId, int arrId, int spIt, int spDd) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    Spinner spinner = (Spinner) root.findViewById(resId);
    if (spinner == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(getActivity().getApplicationContext(), arrId, spIt);
    adapter.setDropDownViewResource(spDd);
    spinner.setAdapter(adapter);
    return spinner;
  }

  @NonNull
  protected Spinner getSpinner(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    Spinner spinner = (Spinner) root.findViewById(resId);
    if (spinner == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return spinner;
  }

  @NonNull
  protected ListView getListView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ListView listView = (ListView) root.findViewById(resId);
    if (listView == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return listView;
  }

  @NonNull
  protected ListView setListView(int resId, BaseAdapter adapter) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ListView listView = (ListView) root.findViewById(resId);
    if (listView == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    listView.setAdapter(adapter);
    return listView;
  }

  @NonNull
  protected LinearListView getLinearListView(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    LinearListView listView = (LinearListView) root.findViewById(resId);
    if (listView == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return listView;
  }

  @NonNull
  protected LinearListView setLinearListView(int resId, BaseAdapter adapter) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    LinearListView listView = (LinearListView) root.findViewById(resId);
    if (listView == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    listView.setAdapter(adapter);
    return listView;
  }

  @NonNull
  protected RelativeLayout getRelativeLayout(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    RelativeLayout v = (RelativeLayout) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected LinearLayout getLinearLayout(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    LinearLayout v = (LinearLayout) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected ProgressBar getProgressBar(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ProgressBar v = (ProgressBar) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  public SeekBar getSeekBar(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    SeekBar v = (SeekBar) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  @NonNull
  protected ViewFlipper getViewFlipper(int resId) throws UIObjectNotFoundException {
    View root = getView();
    if (root == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_THE_ROOT_VIEW);
    ViewFlipper v = (ViewFlipper) root.findViewById(resId);
    if (v == null)
      throw new UIObjectNotFoundException(COULD_NOT_FIND_RES_ID + resId + IN_FIND_VIEW_BY_ID);
    return v;
  }

  public class ContentDescriptionClickListener implements View.OnClickListener {

    private void toast(String message, int length) {
      Toast.makeText(getActivity(), message, length).show();
    }

    @Override
    public void onClick(View view) {
      toast((String) view.getContentDescription(), Toast.LENGTH_LONG);
    }

  }

}
