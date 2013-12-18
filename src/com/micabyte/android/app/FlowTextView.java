package com.micabyte.android.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

// TODO: Draw Allocation
@SuppressLint("DrawAllocation")
public class FlowTextView extends RelativeLayout {


	public FlowTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FlowTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public FlowTextView(Context context) {
		super(context);
		init(context);
	}


	private int mColor = Color.BLACK;
	private int pageHeight = 0;

	public void setColor(int color) {
		this.mColor = color;
		if (this.mTextPaint != null) {
			this.mTextPaint.setColor(this.mColor);
		}

		for (TextPaint paint : this.mPaintHeap) {
			paint.setColor(this.mColor);
		}

		this.invalidate();
	}

	private void init(Context context) {

		this.mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		this.mTextPaint.density = getResources().getDisplayMetrics().density;
		this.mTextPaint.setTextSize(this.mTextsize);
		this.mTextPaint.setColor(Color.BLACK);

		this.mLinkPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		this.mLinkPaint.density = getResources().getDisplayMetrics().density;
		this.mLinkPaint.setTextSize(this.mTextsize);
		this.mLinkPaint.setColor(Color.BLUE);
		this.mLinkPaint.setUnderlineText(true);

		this.setBackgroundColor(Color.TRANSPARENT);

		this.setOnTouchListener(new OnTouchListener() {

			double distance = 0;

			float x1, y1, x2, y2;

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				int event_code = event.getAction();

				if (event_code == MotionEvent.ACTION_DOWN) {
					this.distance = 0;
					this.x1 = event.getX();
					this.y1 = event.getY();
				}

				if (event_code == MotionEvent.ACTION_MOVE) {
					this.x2 = event.getX();
					this.y2 = event.getY();
					this.distance = getPointDistance(this.x1, this.y1, this.x2, this.y2);
				}

				if (this.distance < 10) {

					if (event_code == MotionEvent.ACTION_UP) {

						FlowTextView.this.onClick(event.getX(), event.getY());
					}

					return true;
				}
				return false;
			}
		});

	}

	static double getPointDistance(float x1, float y1, float x2, float y2) {
		double dist = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
		return dist;

	}

	private TextPaint mTextPaint;
	private TextPaint mLinkPaint;

	private int mTextsize = 20;

	public void setTextSize(int textSize) {
		this.mTextsize = textSize;
		this.mTextPaint.setTextSize(this.mTextsize);
		this.mLinkPaint.setTextSize(this.mTextsize);
		invalidate();
	}

	private Typeface typeFace;

	public void setTypeface(Typeface type) {
		this.typeFace = type;
		this.mTextPaint.setTypeface(this.typeFace);
		this.mLinkPaint.setTypeface(this.typeFace);
		invalidate();
	}

	private int mDesiredHeight = 100; // height of the whole view

	private float mSpacingMult = 1.0f;
	private float mSpacingAdd = 0.0f;

	private float mViewWidth;

	private class Area {
		public Area() {
			super();
		}
		float x1;
		float x2;
		float width;
	}


	private ArrayList<Box> mLineboxes = new ArrayList<FlowTextView.Box>();
	private ArrayList<Area> mAreas = new ArrayList<FlowTextView.Area>();

	void onClick(float x, float y) {

		for (HtmlLink link : this.mLinks) {
			float tlX = link.xOffset;
			float tlY = link.yOffset;
			float brX = link.xOffset + link.width;
			float brY = link.yOffset + link.height;

			if (x > tlX && x < brX) {
				if (y > tlY && y < brY) {
					// collision
					onLinkClick(link.url);
					return;
				}
			}

		}
	}

	private OnLinkClickListener mOnLinkClickListener;

	public void setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
		this.mOnLinkClickListener = onLinkClickListener;
	}

	public interface OnLinkClickListener {
		public void onLinkClick(String url);
	}

	private void onLinkClick(String url) {
		if (this.mOnLinkClickListener != null) this.mOnLinkClickListener.onLinkClick(url);
	}

	private Line getLine(float lineYbottom, int lineHeight) {

		Line line = new Line();
		line.leftBound = 0;
		line.rightBound = this.mViewWidth;

		float lineYtop = lineYbottom - lineHeight;

		this.mAreas.clear();
		this.mLineboxes.clear();

		for (Box box : this.boxes) {

			if (box.topLefty > lineYbottom || box.bottomRighty < lineYtop) {
				// NOOP
			}
			else {

				Area leftArea = new Area();
				leftArea.x1 = 0;

				for (Box innerBox : this.boxes) {
					if (innerBox.topLefty > lineYbottom || innerBox.bottomRighty < lineYtop) {
						// NOOP
					}
					else {
						if (innerBox.topLeftx < box.topLeftx) {
							leftArea.x1 = innerBox.bottomRightx;
						}
					}
				}

				leftArea.x2 = box.topLeftx;
				leftArea.width = leftArea.x2 - leftArea.x1;

				Area rightArea = new Area();
				rightArea.x1 = box.bottomRightx;
				rightArea.x2 = this.mViewWidth;

				for (Box innerBox : this.boxes) {
					if (innerBox.topLefty > lineYbottom || innerBox.bottomRighty < lineYtop) {
						// NOOP
					}
					else {
						if (innerBox.bottomRightx > box.bottomRightx) {
							rightArea.x2 = innerBox.topLeftx;
						}
					}
				}

				rightArea.width = rightArea.x2 - rightArea.x1;

				this.mAreas.add(leftArea);
				this.mAreas.add(rightArea);
			}
		}
		this.mLargestArea = null;

		if (this.mAreas.size() > 0) { // if there is no areas then the whole line is clear, if there
										// is areas, return the largest (it means there is one or
										// more boxes colliding with this line)
			for (Area area : this.mAreas) {
				if (this.mLargestArea == null) {
					this.mLargestArea = area;
				}
				else {
					if (area.width > this.mLargestArea.width) {
						this.mLargestArea = area;
					}
				}
			}

			line.leftBound = this.mLargestArea.x1;
			line.rightBound = this.mLargestArea.x2;
		}

		return line;
	}

	Area mLargestArea;

	private int getChunk(String text, float maxWidth) {
		int length = this.mTextPaint.breakText(text, true, maxWidth, null);
		if (length <= 0)
			return length; // if its 0 or less, return it, can't fit any chars on this line
		else if (length >= text.length())
			return length; // we can fit the whole string in
		else if (text.charAt(length - 1) == ' ')
			return length; // if break char is a space -- return
		else {
			if (text.length() > length) if (text.charAt(length) == ' ') return length + 1; // or if
																							// the
																							// following
																							// char
																							// is a
																							// space
																							// then
																							// return
																							// this
																							// length
																							// - it
																							// is
																							// fine
		}

		// otherwise, count back until we hit a space and return that as the break length
		int tempLength = length - 1;
		while (text.charAt(tempLength) != ' ') {

			// char test = text.charAt(tempLength);
			tempLength--;
			if (tempLength <= 0) return length; // if we count all the way back to 0 then this line
												// cannot be broken, just return the original break
												// length
		}

		// char test = text.charAt(tempLength);
		return tempLength + 1; // return the nicer break length which doesn't split a word up

	}

	@Override
	protected void onDraw(Canvas canvas) {

		Log.i("flowText", "onDraw");

		super.onDraw(canvas);

		this.mViewWidth = this.getWidth();
		int lowestYCoord = 0;
		this.boxes.clear();

		int childCount = this.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				Box box = new Box();
				box.topLeftx = child.getLeft();
				box.topLefty = child.getTop();
				box.bottomRightx = box.topLeftx + child.getWidth();
				box.bottomRighty = box.topLefty + child.getHeight();
				this.boxes.add(box);
				if (box.bottomRighty > lowestYCoord) lowestYCoord = box.bottomRighty;
			}
		}

		String[] blocks = this.mText.toString().split("\n");

		int charOffsetStart = 0; // tells us where we are in the original string
		int charOffsetEnd = 0; // tells us where we are in the original string
		int lineIndex = 0;
		float xOffset = 0; // left margin off a given line
		float maxWidth = this.mViewWidth; // how far to the right it can strectch
		float yOffset = 0;
		String thisLineStr;
		int chunkSize;
		int lineHeight = getLineHeight();

		ArrayList<HtmlObject> lineObjects = new ArrayList<FlowTextView.HtmlObject>();
		Object[] spans = new Object[0];

		HtmlObject htmlLine;// = new HtmlObject(); // reuse for single plain lines

		this.mLinks.clear();

		for (int block_no = 0; block_no <= blocks.length - 1; block_no++) {

			String thisBlock = blocks[block_no];
			if (thisBlock.length() <= 0) {
				lineIndex++; // is a line break
				charOffsetEnd += 2;
				charOffsetStart = charOffsetEnd;
			}
			else {

				while (thisBlock.length() > 0) {
					lineIndex++;
					yOffset = lineIndex * lineHeight;
					Line thisLine = getLine(yOffset, lineHeight);
					xOffset = thisLine.leftBound;
					maxWidth = thisLine.rightBound - thisLine.leftBound;
					float actualWidth = 0;


					do {
						Log.i("tv", "maxWidth: " + maxWidth);
						chunkSize = getChunk(thisBlock, maxWidth);
						int thisCharOffset = charOffsetEnd + chunkSize;

						if (chunkSize > 1) {
							thisLineStr = thisBlock.substring(0, chunkSize);
						}
						else {
							thisLineStr = "";
						}

						lineObjects.clear();

						if (this.mIsHtml) {
							spans = ((Spanned) this.mText).getSpans(charOffsetStart, thisCharOffset, Object.class);
							if (spans.length > 0) {
								actualWidth = parseSpans(lineObjects, spans, charOffsetStart, thisCharOffset, xOffset);
							}
							else {
								actualWidth = maxWidth; // if no spans then the actual width will be
														// <= maxwidth anyway
							}
						}
						else {
							actualWidth = maxWidth;// if not html then the actual width will be <=
													// maxwidth anyway
						}


						Log.i("tv", "actualWidth: " + actualWidth);

						if (actualWidth > maxWidth) {
							maxWidth -= 5; // if we end up looping - start slicing chars off till we
											// get a suitable size
						}

					}
					while (actualWidth > maxWidth);



					// chunk is ok
					charOffsetEnd += chunkSize;

					Log.i("tv", "charOffsetEnd: " + charOffsetEnd);

					if (lineObjects.size() <= 0) { // no funky objects found, add the whole chunk as
													// one object
						htmlLine = new HtmlObject(thisLineStr, 0, 0, xOffset, this.mTextPaint);
						lineObjects.add(htmlLine);
					}

					for (HtmlObject thisHtmlObject : lineObjects) {

						if (thisHtmlObject instanceof HtmlLink) {
							HtmlLink thisLink = (HtmlLink) thisHtmlObject;
							float thisLinkWidth = thisLink.paint.measureText(thisHtmlObject.content);
							addLink(thisLink, yOffset, thisLinkWidth, lineHeight);
						}

						paintObject(canvas, thisHtmlObject.content, thisHtmlObject.xOffset, yOffset, thisHtmlObject.paint);

						if (thisHtmlObject.recycle) {
							recyclePaint(thisHtmlObject.paint);
						}
					}


					if (chunkSize >= 1) thisBlock = thisBlock.substring(chunkSize, thisBlock.length());

					charOffsetStart = charOffsetEnd;
				}
			}
		}

		yOffset += (lineHeight / 2);

		View child = getChildAt(getChildCount() - 1);
		if (child.getTag() != null) {
			if (child.getTag().toString().equalsIgnoreCase("hideable")) {
				if (yOffset > this.pageHeight) {
					if (yOffset < this.boxes.get(this.boxes.size() - 1).topLefty - getLineHeight()) {
						child.setVisibility(View.GONE);
						// lowestYCoord = (int) yOffset;
					}
					else {
						// lowestYCoord = boxes.get(boxes.size()-1).bottomRighty + getLineHeight();
						child.setVisibility(View.VISIBLE);
					}
				}
				else {
					child.setVisibility(View.GONE);
					// lowestYCoord = (int) yOffset;
				}
			}
		}


		this.mDesiredHeight = Math.max(lowestYCoord, (int) yOffset);
		if (this.needsMeasure) {
			this.needsMeasure = false;
			requestLayout();
		}
	}


	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		this.invalidate();
	}

	@Override
	public void invalidate() {
		this.needsMeasure = true;
		super.invalidate();
	}

	boolean needsMeasure = true;

	private static void paintObject(Canvas canvas, String thisLineStr, float xOffset, float yOffset, Paint paint) {
		canvas.drawText(thisLineStr, xOffset, yOffset, paint);
	}

	private class Box {
		public Box() {
			// TODO Auto-generated constructor stub
		}
		public int topLeftx;
		public int topLefty;
		public int bottomRightx;
		public int bottomRighty;
	}

	private class Line {
		public Line() {
			super();
		}
		public float leftBound;
		public float rightBound;
	}

	private ArrayList<Box> boxes = new ArrayList<FlowTextView.Box>();
	//private static final BoringLayout.Metrics UNKNOWN_BORING = new BoringLayout.Metrics();

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		Log.i("flowText", "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int width = 0;
		int height = 0;

		if (widthMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			width = widthSize;
		}
		else {
			width = this.getWidth();
		}

		if (heightMode == MeasureSpec.EXACTLY) {
			// Parent has told us how big to be. So be it.
			height = heightSize;
		}
		else {
			height = this.mDesiredHeight;
		}

		setMeasuredDimension(width, height + getLineHeight());

		// setMeasuredDimension(800, 1400);
	}



	public int getLineHeight() {
		return Math.round(this.mTextPaint.getFontMetricsInt(null) * this.mSpacingMult + this.mSpacingAdd);
	}

	private CharSequence mText = "";

	private boolean mIsHtml = false;

	// private URLSpan[] urls;

	class HtmlObject {

		public HtmlObject(String content, int start, int end, float xOffset, TextPaint paint) {
			super();
			this.content = content;
			this.start = start;
			this.end = end;
			this.xOffset = xOffset;
			this.paint = paint;
		}

		public String content;
		public int start;
		public int end;
		public float xOffset;
		public TextPaint paint;
		public boolean recycle = false;
	}

	class HtmlLink extends HtmlObject {
		public HtmlLink(String content, int start, int end, float xOffset, TextPaint paint, String url) {
			super(content, start, end, xOffset, paint);
			this.url = url;
		}

		public float width;
		public float height;
		public float yOffset;
		public String url;
	}


	private boolean[] charFlags;
	int charFlagSize = 0;
	int charFlagIndex = 0;
	int spanStart = 0;
	int spanEnd = 0;
	int charCounter;
	float objPixelwidth;

	// TODO: Use Sparse Arrays
	@SuppressLint("UseSparseArrays")
	HashMap<Integer, HtmlObject> sorterMap = new HashMap<Integer, FlowTextView.HtmlObject>();

	private float parseSpans(ArrayList<HtmlObject> objects, Object[] spans, int lineStart, int lineEnd, float baseXOffset) {

		this.sorterMap.clear();

		this.charFlagSize = lineEnd - lineStart;
		this.charFlags = new boolean[this.charFlagSize];

		for (Object span : spans) {
			this.spanStart = this.mSpannable.getSpanStart(span);
			this.spanEnd = this.mSpannable.getSpanEnd(span);

			if (this.spanStart < lineStart) this.spanStart = lineStart;
			if (this.spanEnd > lineEnd) this.spanEnd = lineEnd;

			for (this.charCounter = this.spanStart; this.charCounter < this.spanEnd; this.charCounter++) { // mark
																											// these
																											// characters
																											// as
																											// rendered
				this.charFlagIndex = this.charCounter - lineStart;
				this.charFlags[this.charFlagIndex] = true;
			}

			this.tempString = extractText(this.spanStart, this.spanEnd);
			this.sorterMap.put(Integer.valueOf(this.spanStart), parseSpan(span, this.tempString, this.spanStart, this.spanEnd));
			// objects.add();
		}

		this.charCounter = 0;

		while (!isArrayFull(this.charFlags)) {
			while (true) {

				if (this.charCounter >= this.charFlagSize) break;


				if (this.charFlags[this.charCounter] == true) {
					this.charCounter++;
					continue;
				}

				this.temp1 = this.charCounter;
				while (true) {
					if (this.charCounter > this.charFlagSize) break;

					if (this.charCounter < this.charFlagSize) {
						if (this.charFlags[this.charCounter] == false) {

							this.charFlags[this.charCounter] = true;// mark as filled
							this.charCounter++;
							continue;

						}
					}
					this.temp2 = this.charCounter;
					this.spanStart = lineStart + this.temp1;
					this.spanEnd = lineStart + this.temp2;
					this.tempString = extractText(this.spanStart, this.spanEnd);
					this.sorterMap.put(Integer.valueOf(this.spanStart), parseSpan(null, this.tempString, this.spanStart, this.spanEnd));
					break;

				}
			}
		}

		this.sorterKeys = this.sorterMap.keySet().toArray();
		Arrays.sort(this.sorterKeys);

		float thisXoffset = baseXOffset;

		for (this.charCounter = 0; this.charCounter < this.sorterKeys.length; this.charCounter++) {
			HtmlObject thisObj = this.sorterMap.get(this.sorterKeys[this.charCounter]);
			thisObj.xOffset = thisXoffset;
			this.tempFloat = thisObj.paint.measureText(thisObj.content);
			thisXoffset += this.tempFloat;
			objects.add(thisObj);
		}

		return (thisXoffset - baseXOffset);
	}

	float tempFloat;
	Object[] sorterKeys;
	int[] sortedKeys;
	String tempString;
	int temp1;
	int temp2;

	int arrayIndex = 0;

	private boolean isArrayFull(boolean[] array) {
		for (this.arrayIndex = 0; this.arrayIndex < array.length; this.arrayIndex++) {
			if (array[this.arrayIndex] == false) return false;
		}
		return true;
	}

	private HtmlObject parseSpan(Object span, String content, int start, int end) {

		if (span instanceof URLSpan) {
			return getHtmlLink((URLSpan) span, content, start, end, 0);
		}
		else if (span instanceof StyleSpan) {
			return getStyledObject((StyleSpan) span, content, start, end, 0);
		}
		else {
			return getHtmlObject(content, start, end, 0);
		}
	}

	private String extractText(int s, int e) {
		int start = s; int end = e;
		if (start < 0) start = 0;
		if (end > this.mTextLength - 1) end = this.mTextLength - 1;
		return this.mSpannable.subSequence(start, end).toString();
	}


	private ArrayList<TextPaint> mPaintHeap = new ArrayList<TextPaint>();

	private TextPaint getPaintFromHeap() {
		if (this.mPaintHeap.size() > 0) {
			return this.mPaintHeap.remove(0);
		}
		return new TextPaint(Paint.ANTI_ALIAS_FLAG);
	}

	private void recyclePaint(TextPaint paint) {
		this.mPaintHeap.add(paint);
	}

	private HtmlObject getStyledObject(StyleSpan span, String content, int start, int end, float thisXOffset) {
		TextPaint paint = getPaintFromHeap();
		paint.setTypeface(Typeface.defaultFromStyle(span.getStyle()));
		paint.setTextSize(this.mTextsize);
		paint.setColor(this.mColor);

		span.updateDrawState(paint);
		span.updateMeasureState(paint);
		HtmlObject obj = new HtmlObject(content, start, end, thisXOffset, paint);
		obj.recycle = true;
		return obj;
	}

	private HtmlObject getHtmlObject(String content, int start, int end, float thisXOffset) {
		HtmlObject obj = new HtmlObject(content, start, end, thisXOffset, this.mTextPaint);
		return obj;
	}

	private ArrayList<HtmlLink> mLinks = new ArrayList<FlowTextView.HtmlLink>();

	private HtmlLink getHtmlLink(URLSpan span, String content, int start, int end, float thisXOffset) {
		HtmlLink obj = new HtmlLink(content, start, end, thisXOffset, this.mLinkPaint, span.getURL());
		this.mLinks.add(obj);
		return obj;
	}

	private void addLink(HtmlLink thisLink, float yOffset, float width, float height) {
		thisLink.yOffset = yOffset - 20;
		thisLink.width = width;
		thisLink.height = height + 20;
		this.mLinks.add(thisLink);

	}

	private Spannable mSpannable;


	int mTextLength = 0;

	public void setText(CharSequence text) {
		this.mText = text;
		if (text instanceof Spannable) {
			this.mIsHtml = true;
			this.mSpannable = (Spannable) text;
			//Object[] urls = this.mSpannable.getSpans(0, this.mSpannable.length(), Object.class);
		}
		else {
			this.mIsHtml = false;
		}

		this.mTextLength = this.mText.length();

		this.invalidate();
	}


	private ArrayList<BitmapSpec> bitmaps = new ArrayList<FlowTextView.BitmapSpec>();

	public BitmapSpec addImage(Bitmap bitmap, int xOffset, int yOffset, int padding) {
		BitmapSpec spec = new BitmapSpec(bitmap, xOffset, yOffset, padding);
		this.bitmaps.add(spec);
		return spec;
	}

	public ArrayList<BitmapSpec> getBitmaps() {
		return this.bitmaps;
	}

	public void setBitmaps(ArrayList<BitmapSpec> bitmaps) {
		this.bitmaps = bitmaps;
	}

	public class BitmapSpec {

		public BitmapSpec(Bitmap bitmap, int xOffset, int yOffset, int mPadding) {
			super();
			this.bitmap = bitmap;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.mPadding = mPadding;
		}

		public Bitmap bitmap;
		public int xOffset;
		public int yOffset;
		public int mPadding = 10;
	}

	public void setPageHeight(int pageHeight) {
		this.pageHeight = pageHeight;
	}


}
