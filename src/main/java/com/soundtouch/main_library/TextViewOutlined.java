package com.soundtouch.main_library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewOutlined extends TextViewOneLine {
	int strokeWidth = 4;
	int[] strokeARGB = {255, 0, 0, 0};
	int textColor = 0;
	
	public TextViewOutlined(Context context) {
		super(context);
		
	}

	public TextViewOutlined(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	

	public void setStrokeARGB(int a, int r, int g, int b) {
		int[] strokeARGBcons = {a,r,g,b};
		strokeARGB = strokeARGBcons;
	}
	public void setTextColor(int color){
		textColor = color;
	}
	public void setStrokeWidth(int strokeWidth){
		this.strokeWidth = strokeWidth;
	}

	@Override
	public void draw(Canvas canvas) {
		Paint strokePaint = new Paint();
		Paint textPaint = new Paint();
		Paint.Align align = getPaint().getTextAlign();
		float textSize = getPaint().getTextSize();
		String text = getText().toString();
		Rect textBounds = new Rect();
		getPaint().getTextBounds(text, 0, getText().length(), textBounds);
		float positionX = 0;// 0;//0.5f * (super.getWidth() - textBounds.width() );
		float positionY = getBaseline(); ; //(getHeight() - textBounds.height())*0.5f + textBounds.height();//0;//0.5f * (super.getHeight() + textBounds.height());
		strokePaint.setARGB(strokeARGB[0],strokeARGB[1], strokeARGB[2], strokeARGB[3]);
		strokePaint.setTextAlign(align);
		strokePaint.setTextSize(textSize);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(strokeWidth);
		textPaint.setColor(textColor);
		textPaint.setTextAlign(align);
		textPaint.setTextSize(getPaint().getTextSize());
		textPaint.setTextScaleX(getPaint().getTextScaleX());
		textPaint.setStyle(getPaint().getStyle());
		resizeText(textPaint);
		resizeText(strokePaint);
		canvas.drawText(text,positionX,positionY, strokePaint);
		canvas.drawText(text,positionX,positionY, textPaint);
		

		//super.draw(canvas);
	}

}
