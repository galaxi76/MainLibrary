package com.soundtouch.main_library;

import java.util.Locale;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.soundtouch.main_library.Logger.LogLevel;


public class SoundTouch2PromotionDialog extends InversableDialog
{
	protected OnClickListener onOKClickListener;

	public SoundTouch2PromotionDialog(Context context , OnClickListener onOKClickListener)
	{
		super(context);
		this.onOKClickListener = onOKClickListener;
		setOKButton();
		setBackground();
		setStoreLinkButton();
		
//		TextViewOutlined txt = (TextViewOutlined) findViewById(R.id.soundtouch2_pop_up_txtViewOutlined);
//		txt.setTextColor(Color.rgb(255, 0, 0));
//		txt.setStrokeARGB(150, 255, 255, 255);
//		txt.setStrokeWidth(2);
	}

	@Override
	protected int getContentView()
	{
		return R.layout.soundtouch2_popup_layout;
	}

	@Override
	public void setInversableViewGroup()
	{
		inversableLayout = (ViewGroup) findViewById(R.id.soundtouch2_popup_container);
	}

	protected void setStoreLinkButton()
	{
		ImageView v = (ImageView) findViewById(R.id.soundtouch2_popup_btn_link);
		v.setImageResource(getStoreButtonImageResource());
		v.setAnimation(getAlphaAnimaiton());
		v.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				openMarketLink();
			}
		});
	}
	
	protected int getStoreButtonImageResource(){
		if(App.global().isUsingAmazon()){
			return R.drawable.amazon_buy_buton;
		}
		Locale locale = Locale.getDefault();
		
		 int identifier = context.getResources().getIdentifier(locale.getLanguage()+"_generic_rgb_wo_60" , "drawable", context.getPackageName());
		 if (identifier == 0)
		 {
		 return R.drawable.en_generic_rgb_wo_60;
		 }
		 return identifier;
	}

	protected AlphaAnimation getAlphaAnimaiton()
	{
		AlphaAnimation anim = new AlphaAnimation(1f, 0.5f);
		anim.setDuration(1000);
		anim.setRepeatCount(AlphaAnimation.INFINITE);
		return anim;
	}

	protected void setOKButton()
	{
		View v = findViewById(R.id.soundtouch2_popup_btn_ok);
		v.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
			//	SoundTouch2PromotionDialog.this.dismiss();
				onOKClickListener.onClick(SoundTouch2PromotionDialog.this, 0);
			}
		});
	}

	// protected void setBackground()
	// {
	// ViewGroup container = (ViewGroup) findViewById(R.id.soundtouch2_popup_container);
	// container.setBackgroundResource(getBackground(Locale.getDefault()));
	// }
	protected void setBackground()
	{
		ViewGroup container = (ViewGroup) findViewById(R.id.soundtouch2_popup_container);
		container.setBackgroundResource(R.drawable.soundtouch2cleanpopup);
	}

	// protected int getBackground(Locale locale)
	// {
	// int identifier = context.getResources().getIdentifier("soundtouch2popup1_" + locale.getLanguage(), "drawable", context.getPackageName());
	// if (identifier == 0)
	// {
	// return R.drawable.soundtouch2popup1;
	// }
	// return identifier;
	// }


	/** 
	 * 
	 * @return the links for the store and browser respectively	 
	 * */
	protected String[] getLinks(){
		if(App.global().isUsingAmazon()){
			return new String[]{context.getString(R.string.soundtouch2_market_url_amazon),context.getString(R.string.soundtouch2_browser_url_amazon)};
		}
		return new String[]{context.getString(R.string.soundtouch2_market_url),context.getString(R.string.soundtouch2_browser_url)};

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

}
