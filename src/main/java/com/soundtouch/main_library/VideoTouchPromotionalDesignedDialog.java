package com.soundtouch.main_library;

import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.soundtouch.main_library.Logger.LogLevel;


public class VideoTouchPromotionalDesignedDialog extends InversableDialog
{
	protected OnClickListener	onOKClickListener;

	public VideoTouchPromotionalDesignedDialog(Context context, OnClickListener onOKClickListener)
	{
		super(context);
		this.onOKClickListener = onOKClickListener;
		setOKButton();
		setStoreLinkButton();
	}

	protected void setStoreLinkButton()
	{
		ImageView v = (ImageView) findViewById(R.id.videotouch_popup_btn_link);
		v.setImageResource(getStoreButtonImageResource());
		v.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				openMarketLink();
			}
		});
	}



	protected void setOKButton()
	{
		View v = findViewById(R.id.videotouch_popup_exit_btn);
		v.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				onOKClickListener.onClick(VideoTouchPromotionalDesignedDialog.this, 0);
			}
		});
	}
	
	/** 
	 * 
	 * @return the links for the store and browser respectively	 
	 * */
	protected String[] getLinks(){
		if(App.global().isUsingAmazon()){
			return new String[]{context.getString(R.string.main_activity_videotouch_promo_link_amazon),context.getString(R.string.main_activity_videotouch_promo_link_browser_amazon)};
		}
		return new String[]{context.getString(R.string.main_activity_videotouch_promo_link),context.getString(R.string.main_activity_videotouch_promo_link_browser)};

	}


	protected void openMarketLink()
	{
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(getLinks()[0]));
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		try
		{
			context.startActivity(goToMarket);
		}
		catch (final ActivityNotFoundException e)
		{
			// Logger.log(LogLevel.DEBUG,
			// "market wasn't found, so starting web browser instead...");
			goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(getLinks()[1]));
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			try
			{
				context.startActivity(goToMarket);
			}
			catch (final ActivityNotFoundException e2)
			{
				Logger.log(LogLevel.WARNING, "can't open simple url , so ignoring request");
			}
		}
	}

	protected int getStoreButtonImageResource()
	{
		if (App.global().isUsingAmazon())
		{
			return R.drawable.amazon_buy_buton;
		}
		Locale locale = Locale.getDefault();

		int identifier = context.getResources().getIdentifier(locale.getLanguage() + "_generic_rgb_wo_60", "drawable", context.getPackageName());
		if (identifier == 0)
		{
			return R.drawable.en_generic_rgb_wo_60;
		}
		return identifier;
	}

	/** Mandatory to override **/
	protected int getContentView()
	{
		return R.layout.videotouch_popup_layout;
	}

	/** It is mandatory to override this method if you want your dialog to be reversable */
	protected void setInversableViewGroup()
	{
		this.inversableLayout = (LinearLayout) findViewById(R.id.videotouch_popup_container);
	}

}
