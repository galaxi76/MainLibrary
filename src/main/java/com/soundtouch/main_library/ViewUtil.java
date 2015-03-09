package com.soundtouch.main_library;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;


public class ViewUtil
{
  /** a helper function to run a specific delegate function (runnable) after the specified view was measured , before it was drawn */
  public static void runJustBeforeBeingDrawn(final View view, final Runnable runnable)
  {
    final OnPreDrawListener preDrawListener = new OnPreDrawListener()
    {
      @Override
      public boolean onPreDraw()
      {
        // Logger.log(LogLevel.DEBUG, "onpredraw");
        runnable.run();
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        return true;
      }
    };
    view.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
  }

  @SuppressWarnings("deprecation")
  @TargetApi(16)
  public static void setViewBackgroundDrawable(final View view, final Drawable background)
  {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
      view.setBackground(background);
    else
      view.setBackgroundDrawable(background);
  }

  public static float convertDpToPixel(final float dp)
  {
    final Resources resources = App.global().getResources();
    final DisplayMetrics metrics = resources.getDisplayMetrics();
    final float px = dp * (metrics.densityDpi / 160f);
    return px;
  }

  public static float convertPixelsToDp(final float px)
  {
    final Resources resources = App.global().getResources();
    final DisplayMetrics metrics = resources.getDisplayMetrics();
    final float dp = px / (metrics.densityDpi / 160f);
    return dp;
  }
}
