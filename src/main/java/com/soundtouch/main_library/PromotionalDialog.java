package com.soundtouch.main_library;

import com.soundtouch.main_library.Logger.LogLevel;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


public abstract class PromotionalDialog extends InversableDialog
{
	public PromotionalDialog(Context context, boolean withTitle)
	{
		super(context);
		ListView lv = (ListView) findViewById(R.id.promotion_dialog_listview);
		lv.setAdapter(getPromotionAdapter(withTitle));
		setButtonAction();
	}

	protected void setButtonAction()
	{
		Button button = (Button) findViewById(R.id.promotion_dialog_okButton);
		button.setOnClickListener(new View.OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				dismiss();

			}
		});
	}

	@Override
	protected int getContentView()
	{
		return R.layout.promotion_dialog_layout;
	}

	@Override
	public void setInversableViewGroup()
	{
		inversableLayout = (LinearLayout) findViewById(R.id.promotion_dialog_rootLayout);
	}

	protected abstract PromotionsAdapter getPromotionAdapter(boolean withTitle);


	
	/**
	 * This is the adapter for the list view in this custom dialog
	 * 
	 * @author Aziz
	 * 
	 */
	public abstract class PromotionsAdapter extends BaseAdapter
	{
		protected boolean				withTitle						= false;
		protected String				URL								= "url_";
		protected Context				context;

		public PromotionsAdapter(Context context, boolean withTitle)
		{
			this.context = context;
			this.withTitle = withTitle;
		}

		/** Set the amount of promotion lines you wish to have in the adapter **/
		protected abstract int getAmountOfPromotions();

		/** Set the item text/description for a given row position **/
		protected abstract String getItemText(int position);

		/** Set the Market URL for a given row position **/
		protected abstract String getItemURL(int position);

		/** Set the icon image for a given row position **/
		protected abstract Drawable getItemIconDrawable(int position);

		/** Set the title second part which is the bigger one **/
		protected abstract String getTitleBigText();

		/**
		 * Listview has more fields if its with title , because another section is added ( "also available:")
		 * 
		 */
		@Override
		public int getCount()
		{
			if (!withTitle)
			{
				return getAmountOfPromotions();
			}
			return getAmountOfPromotions() + 1; // amount of promotion + title row
		}

		@Override
		public Object getItem(int arg0)
		{
			return null;
		}

		@Override
		public long getItemId(int arg0)
		{
			return 0;
		}

		/** Get a view contianing the layout of a normal row */
		protected View setNormalRowView(int position, LayoutInflater inflater, Drawable iconDrawable, String text)
		{
			View rowView = inflater.inflate(R.layout.promotion_row_layout, null, false);
			TextView txt = (TextView) rowView.findViewById(R.id.promotion_row_view_text);
			txt.setText(text);
			ImageView icon = (ImageView) rowView.findViewById(R.id.promotion_row_icon);
			icon.setImageDrawable(iconDrawable);
			setRowViewOnClickListener(rowView, position);
			return rowView;
		}

		/** Get a view with the title layout **/
		protected View setTitleRowView(int position, LayoutInflater inflater, Drawable iconDrawable, String smallText, String bigText)
		{
			View rowView = inflater.inflate(R.layout.promotion_row_title_layout, null, false);
			TextView txtView = (TextView) rowView.findViewById(R.id.promotion_row_view_text);
			txtView.setText(smallText);
			TextView txtView2 = (TextView) rowView.findViewById(R.id.promotion_row_view_text2);
			txtView2.setText(Html.fromHtml(bigText));
			ImageView icon = (ImageView) rowView.findViewById(R.id.promotion_row_icon);
			icon.setImageDrawable(iconDrawable);
			setRowViewOnClickListener(rowView, position);
			return rowView;
		}

		/** Get the View containing only text **/
		protected View setTextRowView(int position, LayoutInflater inflater)
		{
			// Texts without link and icon
			View rowView = inflater.inflate(R.layout.promotion_text_row_layout, null, false);
			TextView txt = (TextView) rowView.findViewById(R.id.promotion_text_row_textview);
			txt.setText(getItemText(position));
			return rowView;
		}

		/** Sets what to when given view is clicked **/
		protected void setRowViewOnClickListener(View rowView, int position)
		{
			final int pos = position;

			rowView.setOnClickListener(new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					openMarketLink(getItemURL(pos));
				}
			});
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = null;
			if (!withTitle)
			{
				rowView = onNoTitleSetting(position, inflater);
			}
			else
			{
				rowView = onWithTitleSetting(position, inflater);
			}

			return rowView;
		}

		/** How to set the view when no title is needed **/
		protected View onNoTitleSetting(int position, LayoutInflater inflater)
		{
			View rowView;
			// / If dialog without title and texts , then show it all in the same normal row view layout
			if (position == 0)
			{
				rowView = setNormalRowView(position, inflater, getItemIconDrawable(position), getItemText(position));
			}
			else
			{
				rowView = setNormalRowView(position + 1, inflater, getItemIconDrawable(position + 1), getItemText(position + 1));
			}
			return rowView;
		}

		/** How to set the view when title is needed **/
		protected View onWithTitleSetting(int position, LayoutInflater inflater)
		{
			View rowView;
			if (position == 0)
			{
				rowView = setTitleRowView(position, inflater, getItemIconDrawable(position), getItemText(position), getTitleBigText());
			}
			else
			{
				rowView = setNormalRowView(position, inflater, getItemIconDrawable(position), getItemText(position));
			}
			return rowView;
			// }
		}

		/** This functions opnes Google Play app page if possible, otherwise, the browser is opened **/
		
		protected void openMarketLink(String link)
		{
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			try
			{
				context.startActivity(goToMarket);
			}
			catch (final ActivityNotFoundException e)
			{
				goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
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
}
