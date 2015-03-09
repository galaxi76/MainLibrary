package com.soundtouch.main_library;

import java.util.Locale;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.google.android.vending.expansion.downloader.Helpers;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.activities.full_screen_image_activity.FullScreenImageActivity;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager;
import com.soundtouch.main_library.activities.main_activity.MainActivity;
import com.soundtouch.main_library.activities.main_activity.ThumbnailsAdapter;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;
import com.soundtouch.main_library.resource_getter.ResourceGetter;
import com.soundtouch.main_library.resource_getter.ResourcesManager;


/** the application class */
public class App extends Application
{
	private static final String				LAST_APP_VERSION_CODE					= "lastAppVersion";
	protected static final String			PROMOTION_DIALOG_WAS_SHOWN				= "promotionDialogShown";
	protected static final String			CLASS_TAG								= App.class.getCanonicalName();
	protected static App					_context;
	protected ResourcesManager				_resourcesManagerSingleton				= null;
	protected CategoriesThumbnailsManager	_categoriesThumbnailsManagerSingleton	= null;
	private Point							_screenSize;
	protected static boolean				_formerOrientation						= false;
	protected static boolean				_isUpdated								= false;
	

	public enum AndroidDeviceType
	{
		OTHER, KINDLE_FIRE, NOOK_TABLET
	}
	
	public static boolean isUsingAmazon() {
	    return android.os.Build.MANUFACTURER.equals("Amazon")
	            && (android.os.Build.MODEL.equals("Kindle Fire")
	                || android.os.Build.MODEL.startsWith("KF"));
	}

	public App()
	{
		_context = this;
		Logger.setApplicationTag("soundtouch");

	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		_formerOrientation = SettingsActivity.isScreenOrientationChecked();
		final int widthPixels = getResources().getDisplayMetrics().widthPixels;
		final int heightPixels = getResources().getDisplayMetrics().heightPixels;
		_screenSize = new Point(widthPixels, heightPixels);
		final SharedPreferences sharedPreferences = getSharedPreferences(CLASS_TAG, Context.MODE_PRIVATE);
		Logger.log(LogLevel.DEBUG, "version code " + getAppVersionCode());
		final int lastAppVersionCode = sharedPreferences.getInt(LAST_APP_VERSION_CODE, getAppVersionCode());
		final int currentAppVersionCode = getAppVersionCode();
		// when updating , reset the files mapping:
		if (lastAppVersionCode != currentAppVersionCode)
		{
			_isUpdated = true;
			ResourceGetter.resetResourceGetter(_context);
		}
		sharedPreferences.edit().putInt(LAST_APP_VERSION_CODE, currentAppVersionCode).commit();

	}

	public static int getAppVersionCode()
	{
		try
		{
			final Context context = App.global();
			final int currentVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			return currentVersionCode;
		}
		catch (final NameNotFoundException e)
		{
			Logger.log(LogLevel.WTF, "couldn't find current app ?!");
			return 0;
		}
	}

	public static String getMergedFileFullPath()
	{
		final Context context = App.global();
		final int versionCode = getAppVersionCode();
		final String expansionAPKFileName = Helpers.getExpansionAPKFileName(context, true, versionCode);
		final String result = Helpers.generateSaveFileName(context, expansionAPKFileName);
		return result;
	}

	/** resets the thumbnails state :forgets how many times the user pressed each thumbnail */
	public void resetThumbnailsStates()
	{
		_categoriesThumbnailsManagerSingleton = null;
	}

	/** return the app context */
	public static App global()
	{
		return _context;
	}

	/** returns a new image adapter for the thumbnails */
	public ThumbnailsAdapter getNewThumbnailsAdapter()
	{
		final ThumbnailsAdapter thumbnailsAdapter = new ThumbnailsAdapter(getCategoriesThumbnailsManager());
		return thumbnailsAdapter;
	}

	/** returns the category thumbnails manager, used by the image adapter to get the correct thumbnails images for the current category */
	public CategoriesThumbnailsManager getCategoriesThumbnailsManager()
	{
		if (_categoriesThumbnailsManagerSingleton == null)
			_categoriesThumbnailsManagerSingleton = new CategoriesThumbnailsManager();
		return _categoriesThumbnailsManagerSingleton;
	}

