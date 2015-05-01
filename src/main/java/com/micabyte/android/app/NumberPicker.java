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

package com.micabyte.android.app;

import java.util.Locale;

import android.content.Context;
import android.os.Handler;
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

import com.micabyte.android.R;

/**
 * This class has been pulled from the Android platform source code, its an internal widget that hasn't been
 * made public so its included in the project in this fashion for use with the preferences screen; I have made
 * a few slight modifications to the code here, I simply put a MAX and MIN default in the code but these values
 * can still be set publically by calling code.
 *
 * @author Google
 */
public class NumberPicker extends LinearLayout implements OnClickListener,
        OnFocusChangeListener, OnLongClickListener {

	// private static final String TAG = "NumberPicker";
    private static final int DEFAULT_MAX = 200;
    private static final int DEFAULT_MIN = 0;

    public interface OnChangedListener {
        void onChanged(NumberPicker picker, int oldVal, int newVal);
    }

    public interface Formatter {
        String toString(int value);
    }

    /*
     * Use a custom NumberPicker formatting callback to use two-digit
     * minutes strings like "01".  Keeping a static formatter etc. is the
     * most efficient way to do this; it avoids creating temporary objects
     * on every call to format().
     */
    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER =
            new NumberPicker.Formatter() {
                final StringBuilder mBuilder = new StringBuilder();
                final java.util.Formatter mFmt = new java.util.Formatter(this.mBuilder);
                final Object[] mArgs = new Object[1];
                @Override
				public String toString(int value) {
                    this.mArgs[0] = value;
                    this.mBuilder.delete(0, this.mBuilder.length());
                    this.mFmt.format("%02d", this.mArgs);
                    return this.mFmt.toString();
                }
        };

    final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        @Override
		public void run() {
            if (NumberPicker.this.mIncrement) {
                changeCurrent(NumberPicker.this.mCurrent + NumberPicker.this.mIncrementSize);
                NumberPicker.this.mHandler.postDelayed(this, NumberPicker.this.mSpeed);
            } else if (NumberPicker.this.mDecrement) {
                changeCurrent(NumberPicker.this.mCurrent - NumberPicker.this.mIncrementSize);
                NumberPicker.this.mHandler.postDelayed(this, NumberPicker.this.mSpeed);
            }
        }
    };

    private final EditText mText;
    final InputFilter mNumberInputFilter;

    String[] mDisplayedValues;
    protected int mStart;
    protected int mEnd;
    protected int mCurrent;
    protected int mPrevious;
    private OnChangedListener mListener;
    private Formatter mFormatter;
    long mSpeed = 300;

    boolean mIncrement;
    boolean mDecrement;
    protected int mIncrementSize = 1;

    public NumberPicker(Context context) {
        this(context, null);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
	 * @param defStyle Default style. Not used. 
	 */
    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker, this, true);
        this.mHandler = new Handler();
        InputFilter inputFilter = new NumberPickerInputFilter();
        this.mNumberInputFilter = new NumberRangeKeyListener();
        this.mIncrementButton = (NumberPickerButton) findViewById(R.id.increment);
        this.mIncrementButton.setOnClickListener(this);
        this.mIncrementButton.setOnLongClickListener(this);
        this.mIncrementButton.setNumberPicker(this);
        this.mDecrementButton = (NumberPickerButton) findViewById(R.id.decrement);
        this.mDecrementButton.setOnClickListener(this);
        this.mDecrementButton.setOnLongClickListener(this);
        this.mDecrementButton.setNumberPicker(this);

        this.mText = (EditText) findViewById(R.id.timepicker_input);
        this.mText.setOnFocusChangeListener(this);
        this.mText.setFilters(new InputFilter[] {inputFilter});
        this.mText.setRawInputType(InputType.TYPE_CLASS_NUMBER);

        if (!isEnabled()) {
            setEnabled(false);
        }

        this.mStart = DEFAULT_MIN;
        this.mEnd = DEFAULT_MAX;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mIncrementButton.setEnabled(enabled);
        this.mDecrementButton.setEnabled(enabled);
        this.mText.setEnabled(enabled);
    }

    public void setOnChangeListener(OnChangedListener listener) {
        this.mListener = listener;
    }

    public void setFormatter(Formatter formatter) {
        this.mFormatter = formatter;
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     */
    public void setRange(int start, int end) {
        this.mStart = start;
        this.mEnd = end;
        this.mCurrent = start;
        updateView();
    }

    /**
     * Set the range of numbers allowed for the number picker. The current
     * value will be automatically set to the start. Also provide a mapping
     * for values used to display to the user.
     *
     * @param start the start of the range (inclusive)
     * @param end the end of the range (inclusive)
     * @param displayedValues the values displayed to the user.
     */
    public void setRange(int start, int end, String[] displayedValues) {
        this.mDisplayedValues = displayedValues;
        this.mStart = start;
        this.mEnd = end;
        this.mCurrent = start;
        updateView();
    }
    
    public void setIncrementSize(int n) {
    	this.mIncrementSize = n;
    }

    public void setCurrent(int current) {
        this.mCurrent = current;
        updateView();
    }

    /**
     * The speed (in milliseconds) at which the numbers will scroll
     * when the the +/- buttons are longpressed. Default is 300ms.
     */
    public void setSpeed(long speed) {
        this.mSpeed = speed;
    }

    @Override
	public void onClick(View v) {
        validateInput(this.mText);
        if (!this.mText.hasFocus()) this.mText.requestFocus();
        // now perform the increment/decrement
        if (R.id.increment == v.getId()) {
            changeCurrent(this.mCurrent + this.mIncrementSize);
        } else if (R.id.decrement == v.getId()) {
            changeCurrent(this.mCurrent - this.mIncrementSize);
        }
    }

    private String formatNumber(int value) {
        return (this.mFormatter != null)
                ? this.mFormatter.toString(value)
                : String.valueOf(value);
    }

    protected void changeCurrent(int current) {
    	int cur = current;
        // Wrap around the values if we go past the start or end
        if (cur > this.mEnd) {
            cur = this.mStart;
        } else if (cur < this.mStart) {
            cur = this.mEnd;
        }
        this.mPrevious = this.mCurrent;
        this.mCurrent = cur;

        notifyChange();
        updateView();
    }

    protected void notifyChange() {
        if (this.mListener != null) {
            this.mListener.onChanged(this, this.mPrevious, this.mCurrent);
        }
    }

    protected void updateView() {

        /* If we don't have displayed values then use the
         * current number else find the correct value in the
         * displayed values for the current number.
         */
        if (this.mDisplayedValues == null) {
            this.mText.setText(formatNumber(this.mCurrent));
        } else {
            this.mText.setText(this.mDisplayedValues[this.mCurrent - this.mStart]);
        }
        this.mText.setSelection(this.mText.getText().length());
    }

    private void validateCurrentView(CharSequence str) {
        int val = getSelectedPos(str.toString());
        if ((val >= this.mStart) && (val <= this.mEnd)) {
            if (this.mCurrent != val) {
                this.mPrevious = this.mCurrent;
                this.mCurrent = val;
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
        String str = String.valueOf(((TextView) v).getText());
        if ("".equals(str)) {

            // Restore to the old value as we don't allow empty values
            updateView();
        } else {

            // Check the new value and ensure it's in range
            validateCurrentView(str);
        }
    }

    /**
     * We start the long click here but rely on the {@link NumberPickerButton}
     * to inform us when the long click has ended.
     */
    @Override
	public boolean onLongClick(View v) {

        /* The text view may still have focus so clear it's focus which will
         * trigger the on focus changed and any typed values to be pulled.
         */
        this.mText.clearFocus();

        if (R.id.increment == v.getId()) {
            this.mIncrement = true;
            this.mHandler.post(this.mRunnable);
        } else if (R.id.decrement == v.getId()) {
            this.mDecrement = true;
            this.mHandler.post(this.mRunnable);
        }
        return true;
    }

    public void cancelIncrement() {
        this.mIncrement = false;
    }

    public void cancelDecrement() {
        this.mDecrement = false;
    }

    static final char[] DIGIT_CHARACTERS = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    };

    private NumberPickerButton mIncrementButton;
    private NumberPickerButton mDecrementButton;

    private class NumberPickerInputFilter implements InputFilter {
        public NumberPickerInputFilter() {
			super();
		}

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {
            if (NumberPicker.this.mDisplayedValues == null) {
                return NumberPicker.this.mNumberInputFilter.filter(source, start, end, dest, dstart, dend);
            }
            CharSequence filtered = String.valueOf(source.subSequence(start, end));
            String result = String.valueOf(dest.subSequence(0, dstart))
                    + filtered
                    + dest.subSequence(dend, dest.length());
            String str = String.valueOf(result).toLowerCase(Locale.US);
            for (String val : NumberPicker.this.mDisplayedValues) {
                val = val.toLowerCase(Locale.US);
                if (val.startsWith(str)) {
                    return filtered;
                }
            }
            return "";
        }
    }

    private class NumberRangeKeyListener extends NumberKeyListener {

        public NumberRangeKeyListener() {
			super();
		}

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
        public CharSequence filter(CharSequence source, int start, int end,
                Spanned dest, int dstart, int dend) {

            CharSequence filtered = super.filter(source, start, end, dest, dstart, dend);
            if (filtered == null) {
                filtered = source.subSequence(start, end);
            }

            String result = String.valueOf(dest.subSequence(0, dstart))
                    + filtered
                    + dest.subSequence(dend, dest.length());

            if ("".equals(result)) {
                return result;
            }
            int val = getSelectedPos(result);

            /* Ensure the user can't type in a value greater
             * than the max allowed. We have to allow less than min
             * as the user might want to delete some numbers
             * and then type a new number.
             */
            if (val > NumberPicker.this.mEnd) {
                return "";
            }
			return filtered;
        }
    }

    int getSelectedPos(String str) {
        if (this.mDisplayedValues == null) {
            return Integer.parseInt(str);
        }
		for (int i = 0; i < this.mDisplayedValues.length; i++) {
		    /* Don't force the user to type in jan when ja will do */
		    String strl = str.toLowerCase(Locale.US);
		    if (this.mDisplayedValues[i].toLowerCase(Locale.US).startsWith(strl)) {
		        return this.mStart + i;
		    }
		}

		/* The user might have typed in a number into the month field i.e.
		 * 10 instead of OCT so support that too.
		 */
		try {
		    return Integer.parseInt(str);
		} catch (NumberFormatException e) {

		    /* Ignore as if it's not a number we don't care */
		}
        return this.mStart;
    }

    /**
     * @return the current value.
     */
    public int getCurrent() {
    	try {
    		this.mCurrent = Integer.parseInt(String.valueOf(((TextView) findViewById(R.id.timepicker_input)).getText()));
    	}
    	catch (NumberFormatException e){
    		return 0;    		
    	}
        return this.mCurrent;
    }
}
