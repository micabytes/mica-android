/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.micabytes.gui;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.micabytes.R;
import com.micabytes.util.GameLog;

import org.jetbrains.annotations.NonNls;

import java.util.Locale;

/**
 * This class has been pulled from the Android platform source code, its an internal widget that
 * hasn't been made public so its included in the project in this fashion for use with the
 * preferences screen; I have made  a few slight modifications to the code here,
 *
 * @author micabyte
 */
public final class NumberPicker extends LinearLayout implements OnClickListener,
    OnFocusChangeListener, OnLongClickListener {
  private static final String TAG = NumberPicker.class.getName();
  private static final int DEFAULT_MAX = 200;
  private static final int DEFAULT_MIN = 0;

  @SuppressWarnings("InterfaceNeverImplemented")
  private interface OnChangedListener {
    void onChanged(NumberPicker picker, int oldVal, int newVal);
  }

  @SuppressWarnings("InterfaceNeverImplemented")
  private interface Formatter {
    String toString(int value);
  }

  private final Handler mHandler;
  private final Runnable mRunnable = new Runnable() {
    @Override
    public void run() {
      if (mIncrement) {
        changeCurrent(mCurrent + mIncrementSize);
        mHandler.postDelayed(this, mSpeed);
      } else if (mDecrement) {
        changeCurrent(mCurrent - mIncrementSize);
        mHandler.postDelayed(this, mSpeed);
      }
    }
  };

  private final EditText mText;
  private final InputFilter mNumberInputFilter;

  private String[] mDisplayedValues;
  private int mStart;
  private int mEnd;
  private int mCurrent;
  private int mPrevious;
  private OnChangedListener mListener;
  @SuppressWarnings("unused")
  private Formatter formatter;
  private long mSpeed = 300;

  private boolean mIncrement;
  private boolean mDecrement;
  private int mIncrementSize = 1;

  public NumberPicker(Context context) {
    this(context, null);
  }

  public NumberPicker(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  /**
   * @param defStyle Default style. Not used.
   */
  @SuppressWarnings("UnusedParameters")
  public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs);
    setOrientation(VERTICAL);
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.number_picker, this, true);
    mHandler = new Handler();
    InputFilter inputFilter = new NumberPickerInputFilter();
    mNumberInputFilter = new NumberRangeKeyListener();
    mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
    mIncrementButton.setOnClickListener(this);
    mIncrementButton.setOnLongClickListener(this);
    mIncrementButton.setNumberPicker(this);
    mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
    mDecrementButton.setOnClickListener(this);
    mDecrementButton.setOnLongClickListener(this);
    mDecrementButton.setNumberPicker(this);

    mText = (EditText) findViewById(R.id.timepicker_input);
    mText.setOnFocusChangeListener(this);
    mText.setFilters(new InputFilter[]{inputFilter});
    mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);

    if (!isEnabled()) {
      setEnabled(false);
    }

    mStart = DEFAULT_MIN;
    mEnd = DEFAULT_MAX;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mIncrementButton.setEnabled(enabled);
    mDecrementButton.setEnabled(enabled);
    mText.setEnabled(enabled);
  }

  @SuppressWarnings("unused")
  public void setOnChangeListener(OnChangedListener listener) {
    mListener = listener;
  }

  /**
   * Set the range of numbers allowed for the number picker. The current value will be automatically
   * set to the start.
   *
   * @param end   the end of the range (inclusive)
   */
  public void setRange(int end) {
    GameLog.d(TAG, "Set range of dialog " + 1 + " to " + end);
    mStart = 1;
    mEnd = end;
    mCurrent = 1;
    updateView();
  }

  /**
   * Set the range of numbers allowed for the number picker. The current value will be automatically
   * set to the start. Also provide a mapping for values used to display to the user.
   *
   * @param start           the start of the range (inclusive)
   * @param end             the end of the range (inclusive)
   * @param displayedValues the values displayed to the user.
   */
  @SuppressWarnings({"unused", "MethodCanBeVariableArityMethod"})
  public void setRange(int start, int end, String[] displayedValues) {
    mDisplayedValues = displayedValues;
    mStart = start;
    mEnd = end;
    mCurrent = start;
    updateView();
  }

  @SuppressWarnings("unused")
  public void setIncrementSize(int n) {
    mIncrementSize = n;
  }

  public void setCurrent() {
    mCurrent = 1;
    updateView();
  }

  /**
   * The speed (in milliseconds) at which the numbers will scroll when the the +/- buttons are
   * longpressed. Default is 300ms.
   */
  public void setSpeed(long speed) {
    mSpeed = speed;
  }

  @Override
  public void onClick(View v) {
    validateInput(mText);
    if (!mText.hasFocus()) mText.requestFocus();
    // now perform the increment/decrement
    if (R.id.increment == v.getId()) {
      changeCurrent(mCurrent + mIncrementSize);
    } else if (R.id.decrement == v.getId()) {
      changeCurrent(mCurrent - mIncrementSize);
    }
  }

  private String formatNumber(int value) {
    return (formatter != null)
        ? formatter.toString(value)
        : String.valueOf(value);
  }

  private void changeCurrent(int current) {
    int cur = current;
    // Wrap around the values if we go past the start or end
    if (cur > mEnd) {
      cur = mStart;
    } else if (cur < mStart) {
      cur = mEnd;
    }
    mPrevious = mCurrent;
    mCurrent = cur;

    notifyChange();
    updateView();
  }

  private void notifyChange() {
    if (mListener != null) {
      mListener.onChanged(this, mPrevious, mCurrent);
    }
  }

  private void updateView() {

        /* If we don't have displayed values then use the
         * current number else find the correct value in the
         * displayed values for the current number.
         */
    if (mDisplayedValues == null) {
      mText.setText(formatNumber(mCurrent));
    } else {
      mText.setText(mDisplayedValues[mCurrent - mStart]);
    }
    mText.setSelection(mText.getText().length());
  }

  private void validateCurrentView(CharSequence str) {
    int val = getSelectedPos(str.toString());
    if ((val >= mStart) && (val <= mEnd)) {
      if (mCurrent != val) {
        mPrevious = mCurrent;
        mCurrent = val;
        notifyChange();
      }
    }
    updateView();
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {

        /* When focus is lost check that the text field
         * has valid values.
         */
    if (!hasFocus) {
      validateInput(v);
    }
  }

  private void validateInput(View v) {
    @NonNls String str = String.valueOf(((TextView) v).getText());
    if (str != null && str.isEmpty()) {

      // Restore to the old value as we don't allow empty values
      updateView();
    } else {

      // Check the new value and ensure it's in range
      validateCurrentView(str);
    }
  }

  /**
   * We start the long click here but rely on the {@link NumberPickerButton} to inform us when the
   * long click has ended.
   */
  @Override
  public boolean onLongClick(View v) {

        /* The text view may still have focus so clear it's focus which will
         * trigger the on focus changed and any typed values to be pulled.
         */
    mText.clearFocus();

    if (R.id.increment == v.getId()) {
      mIncrement = true;
      mHandler.post(mRunnable);
    } else if (R.id.decrement == v.getId()) {
      mDecrement = true;
      mHandler.post(mRunnable);
    }
    return true;
  }

  public void cancelIncrement() {
    mIncrement = false;
  }

  public void cancelDecrement() {
    mDecrement = false;
  }

  private static final char[] DIGIT_CHARACTERS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
  };

  private final NumberPickerButton mIncrementButton;
  private final NumberPickerButton mDecrementButton;

  private final class NumberPickerInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
      if (mDisplayedValues == null) {
        return mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
      }
      @NonNls CharSequence filtered = String.valueOf(source.subSequence(start, end));
      @NonNls String result = String.valueOf(dest.subSequence(0, dstart))
          + filtered
          + dest.subSequence(dend, dest.length());
      String str = String.valueOf(result).toLowerCase(Locale.US);
      for (String val : mDisplayedValues) {
        val = val.toLowerCase(Locale.US);
        if (val.startsWith(str)) {
          return filtered;
        }
      }
      return "";
    }
  }

  private final class NumberRangeKeyListener extends NumberKeyListener {

    // This doesn't allow for range limits when controlled by a
    // soft input method!
    @Override
    public int getInputType() {
      return InputType.TYPE_CLASS_NUMBER;
    }

    @Override
    protected char[] getAcceptedChars() {
      return DIGIT_CHARACTERS;
    }

    @Override
    public CharSequence filter(@NonNull CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {

      @NonNls CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
      if (filtered == null) {
        filtered = source.subSequence(start, end);
      }

      @NonNls String result = String.valueOf(dest.subSequence(0, dstart))
          + filtered
          + dest.subSequence(dend, dest.length());

      if (result.isEmpty()) {
        return result;
      }
      int val = getSelectedPos(result);

            /* Ensure the user can't type in a value greater
             * than the max allowed. We have to allow less than min
             * as the user might want to delete some numbers
             * and then type a new number.
             */
      if (val > mEnd) {
        return "";
      }
      return filtered;
    }
  }

  private int getSelectedPos(String str) {
    if (mDisplayedValues == null) {
      return Integer.parseInt(str);
    }
    for (int i = 0; i < mDisplayedValues.length; i++) {
        /* Don't force the user to type in jan when ja will do */
      String stl = str.toLowerCase(Locale.US);
      if (mDisplayedValues[i].toLowerCase(Locale.US).startsWith(stl)) {
        return mStart + i;
      }
    }

		/* The user might have typed in a number into the month field i.e.
     * 10 instead of OCT so support that too.
		 */
    try {
      return Integer.parseInt(str);
    } catch (NumberFormatException ignored) {

		    /* Ignore as if it's not a number we don't care */
    }
    return mStart;
  }

  /**
   * @return the current value.
   */
  public int getCurrent() {
    try {
      mCurrent = Integer.parseInt(String.valueOf(((TextView) findViewById(R.id.timepicker_input)).getText()));
    } catch (NumberFormatException ignored) {
      return 0;
    }
    return mCurrent;
  }
}