	/** returns the full screen manager, used by the fullScreenImageActivity , in order to show an image and play a sound */
	public ResourcesManager getResourcesManager()
	{
		if (_resourcesManagerSingleton == null)
			_resourcesManagerSingleton = new ResourcesManager(this);
		return _resourcesManagerSingleton;
	}

	public boolean isFileMappingAvailable()
	{
		final ResourcesManager resourcesManager = getResourcesManager();
		final boolean mergedFileAvailable = resourcesManager.isMergedFileAvailable(this);
		if (!mergedFileAvailable)
			return false;
		final boolean initialized = resourcesManager.isInitialized();
		return initialized;
	}

	/** tries to load from previously saved file mapping .return true iff it exists and loaded successfully */
	public boolean loadFileMappingIfPossible()
	{
		final ResourcesManager resourcesManager = getResourcesManager();
		final boolean fileMappingAvailable = resourcesManager.loadFileMappingIfPossible(this);
		return fileMappingAvailable;
	}

	/** returns an intent to the fullScreenImageActivity */
	@SuppressWarnings("static-method")
	public Intent getFullScreenImageActivityIntent(final Context context)
	{
		return new Intent(context, FullScreenImageActivity.class);
	}
	
	public SoundTouch2PromotionDialog getSoundTouch2PromotionDialog(Context context , DialogInterface.OnClickListener onOKClickeListener){
		return new SoundTouch2PromotionDialog(context,onOKClickeListener);
	}
	

	@SuppressWarnings("static-method")
	public Intent getMainActivityIntent(final Context context)
	{
		return new Intent(context, MainActivity.class);
	}

	public Intent getSettingsActivityIntent(Context context)
	{
		return new Intent(context, SettingsActivity.class);

	}

	public static boolean isRunningOnEmulator()
	{
		final boolean result = "google_sdk".equals(Build.PRODUCT) || "sdk".equals(Build.PRODUCT) || Build.FINGERPRINT.startsWith("generic");
		return result;
	}

	public static AndroidDeviceType getDeviceType()
	{
		final String manufacturer = android.os.Build.MANUFACTURER;
		final String model = android.os.Build.MODEL;
		if ("Amazon".equals(manufacturer) && "Kindle Fire".equals(model))
			return AndroidDeviceType.KINDLE_FIRE;
		if ("bn".equals(manufacturer) && "NookColor".equals(model))
			return AndroidDeviceType.NOOK_TABLET;
		return AndroidDeviceType.OTHER;
	}

	/** returns the path on the external storage that is associated with the application.upon uninstallation of the application , this folder will be automatically be revmoved */
	public static String getApplicationExternalStoragePath(final Context context, final String targetFolderName)
	{
		if (context == null)
			return null;
		if (targetFolderName == null)
			return Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName();
		return Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/" + targetFolderName;
	}

	public Point getScreenSize()
	{
		return _screenSize;
	}

	public static int getGlobalScreenOrientation()
	{
		if (SettingsActivity.isScreenOrientationChecked())
		{
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		}
		return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	}

	public static boolean isReverseEvent()
	{
		if (_formerOrientation != SettingsActivity.isScreenOrientationChecked())
		{
			_formerOrientation = SettingsActivity.isScreenOrientationChecked();
			return true;
		}
		return false;
	}

	public static boolean isScaleSupported()
	{
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < android.os.Build.VERSION_CODES.HONEYCOMB)
		{
			return false;
		}
		return true;
	}

	public static boolean isReversePortraitSupported()
	{
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion < android.os.Build.VERSION_CODES.GINGERBREAD)
		{
			return false;
		}
		return true;
	}

	public static boolean isAppUpdated()
	{
		return _isUpdated;
	}

	/** Set language to be the one given , if setDefaultLanguage is false , then the language selected in settings will be set **/
	public void setLocale(Activity activity, boolean setDefaultLanguage)
	{
		Configuration conf = getResources().getConfiguration();
		String language = "";
		if (!setDefaultLanguage)
		{
			language = SettingsActivity.getSpeechLocale();
		}
		else
		{
			language = Locale.getDefault().getDisplayLanguage();
		}
		conf.locale = new Locale(language);
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Resources resources = new Resources(getAssets(), metrics, conf);
	}
}
