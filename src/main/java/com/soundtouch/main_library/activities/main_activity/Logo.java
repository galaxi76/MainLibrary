package com.soundtouch.main_library.activities.main_activity;
import android.widget.ImageView;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;

/** a data class for the logos on the toolbar of the main activity */
public class Logo
  {
  /** the imageView of the logo */
  public ImageView imageView;
  /** the idle image resource id of the logo , when the user doesn't click on it */
  public int       normalImage;
  /** the pressed image resource id of the logo , when the user touch on it */
  public int       pressedImage;
  /** the associated category of the logo */
  public Category  categoty;
  }
