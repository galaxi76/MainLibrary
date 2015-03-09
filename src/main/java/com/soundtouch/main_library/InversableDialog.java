package com.soundtouch.main_library;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


public class InversableDialog extends Dialog
{
	protected Context	context;
	protected ViewGroup	inversableLayout;

	public enum INVERSION_TYPE
	{
		UPSIDE_DOWN, NORMAL
	};

	public InversableDialog(Context context)
	{
		
		super(context);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getContentView());
		setInversableViewGroup();
		if (SettingsActivity.isScreenOrientationChecked())
		{
			inverse(INVERSION_TYPE.UPSIDE_DOWN);
		}
	}

	

	/** Mandatory to override **/
	protected int getContentView()
	{
		return R.layout.inversable_alert_dialog;
	}

	/** It is mandatory to override this method if you want your dialog to be reversable */
	protected void setInversableViewGroup()
	{
		this.inversableLayout = (LinearLayout) findViewById(R.id.inversable_alert_dialog_container);
	}

	public void setText(String txt)
	{
		TextView tv = (TextView) findViewById(R.id.inversable_alert_dialog_txt);
		tv.setText(txt);
	}

	public void setPositiveButton(String buttonString, View.OnClickListener ocl)
	{
		Button b = (Button) findViewById(R.id.inversable_alert_dialog_dialogButton1);
		b.setText(buttonString);

		b.setOnClickListener(ocl);

	}

	public void setNegativeButton(String buttonString, android.view.View.OnClickListener oncl)
	{

		Button b = (Button) findViewById(R.id.inversable_alert_dialog_dialogButton2);
		if (b == null)
		{
			return;
		}
		b.setText(buttonString);
		b.setOnClickListener(oncl);
	}

	public void inverse(INVERSION_TYPE type)
	{

		if (!App.global().isScaleSupported())
		{

			return;
		}
		int x = 1;
		int y = 1;
		if (type == INVERSION_TYPE.UPSIDE_DOWN)
		{
			x = -1;
			y = -1;
		}

		inversableLayout.setScaleX(x);
		inversableLayout.setScaleY(y);
	}

	public void setOnlyOneButton()
	{
		Button b = (Button) findViewById(R.id.inversable_alert_dialog_dialogButton2);
		b.setVisibility(View.GONE);

	}

	public void setTitle(CharSequence titleString)
	{
		TextView tv = (TextView) findViewById(R.id.inversable_alert_dialog_title);
		tv.setText(titleString);
	}
}
