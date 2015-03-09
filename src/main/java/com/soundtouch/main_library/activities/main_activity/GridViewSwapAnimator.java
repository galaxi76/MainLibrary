package com.soundtouch.main_library.activities.main_activity;

import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.GridView;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


public class GridViewSwapAnimator
{
	private final static int	DURATION	= 1000;
	protected SwapEndListener	swapListener;
	private ThumbnailsAdapter	adapter;
	private GridView			gridView;
	Handler						handler;

	public GridViewSwapAnimator(GridView gridView, ThumbnailsAdapter adapter, SwapEndListener swapListener)
	{
		this.gridView = gridView;
		this.adapter = adapter;
		this.swapListener = swapListener;
		handler = new Handler();
	}

	/** Get the distance between two views **/
	protected int[] getDistancesBetweenViews(FrameLayout img1, FrameLayout img2)
	{
		int[] result = new int[2];
		int[] loc1 = new int[2];
		int[] loc2 = new int[2];
		img1.getLocationOnScreen(loc1);
		img2.getLocationOnScreen(loc2);
		int img1X = loc1[0];
		int img1Y = loc1[1];
		int img2X = loc2[0];
		int img2Y = loc2[1];
		int img1XDist = img2X - img1X;
		result[0] = img1XDist;
		int img1YDist = img2Y - img1Y;
		result[1] = img1YDist;
		return result;
	}

	/** Start the swap animation between two indices **/
	public void swapAnimation(final int indx1, final int indx2, final boolean last)
	{
		final FrameLayout img1 = (FrameLayout) gridView.getChildAt(indx1);
		final FrameLayout img2 = (FrameLayout) gridView.getChildAt(indx2);
		int[] deltasBetweenViews = getDistancesBetweenViews(img1, img2);
		int img1XDist = deltasBetweenViews[0]; // get the X delta
		int img1YDist = deltasBetweenViews[1]; // get the Y delta
		// / reversed
		if (SettingsActivity.isScreenOrientationChecked() && App.isScaleSupported())
		{
			img1XDist *= -1;
			img1YDist *= -1;
		}

		TranslateAnimation anim1 = new TranslateAnimation(0, img1XDist, 0, img1YDist);
		TranslateAnimation anim2 = new TranslateAnimation(0, -img1XDist, 0, -img1YDist);
		anim2.setDuration(DURATION);
		anim1.setDuration(DURATION);
		anim2.setFillAfter(true);
		anim1.setFillAfter(true);
		anim1.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				// Swap two indices in the array containing the thumbnail names **/

				new Handler().post(new Runnable()
				{
					public void run()
					{
						adapter.swapTwoIndicesInThumbArrayAndCache(indx1, indx2);
						// gridView.setAdapter(adapter);
						// adapter.notifyDataSetChanged();
						// Since notfiyDataSetChange led to some problems in the gridview , setAdapter has been used instead **/
						if (last)
						{
							adapter.notifyDataSetChanged();
							swapListener.onSwapEnd();
						}

					}
				});

			}
		});
		img2.startAnimation(anim2);
		img1.startAnimation(anim1);

	}

}
