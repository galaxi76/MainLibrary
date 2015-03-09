package com.soundtouch.main_library.activities.full_screen_image_activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;


public class AutoSizeTextView extends TextView
{
  private static final float DEFAULT_MAX_FONT_SIZE = 1024;
  private static final float DEFAULT_MIN_FONT_SIZE = 2;
  private Paint              _testPaint;
  private float              _maxFontSize, _minFontSize;
  boolean                    _handledAutoResizing  = false;

  public AutoSizeTextView(final Context context)
  {
    super(context);
    initialize();
  }

  public AutoSizeTextView(final Context context, final AttributeSet attrs)
  {
    super(context, attrs);
    initialize();
  }

  @TargetApi(11)
  private void initialize()
  {
    // disable hardware accelaration
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
      setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    _minFontSize = DEFAULT_MIN_FONT_SIZE;
    _maxFontSize = DEFAULT_MAX_FONT_SIZE;
    _testPaint = new Paint();
    _testPaint.set(this.getPaint());
  }

  public void setMinFontSize(final float minFontSize)
  {
    _minFontSize = minFontSize;
  }

  public void setMaxFontSize(final float maxFontSize)
  {
    _maxFontSize = maxFontSize;
  }

  public float getMinFontSize()
  {
    return _minFontSize;
  }

  public float getMaxFontSize()
  {
    return _maxFontSize;
  }

  /** auto resizes the font according to the text content and the width of the textView */
  protected void refitText(final String text, final int textViewWidth)
  {
    if (textViewWidth <= 0 || _handledAutoResizing)
      return;
    final int targetWidth = textViewWidth - this.getPaddingLeft() - this.getPaddingRight();
    float xHi = _maxFontSize, xLo = _minFontSize;
    float yHi, yLo;
    _testPaint.set(this.getPaint());
    //
    _testPaint.setTextSize(xHi);
    yHi = _testPaint.measureText(text);
    // if the largest font size fits, use it
    if (yHi < targetWidth)
    {
      _handledAutoResizing = true;
      setSingleLine(true);
      this.setTextSize(TypedValue.COMPLEX_UNIT_PX, xHi);
      return;
    }
    //
    _testPaint.setTextSize(xLo);
    yLo = _testPaint.measureText(text);
    // if the smallest font size is too large , set to multiple lines and use this font size
    if (yLo > targetWidth)
    {
      _handledAutoResizing = true;
      setSingleLine(false);
      this.setTextSize(TypedValue.COMPLEX_UNIT_PX, xLo);
      return;
    }
    //
    final float threshold = 0.5f; // How close we have to be
    // do a search for the best size of the font:
    while (true)
    {
      if (xHi - xLo <= threshold)
      {
        _handledAutoResizing = true;
        setSingleLine(true);
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, xLo);
        break;
      }
      final float size = (xHi + xLo) / 2;
      // TODO use a better search algorithm , like the linear interpolation
      _testPaint.setTextSize(size);
      final float measuredTextWidth = _testPaint.measureText(text);
      if (measuredTextWidth >= targetWidth)
      {
        // too big
        xHi = size;
        yHi = measuredTextWidth;
      }
      else
      {
        // too small
        xLo = size;
        yLo = measuredTextWidth;
      }
    }
  }

  @Override
  protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec)
  {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    final int height = getMeasuredHeight();
    refitText(this.getText().toString(), parentWidth);
    this.setMeasuredDimension(parentWidth, height);
    // _handledAutoResizing = false;
  }

  @Override
  protected void onTextChanged(final CharSequence text, final int start, final int before, final int after)
  {
    refitText(text.toString(), this.getWidth());
    _handledAutoResizing = false;
  }

  @Override
  protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh)
  {
    if (w != oldw)
      refitText(this.getText().toString(), w);
    _handledAutoResizing = false;
  }
}
