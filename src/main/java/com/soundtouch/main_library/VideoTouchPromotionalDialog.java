package com.soundtouch.main_library;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;


public class VideoTouchPromotionalDialog extends PromotionalDialog
{

	public VideoTouchPromotionalDialog(Context context, boolean withTitle)
	{
		super(context, withTitle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected PromotionsAdapter getPromotionAdapter(boolean withTitle)
	{
		return new VideoTouchPromotionAdapter(context, withTitle);
	}

	
	/**
	 * 
	 * @author Aziz
	 * This is the adapter for video touch promotions
	 */
	class VideoTouchPromotionAdapter extends PromotionsAdapter
	{

		protected static final String	PROMOTION_TEXT_RES_ID_PREFIX	= "main_activity_promotion_dialog_item_";
		protected static final String	PROMOTION_ICON_RES_ID_PREFIX	= "promotion_dialog_icon_";
		protected static final int		ALSO_AVAILABLE_INDX				= 1;
		protected static final int		BOTTOM_TEXT_INDX				= 6;
		
		public VideoTouchPromotionAdapter(Context context, boolean withTitle)
		{
			super(context, withTitle);
		}

		@Override
		protected int getAmountOfPromotions()
		{
			return 5;
		}

		@Override
		protected String getItemText(int position)
		{
			int identifier = context.getResources().getIdentifier(PROMOTION_TEXT_RES_ID_PREFIX + position, "string", context.getPackageName());
			return context.getResources().getString(identifier);
		}

		@Override
		protected String getItemURL(int position)
		{
			int identifier = context.getResources().getIdentifier(PROMOTION_TEXT_RES_ID_PREFIX + URL + position, "string", context.getPackageName());
			return context.getResources().getString(identifier);
		}

		@Override
		protected Drawable getItemIconDrawable(int position)
		{

			int identifier = context.getResources().getIdentifier(PROMOTION_ICON_RES_ID_PREFIX + position, "drawable", context.getPackageName());
			return context.getResources().getDrawable(identifier);
		}

		@Override
		protected String getTitleBigText()
		{

			return "<font color='#91C8FF'>" + context.getResources().getString(R.string.main_activity_promotion_dialog_item_0_1);
		}

		@Override
		protected View onWithTitleSetting(int position, LayoutInflater inflater)
		{// If with title , then few indices should have different layouts.
			if (position == ALSO_AVAILABLE_INDX)
			{
				View result;
				result = setTextRowView(position, inflater);
				return result;
			}
			return super.onWithTitleSetting(position, inflater);
		}

	}
}
