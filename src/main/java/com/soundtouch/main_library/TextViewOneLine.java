package com.soundtouch.main_library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.soundtouch.main_library.Logger.LogLevel;


public class TextViewOneLine extends TextView
{
	protected boolean changeSize  = true;

	protected static final int	MAX_SIZE	= 70;

	public TextViewOneLine(Context context, AttributeSet attrs)
	{

		super(context, attrs);

	}

	public TextViewOneLine(Context context)
	{
		super(context);

	}

	public TextViewOneLine(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public float resizeText(Paint paint)
	{
		float txtSize = getTextSize();
		while (getTextFrameDiff(getPaint()) < 0 )
		{
			txtSize -= 1;
			paint.setTextSize(txtSize);
		}
		return txtSize;
	}


	/** Returns negative if the text width is larger than it should be **/
	protected float getTextFrameDiff(Paint paint)
	{
		float frameLayoutSize = getWidth() - getPaddingRight() - getPaddingLeft();
		return frameLayoutSize - paint.measureText(getText().toString());
	}
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		changeSize = true;
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{

		if(changeSize){
			// if text has changed ,  set text size to the max size in order to re-size it again.
			setTextSize(MAX_SIZE);
			changeSize = false;
		}
		resizeText(getPaint());
		super.onDraw(canvas);
	}
}
