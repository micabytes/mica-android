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
import android.support.annotation.Nullable;
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
  public static final int DEFAULT_SPEED = 300;

  @SuppressWarnings("InterfaceNeverImplemented")
  private interface OnChangedListener {
    void onChanged(NumberPicker picker, int oldVal, int newVal);
  }

  @SuppressWarnings("InterfaceNeverImplemented")
  private interface Formatter {
    String toString(int value);
  }

  private final Handler handler;
  private final Runnable runnable = new Runnable() {
    @Override
    public void run() {
      if (increment) {
        changeCurrent(currentValue + incrementSize);
        handler.postDelayed(this, speed);
      } else if (decrement) {
        changeCurrent(currentValue - incrementSize);
        handler.postDelayed(this, speed);
      }
    }
  };

  private final EditText text;
  private final InputFilter numberInputFilter;

  private String[] displayedValues;
  private int startValue;
  private int endValue;
  private int currentValue;
  private int previous;
  private OnChangedListener listener;
  @SuppressWarnings("unused")
  private Formatter formatter;
  private long speed = DEFAULT_SPEED;

  private boolean increment;
  private boolean decrement;
  private int incrementSize = 1;

  public NumberPicker(Context context) {
    this(context, null);
  }

  public NumberPicker(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  @SuppressWarnings("ThisEscapedInObjectConstruction")
  public NumberPicker(Context context, @Nullable AttributeSet attrs, @SuppressWarnings("UnusedParameters") int defStyle) {
    super(context, attrs);
    setOrientation(VERTICAL);
    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.number_picker, this, true);
    handler = new Handler();
    InputFilter inputFilter = new NumberPickerInputFilter();
    numberInputFilter = new NumberRangeKeyListener();
    incrementButton = findViewById(R.id.increment);
    incrementButton.setOnClickListener(this);
    incrementButton.setOnLongClickListener(this);
    incrementButton.setNumberPicker(this);
    decrementButton = findViewById(R.id.decrement);
    decrementButton.setOnClickListener(this);
    decrementButton.setOnLongClickListener(this);
    decrementButton.setNumberPicker(this);

    text = findViewById(R.id.timepicker_input);
    text.setOnFocusChangeListener(this);
    text.setFilters(new InputFilter[]{inputFilter});
    text.setRawInputType(InputType.TYPE_CLASS_NUMBER);

    if (!isEnabled()) {
      setEnabled(false);
    }

    startValue = DEFAULT_MIN;
    endValue = DEFAULT_MAX;
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    incrementButton.setEnabled(enabled);
    decrementButton.setEnabled(enabled);
    text.setEnabled(enabled);
  }

  @SuppressWarnings("unused")
  public void setListener(OnChangedListener l) {
    listener = l;
  }

  /**
   * Set the range of numbers allowed for the number picker. The currentValue value will be automatically
   * set to the startValue.
   *
   * @param i the i of the range (inclusive)
   */
  @SuppressWarnings("unused")
  public void setRange(int i) {
    GameLog.d(TAG, "Set range of dialog " + 1 + " to " + i);
    startValue = 1;
    endValue = i;
    currentValue = 1;
    updateView();
  }

  /**
   * Set the range of numbers allowed for the number picker. The currentValue value will be automatically
   * set to the startValue. Also provide a mapping for values used to display to the user.
   *
   * @param st the startValue of the range (inclusive)
   * @param ed the endValue of the range (inclusive)
   * @param dv the values displayed to the user.
   */
  @SuppressWarnings({"unused", "MethodCanBeVariableArityMethod", "AssignmentToCollectionOrArrayFieldFromParameter"})
  public void setRange(int st, int ed, @Nullable String[] dv) {
    displayedValues = dv;
    startValue = st;
    endValue = ed;
    currentValue = st;
    updateView();
  }

  @SuppressWarnings("unused")
  public void setIncrementSize(int n) {
    incrementSize = n;
  }

  public void setCurrentValue(int val) {
    currentValue = val;
    updateView();
  }

  /**
   * The speed (in milliseconds) at which the numbers will scroll when the the +/- buttons are
   * longpressed. Default is 300ms.
   */
  public void setSpeed(long spd) {
    speed = spd;
  }

  @Override
  public void onClick(View v) {
    validateInput(text);
    if (!text.hasFocus()) text.requestFocus();
    // now perform the increment/decrement
    if (R.id.increment == v.getId()) {
      changeCurrent(currentValue + incrementSize);
    } else if (R.id.decrement == v.getId()) {
      changeCurrent(currentValue - incrementSize);
    }
  }

  private String formatNumber(int value) {
    return (formatter != null)
        ? formatter.toString(value)
        : String.valueOf(value);
  }

  private void changeCurrent(int c) {
    int cur = c;
    // Wrap around the values if we go past the startValue or endValue
    if (cur > endValue) {
      cur = startValue;
    } else if (cur < startValue) {
      cur = endValue;
    }
    previous = currentValue;
    currentValue = cur;

    notifyChange();
    updateView();
  }

  private void notifyChange() {
    if (listener != null) {
      listener.onChanged(this, previous, currentValue);
    }
  }

  private void updateView() {

    /* If we don't have displayed values then use the
    * currentValue number else find the correct value in the
    * displayed values for the currentValue number.
    */
    text.getText().clear();
    if (displayedValues == null) {
      text.append(formatNumber(currentValue));
    } else {
      text.append(displayedValues[currentValue - startValue]);
    }
    text.setSelection(text.getText().length());
  }

  private void validateCurrentView(CharSequence str) {
    int val = getSelectedPos(str.toString());
    if ((val >= startValue) && (val <= endValue)) {
      if (currentValue != val) {
        previous = currentValue;
        currentValue = val;
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
      if (str != null)
        validateCurrentView(str);
    }
  }

  /**
   * We startValue the long click here but rely on the {@link NumberPickerButton} to inform us when the
   * long click has ended.
   */
  @Override
  public boolean onLongClick(View v) {

        /* The text view may still have focus so clear it's focus which will
         * trigger the on focus changed and any typed values to be pulled.
         */
    text.clearFocus();

    if (R.id.increment == v.getId()) {
      increment = true;
      handler.post(runnable);
    } else if (R.id.decrement == v.getId()) {
      decrement = true;
      handler.post(runnable);
    }
    return true;
  }

  public void cancelIncrement() {
    increment = false;
  }

  public void cancelDecrement() {
    decrement = false;
  }

  private static final char[] DIGIT_CHARACTERS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
  };

  private final NumberPickerButton incrementButton;
  private final NumberPickerButton decrementButton;

  private final class NumberPickerInputFilter implements InputFilter {

    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
      if (displayedValues == null) {
        return numberInputFilter.filter(source, start, end, dest, dstart, dend);
      }
      @NonNls CharSequence filtered = String.valueOf(source.subSequence(start, end));
      @NonNls String result = String.valueOf(dest.subSequence(0, dstart))
          + filtered
          + dest.subSequence(dend, dest.length());
      String str = String.valueOf(result).toLowerCase(Locale.US);
      for (String val : displayedValues) {
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
    @SuppressWarnings("MethodReturnAlwaysConstant")
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
      if (val > endValue) {
        return "";
      }
      return filtered;
    }
  }

  private int getSelectedPos(String str) {
    if (displayedValues == null) {
      try {
        return Integer.parseInt(str);
      }
      catch (NumberFormatException ignored) {
        // Invalid number format
        return startValue;
      }
    }
    for (int i = 0; i < displayedValues.length; i++) {
        /* Don't force the user to type in jan when ja will do */
      String stl = str.toLowerCase(Locale.US);
      if (displayedValues[i].toLowerCase(Locale.US).startsWith(stl)) {
        return startValue + i;
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
    return startValue;
  }

  /**
   * @return the currentValue value.
   */
  public int getCurrentValue() {
    try {
      currentValue = Integer.parseInt(String.valueOf(((TextView) findViewById(R.id.timepicker_input)).getText()));
    } catch (NumberFormatException ignored) {
      return 0;
    }
    return currentValue;
  }

}
