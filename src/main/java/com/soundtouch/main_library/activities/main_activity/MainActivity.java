package com.soundtouch.main_library.activities.main_activity;

//note:for emulators, use the next arguments: " -partition-size 1024 "
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.InversableDialog;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.R;
import com.soundtouch.main_library.SoundTouch2PromotionDialog;
import com.soundtouch.main_library.VideoTouchPromotionalDesignedDialog;
import com.soundtouch.main_library.ViewUtil;
import com.soundtouch.main_library.activities.BaseActivity;
import com.soundtouch.main_library.activities.full_screen_image_activity.FullScreenImageActivity;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;
import com.soundtouch.main_library.activities.main_activity.ShakeListener.OnShakeListener;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


/**
 * the main activity , which shows a grid of thumbnails and a toolbar of categories to choose from
 */
public class MainActivity extends BaseActivity
{
//MY CHANGE
    private static final String CHANGES = "made changes";
	private static final String	PROMOTIONAL_DIALOG_BEEN_SHOWN_BEFORE	= "PromotionalDialogBeenShownBefore";
	private static final String	SOUNDTOUCH2_PACKAGENAME					= "com.soundtouch2";
	private static final String	SP_STRING_HIDDEN_ITEM					= "SP_STRING_HIDDEN_ITEM";
	private static final String	SP_INDEX_HIDDEN_ITEM					= "SP_INDEX_HIDDEN_ITEM";
	private boolean				isShakeEnabled							= true;

	protected enum PromotionType
	{
		VIDEO_TOUCH, SOUNDTOUCH2
	};

	public enum AutoPilotState
	{
		ALL_CATEGORIES, ANIMALS_WILDLIFE_BIRDS, ANIMALS_ONLY, WILDLIFE_ONLY, BIRDS_ONLY, HOUSEHOLD_ONLY, MUSICAL_INSTR_ONLY, VEHICLES_ONLY, NO_PILOT
	}

	private static final String		CLASS_TAG_SHARED_PREFERENCES	= MainActivity.class.getCanonicalName();

	public static final int			LOGO_CLICK_TIME_IN_MS			= 400;
	private static final int		MINIMUM_SPLASH_SCREEN_TIME		= 2000;
	private static final int		ENABLE_SHAKE_INTERVAL			= 1500;
	private ShakeListener			shakeListener;
	protected ThumbnailsAdapter		_thumbnailsAdapter				= null;
	protected AndroidTimer			_timer							= null;
	private boolean					_isSplashScreenShown			= true;
	protected boolean				isShakeResponseActive			= true;
	private boolean					_isStartingActivity				= false;
	protected GridView				gridView;
	protected Random				rdmGen							= new Random();
	protected MediaPlayer			audioMediaPlayer;
	private static final String		AUDIO_PATH						= "mix_sound";
	protected boolean				_checkItemsAmountOnStart		= false;
	protected LayoutFadeAnimator	layoutFadeAnimator;
	protected final PromotionType	DEFAULTPROMOTION				= PromotionType.VIDEO_TOUCH;
	ViewGroup						mainContainer;

	public Category getCurrentCategory()
	{
		if (_thumbnailsAdapter == null)
			return Category.UNKNOWN;
		return _thumbnailsAdapter.getCategory();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_checkItemsAmountOnStart = true;
		App.global().resetThumbnailsStates();
		setContentView(R.layout.main_activity);
		mainContainer = (ViewGroup) findViewById(R.id.mainActivity_mainContainer);

		if (!getApplicationContext().getResources().getBoolean(R.bool.settings_activity_picture_label_enabled))
			setLogosClickableState(false);
		if (_thumbnailsAdapter == null)
			_thumbnailsAdapter = App.global().getNewThumbnailsAdapter();
		final View toolbarView = findViewById(R.id.mainActivity_toolbar);
		// TODO:find a solution of the bottom toolbar that will work for all
		// devices , or at least put this logic in the appropriate projects
		switch (App.getDeviceType())
		{
			case KINDLE_FIRE:
				final RelativeLayout.LayoutParams kindleLayoutParams = (android.widget.RelativeLayout.LayoutParams) toolbarView.getLayoutParams();
				kindleLayoutParams.bottomMargin = +20;
				toolbarView.setLayoutParams(kindleLayoutParams);
				break;
			case NOOK_TABLET:
				final RelativeLayout.LayoutParams nookLayoutParams = (android.widget.RelativeLayout.LayoutParams) toolbarView.getLayoutParams();
				nookLayoutParams.bottomMargin = +35;
				toolbarView.setLayoutParams(nookLayoutParams);
				break;
			default:
				break;
		}
		// a workaround in order to overcome gridview's bugs when it's forced to
		// be without any scrolling:
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
		// gridview
		gridView = (GridView) findViewById(R.id.mainActivity_gridview);
		ViewUtil.runJustBeforeBeingDrawn(gridView, new Runnable()
		{
			@Override
			public void run()
			{
				setImageAdapterForGridView(gridView);
			}
		});
		// gridView = (GridView) findViewById(R.id.mainActivity_gridview); //TODO : CHANGED HERE
		layoutFadeAnimator = new LayoutFadeAnimator(gridView);
		initializeMixSoundPlayer();

		// logos
		setLogosTouchListeners();
		doExtraWork();
	}

	protected void doExtraWork(){
		
	}
	/** enables or disables the logos to be clickable . */
	protected void setLogosClickableState(final boolean enableToClick)
	{
		findViewById(R.id.mainActivity_logo_animals).setEnabled(enableToClick);
		findViewById(R.id.mainActivity_logo_vehicles).setEnabled(enableToClick);
		findViewById(R.id.mainActivity_logo_wildanimals).setEnabled(enableToClick);
		findViewById(R.id.mainActivity_logo_wildbirds).setEnabled(enableToClick);
		findViewById(R.id.mainActivity_logo_musicalinstruments).setEnabled(enableToClick);
		findViewById(R.id.mainActivity_logo_household).setEnabled(enableToClick);
	}

	protected void setMainActivityOrientation()
	{
		setOrientation((FrameLayout) findViewById(R.id.mainActivity_mainContainer));
	}

	@Override
	protected void onPause()
	{
		isShakeResponseActive = false;
		if (shakeListener != null)
		{
			shakeListener.setOnShakeListener(null);
		}
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		if (SettingsActivity.getAutoPilotCategories() != AutoPilotState.NO_PILOT && !_isSplashScreenShown)
		{
			onAutoPilotEnabled(SettingsActivity.getAutoPilotCategories());
		}
		mainContainer.setVisibility(View.VISIBLE);
		setMainActivityOrientation();
		// respond to shake events when activity is resumed
		setOnShakeResponse();
		isShakeResponseActive = true;
		// enable screen touch response after pre-defined amount of time
		checkAmountOfItemsToShow();
		_isStartingActivity = false;
		_checkItemsAmountOnStart = false;
		hideScaryAnimalIfNeeded();
		unhideScaryAnimalIfNeeded();
		checkValidityOfMergedFile(this, new Runnable()
		{
			@Override
			public void run()
			{
				onMergedFileValidated();
			}
		});
		super.onResume();
	}

	protected void checkAmountOfItemsToShow()
	{
		if (SettingsActivity.isAmountOfItemsChanged() || _checkItemsAmountOnStart)
		{
			_thumbnailsAdapter.setNumOfItemsToShow(SettingsActivity.getAmountOfItems());
			_thumbnailsAdapter.setCategory(_thumbnailsAdapter._superCategory);
			_thumbnailsAdapter.initializeImageAdapter(gridView, _thumbnailsAdapter.getGridViewHeight());
			gridView.setAdapter(_thumbnailsAdapter);
			goToCategory(Category.ANIMALS, false);
			// goToCategory(Category.ANIMALS);
		}
	}

	protected void onMergedFileValidated()
	{
		// showWhatsNewDialogIfNeeded();
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event)
	{
		if (event.getAction() != KeyEvent.ACTION_UP)
			return super.onKeyUp(keyCode, event);
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_MENU:
				startActivity(App.global().getSettingsActivityIntent(this));
				return true;
			case KeyEvent.KEYCODE_BACK:

				return backButtonAction();
				// case KeyEvent.KEYCODE_BACK:
				// MainActivity.this.moveTaskToBack(true);
				// return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	protected boolean backButtonAction()
	{
		if (_isStartingActivity)
		{
			return true;
		}

		if (!deviceHasPermanentMenuKey())
		{
			startActivity(App.global().getSettingsActivityIntent(this));
			return true;
		}
		showExitDialog();
		return false;
	}

	/** State what to do when user exits app **/
	protected void onExitAction()
	{
	}

	@Override
	public void finish()
	{
		onExitAction();
		super.finish();
	}

	/** Show exit dialog asking the user if he wishes to exit the app when pressing on back button **/
	protected void showExitDialog()
	{

		final InversableDialog exitDialog = new InversableDialog(this);
		exitDialog.setText(getString(R.string.main_activity_exit_dialog_desc));
		exitDialog.setTitle(getString(R.string.main_activity_exit_dialog_title));
		exitDialog.setPositiveButton(getString(R.string.main_activity_exit_dialog_positive_button), new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				exitDialog.dismiss();
				finish();
			}
		});
		exitDialog.setNegativeButton(getString(R.string.main_activity_exit_dialog_negative_button), new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				exitDialog.dismiss();
			}
		});
		exitDialog.show();
	}

	/** sets touch listeners for each of the logos on the bottom toolbar */
	protected void setLogosTouchListeners()
	{
		final Logo[] logos = new Logo[ThumbnailsAdapter.NUMBER_OF_CATEGORIES];
		for (int i = 0; i < logos.length; ++i)
			logos[i] = new Logo();
		int categoryIndex = CategoriesThumbnailsManager.Category.ANIMALS.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.ANIMALS, R.id.mainActivity_logo_animals, R.drawable.logo_animals, R.drawable.logo_animals_blur_effect);
		//
		categoryIndex = CategoriesThumbnailsManager.Category.VEHICLES.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.VEHICLES, R.id.mainActivity_logo_vehicles, R.drawable.logo_vehicles, R.drawable.logo_vehicles_blur_effect);
		//
		categoryIndex = CategoriesThumbnailsManager.Category.WILD_ANIMALS.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.WILD_ANIMALS, R.id.mainActivity_logo_wildanimals, R.drawable.logo_wildanimals, R.drawable.logo_wildanimals_blur_effect);
		//
		categoryIndex = CategoriesThumbnailsManager.Category.WILD_BIRDS.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.WILD_BIRDS, R.id.mainActivity_logo_wildbirds, R.drawable.logo_wildbirds, R.drawable.logo_wildbirds_blur_effect);
		//
		categoryIndex = CategoriesThumbnailsManager.Category.MUSICAL_INSTRUMENTS.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.MUSICAL_INSTRUMENTS, R.id.mainActivity_logo_musicalinstruments, R.drawable.logo_musicalinstruments, R.drawable.logo_musicalinstruments_blur_effect);
		//
		categoryIndex = CategoriesThumbnailsManager.Category.HOUSEHOLD.getCategoryNumber();
		initLogo(categoryIndex, logos, Category.HOUSEHOLD, R.id.mainActivity_logo_household, R.drawable.logo_household, R.drawable.logo_household_blur_effect);
		//
		for (int i = 0; i < logos.length; ++i)
		{
			final Logo currentLogo = logos[i];
			currentLogo.categoty = Category.fromValue(i);
			currentLogo.imageView.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(final View arg0, final MotionEvent arg1)
				{
					switch (arg1.getAction())
					{
						case MotionEvent.ACTION_DOWN:
							if (_timer != null)
								_timer.stopScheduleAndRunTask();
							_timer = null;
							currentLogo.imageView.setImageResource(currentLogo.pressedImage);
							goToCategory(currentLogo.categoty, false);
							return true;
						case MotionEvent.ACTION_UP:
							_timer = new AndroidTimer(new Runnable()
							{
								@Override
								public void run()
								{
									currentLogo.imageView.setImageResource(currentLogo.normalImage);
								}
							}, LOGO_CLICK_TIME_IN_MS);
							_timer.startSchedule();
							return true;
						default:
							return true;
					}
				}
			});
		}
	}

	protected void initLogo(int categoryIndex, Logo[] logos, Category category, int imgViewID, int normalDrawableResource, int blurredDrawableResource)
	{
		categoryIndex = category.getCategoryNumber();
		logos[categoryIndex].imageView = (ImageView) findViewById(imgViewID);
		logos[categoryIndex].normalImage = normalDrawableResource;
		logos[categoryIndex].pressedImage = blurredDrawableResource;
	}

	protected void onCategorySwitch(final Category category)
	{
		_thumbnailsAdapter.setCategory(category);
		hideScaryAnimalIfNeeded();
		_thumbnailsAdapter.notifyDataSetChanged();
	}

	/**
	 * sets the category to show the next time that you call the image adapter to update itself(via notifyDataSetChanged)
	 */
	protected void goToCategory(final Category category, boolean enableAnimation)
	{

		// if (getCurrentCategor() == category)
		// return;
		if (!enableAnimation)
		{
			onCategorySwitch(category);
			return;
		}

		layoutFadeAnimator.setOnAnimationStartRunnable(new IFadeAnimationStarter()
		{

			@Override
			public void onStart()
			{
				onCategorySwitch(category);

			}
		});
		layoutFadeAnimator.startAnimation();
	}

	/** sets the image adapter to the gridview of thumbnails ... */
	protected void setImageAdapterForGridView(final GridView gridview)
	{
		final ImageView splashScreen = new ImageView(this);
		final int gridViewHeightFull = gridview.getMeasuredHeight();
		// gridViewHeightFull);
		// gridview
		gridview.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(final AdapterView<?> parent, final View v, final int position, final long id)
			{
				if (_isStartingActivity)
					return;
				_isStartingActivity = true;
				final Intent intent = prepareFullScreenImageActivity(position, getCurrentCategory());
				startActivity(intent);
			}
		});
		// caching mechanism , while showing splash screen:
		splashScreen.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		splashScreen.setScaleType(ScaleType.FIT_XY);
		splashScreen.setImageResource(R.drawable.main_activity_splash_screen);
		final View toolbarView = findViewById(R.id.mainActivity_toolbar);
		gridview.setVisibility(View.INVISIBLE);
		toolbarView.setVisibility(View.INVISIBLE);
		mainContainer.addView(splashScreen, 0);
		// try to load file mapping if was saved before , and if not ,
		// initialize it while showing a progress dialog:
		final boolean isFileMappingAvailable = App.global().loadFileMappingIfPossible();
		new AsyncTask<Void, Boolean, Void>()
		{
			private ProgressDialog	progressDialog;

			@Override
			protected Void doInBackground(final Void ... args)
			{
				Logger.log(LogLevel.DEBUG, "has to initialize file mapping ");

				final long startTime = System.currentTimeMillis();
				if (!isFileMappingAvailable)
					publishProgress(false);
				App.global().getResourcesManager().initializeFilesMapping(MainActivity.this);
				publishProgress(true);
				_thumbnailsAdapter.initializeImageAdapter(gridview, gridViewHeightFull);
				final long endTime = System.currentTimeMillis();
				final long timeToWait = MINIMUM_SPLASH_SCREEN_TIME - (endTime - startTime);
				if (timeToWait > 0 && timeToWait <= MINIMUM_SPLASH_SCREEN_TIME)
					try
					{
						Thread.sleep(timeToWait);
					}
					catch (final InterruptedException e)
					{
					}
				return null;
			}

			@Override
			protected void onProgressUpdate(final Boolean ... values)
			{
				if (values == null || values.length == 0)
					return;
				final boolean isFileMappingAvailableNow = values[0];
				if (progressDialog != null && isFileMappingAvailableNow)
				{
					progressDialog.dismiss();
					progressDialog = null;
				}
				if (!isFileMappingAvailableNow && progressDialog == null)
				{
					final String progressDialogTitle = getString(R.string.main_activity_initializing_progress_dialog_title);
					final String progressDialogDescription = getString(R.string.main_activity_initializing_progress_dialog_description);
					progressDialog = ProgressDialog.show(MainActivity.this, progressDialogTitle, progressDialogDescription, true, false);
				}
			};

			@Override
			protected void onPostExecute(final Void result)
			{
				gridview.setVisibility(View.VISIBLE);
				toolbarView.setVisibility(View.VISIBLE);
				gridview.setAdapter(_thumbnailsAdapter);
				mainContainer.removeView(splashScreen);
				_isSplashScreenShown = false;
				onFinishedWithSplashScreen();

			}
		}.execute();
	}

	protected boolean isAppInstalledOnDevice(String packageName)
	{
		PackageManager pm = getPackageManager();
		try
		{
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			return true;
		}
		catch (NameNotFoundException e)
		{
			return false;
		}
	}

	/** Check if exitting app is requested , and exit **/
	@Override
	protected void onNewIntent(Intent intent)
	{
		if (intent.getExtras() == null)
		{
			return;
		}
		boolean shouldExit = intent.getExtras().getBoolean(SettingsActivity.EXIT_APP_EXTRA);
		if (shouldExit)
		{
			finish();
		}
		super.onNewIntent(intent);
	}

	/** opens the activity that shows the full image of the chosen thumbnail */
	protected Intent prepareFullScreenImageActivity(final int thumbnailIndex, Category currentCategory)
	{
		final Intent intent = App.global().getFullScreenImageActivityIntent(MainActivity.this);
		final String thumbnailImage = App.global().getCategoriesThumbnailsManager().getThumbnailImage(currentCategory, thumbnailIndex);
		FullScreenImageActivity.addImageRequestToIntent(intent, currentCategory, thumbnailImage);
		return intent;
	}

	/** What to do when finished with showing splash screen **/
	protected void onFinishedWithSplashScreen()
	{
		showPromotionDialogIfNeeded();
		initializeShakeListener();
		if (SettingsActivity.getAutoPilotCategories() != AutoPilotState.NO_PILOT)
		{
			onAutoPilotEnabled(SettingsActivity.getAutoPilotCategories());
		}
	}

	protected boolean isSplashScreenShown()
	{
		return _isSplashScreenShown;
	}

	/********************************************************************************************
	 *POPUP LOGIC ******************************************************* ******************
	 */
	private void showPromotionDialogIfNeeded()
	{
		if (!getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).getBoolean(PROMOTIONAL_DIALOG_BEEN_SHOWN_BEFORE + App.getAppVersionCode(), false))
		{
			showPromotionDialog(this, DEFAULTPROMOTION);
			getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).edit().putBoolean(PROMOTIONAL_DIALOG_BEEN_SHOWN_BEFORE + App.getAppVersionCode(), true).commit();
		}
	}

	/** This shows the dialog iff it was never shown before **/
	protected void showPromotionDialog(Context context, PromotionType promotionType)
	{
		switch (promotionType)
		{
			case VIDEO_TOUCH:
				showVideoTouchPromotion();
				break;
			case SOUNDTOUCH2:
				showSoundTouch2Promotion();
				break;
		}

	}

	/** Detect if SoundTouch2 is installed on the device and act properly **/
	protected void showSoundTouch2Promotion()
	{
		if (isAppInstalledOnDevice(SOUNDTOUCH2_PACKAGENAME))
		{
			return; // If its already installed on device then do not show dialog
		}
		final SoundTouch2PromotionDialog stProm = App.global().getSoundTouch2PromotionDialog(this, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				isShakeResponseActive = true; // re-activate shake response after dismissing dialog
			}
		});
		stProm.show();
		isShakeResponseActive = false; // deactivate shake response while showing dialog
	}

	/** Detect if SoundTouch2 is installed on the device and act properly **/
	protected void showVideoTouchPromotion()
	{
		if (isAppInstalledOnDevice("com.insectsvideotouch"))
		{
			return; // If its already installed on device then do not show dialog
		}

		final VideoTouchPromotionalDesignedDialog vtProm = new VideoTouchPromotionalDesignedDialog(this, new DialogInterface.OnClickListener()
		{

			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
				isShakeResponseActive = true; // re-activate shake response after dismissing dialog
			}
		});
		vtProm.show();
		isShakeResponseActive = false; // deactivate shake response while showing dialog
	}

	/********************************************************************************************
	 * ITEM HIDE LOGIC ******************************************************* ******************
	 */

	protected void unhideScaryAnimalIfNeeded()
	{
		if (!SettingsActivity.isHideItem())
		{
			// If new event has occured, and user has unchecked the hide snake property, then initialize all thumbnail arrays.
			if (_thumbnailsAdapter.getCategory() == getCategoryHoldingHiddenAnimal())
			{
				if (!SettingsActivity.isNewHideEvent())
				{
					return;
				}
				_thumbnailsAdapter.getThumbnailsArray().set(getHiddenItemIndexFromSP(), getHiddenItemNameFromSP());
				_thumbnailsAdapter.notifyDataSetChanged();
			}

			return;
		}
	}

	/** This method is used to hide unwanted animals and replace them with others **/
	protected void hideScaryAnimalIfNeeded()
	{
		Logger.log(LogLevel.DEBUG, "hide scary animal222");
		if (!SettingsActivity.isHideItem())
		{
			unhideScaryAnimalIfNeeded();
		}

		// If user selected hide, then hide it and rreplace it with random item
		else
		{

			int indexToHide = getItemIndex(getScaryAnimalName(), _thumbnailsAdapter.getThumbnailsArray());
			if (indexToHide != -1)
			{
				int replaceIndex = pickRandomFarIndex(indexToHide, _thumbnailsAdapter.getThumbnailsArray().size(), 3);
				String replaceString = _thumbnailsAdapter.getThumbnailsArray().get(replaceIndex);
				_thumbnailsAdapter.getThumbnailsArray().set(indexToHide, replaceString);
				saveHiddenItemToSP(indexToHide, getScaryAnimalName());
				_thumbnailsAdapter.notifyDataSetChanged();

			}
		}

	}

	protected void saveHiddenItemToSP(int index, String name)
	{
		getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).edit().putInt(SP_INDEX_HIDDEN_ITEM, index).commit();
		getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).edit().putString(SP_STRING_HIDDEN_ITEM, name).commit();
	}

	protected String getHiddenItemNameFromSP()
	{
		return getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).getString(SP_STRING_HIDDEN_ITEM, "");
	}

	protected int getHiddenItemIndexFromSP()
	{
		return getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).getInt(SP_INDEX_HIDDEN_ITEM, -1);

	}

	protected Category getCategoryHoldingHiddenAnimal()
	{
		return Category.WILD_ANIMALS;
	}

	/** Pick an index which is not neighboring the given one . **/
	protected int pickRandomFarIndex(int index, int arrayRange, int numOfColumns)
	{

		int indexPicked = rdmGen.nextInt(arrayRange);
		while (indexPicked >= index - numOfColumns - 1 && indexPicked <= index + numOfColumns + 1)
		{
			indexPicked = rdmGen.nextInt(arrayRange);
		}
		return indexPicked;
	}

	protected int getItemIndex(String itemName, ArrayList<String> names)
	{
		for (int i = 0; i < names.size(); i++)
		{
			if (names.get(i).equals(itemName))
			{
				return i;
			}
		}
		return -1;
	}

	protected String getScaryAnimalName()
	{
		return "snake";
	}

	/********************************************************************************************
	 * AUTO PILOT LOGIC ******************************************************* ******************
	 */
	protected void onAutoPilotEnabled(AutoPilotState state)
	{
		startAutoPilot(state);
	}

	/** This function picks random item from given category without having duplicates **/
	protected int[] pickRandomItem(Category[] categoriesToPickFrom, int range)
	{
		int categoryIndx = rdmGen.nextInt(categoriesToPickFrom.length); // pick random category from the given set of categories given
		int itemIndx = rdmGen.nextInt(range); // pick a random item in this category
		int[] result = {categoryIndx, itemIndx};
		return result;
	}

	protected void startAutoPilot(AutoPilotState state)
	{
		mainContainer.setVisibility(View.INVISIBLE);
		Category[] categoriesToPickFrom = getCategoriesToPickFrom(state);
		int[] indicesPicked = pickRandomItem(categoriesToPickFrom, SettingsActivity.getAmountOfItems());
		Category categoryToPickFrom = categoriesToPickFrom[indicesPicked[0]];
		int itemIndxToPick = indicesPicked[1];
		startActivity(prepareFullScreenImageActivity(itemIndxToPick, categoryToPickFrom));
	}

	/** Get the sub cateogry if amount of items is below 12 **/
	protected Category getSubCategory(Category givenCategory)
	{
		return App.global().getCategoriesThumbnailsManager().getSubCategory(givenCategory, SettingsActivity.getAmountOfItems());
	}

	/** Get the categories to pick thumbnails from based on the auto pilot state **/
	protected Category[] getCategoriesToPickFrom(AutoPilotState autoPilotState)
	{
		switch (autoPilotState)
		{
			case ALL_CATEGORIES:
				Category[] allCategories = {getSubCategory(Category.ANIMALS), getSubCategory(Category.WILD_ANIMALS), getSubCategory(Category.WILD_BIRDS), getSubCategory(Category.VEHICLES), getSubCategory(Category.MUSICAL_INSTRUMENTS), getSubCategory(Category.HOUSEHOLD)};
				return allCategories;
			case ANIMALS_WILDLIFE_BIRDS:
				Category[] animalsCategoires = {getSubCategory(Category.ANIMALS), getSubCategory(Category.WILD_ANIMALS), getSubCategory(Category.WILD_BIRDS)};
				return animalsCategoires;
			case ANIMALS_ONLY:
				Category[] result = {getSubCategory(Category.ANIMALS)};
				return result;
			case WILDLIFE_ONLY:
				Category[] result1 = {getSubCategory(Category.WILD_ANIMALS)};
				return result1;
			case BIRDS_ONLY:
				Category[] result2 = {getSubCategory(Category.WILD_BIRDS)};
				return result2;
			case VEHICLES_ONLY:
				Category[] result3 = {getSubCategory(Category.VEHICLES)};
				return result3;
			case HOUSEHOLD_ONLY:
				Category[] result4 = {getSubCategory(Category.HOUSEHOLD)};
				return result4;
			case MUSICAL_INSTR_ONLY:
				Category[] result5 = {getSubCategory(Category.MUSICAL_INSTRUMENTS)};
				return result5;
			default:
				return null;
		}
	}

	/******************************************************************************************************
	 * THUMBNAILS SCRAMBLE LOGIC **************************************************************************
	 */

	/** Sets what to do as a response of the device shake event **/
	protected void setOnShakeResponse()
	{
		if (shakeListener == null)
		{
			return;
		}
		shakeListener.setOnShakeListener(new OnShakeListener()
		{
			@Override
			public void onShake()
			{
				if (isShakeResponseActive && isShakeEnabled())
				{
					isShakeResponseActive = false;
					// Mix thumbnails with animation when the device is shaked
					mixThumbnailsWithAnimation();
					ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
					Runnable task = new Runnable()
					{

						@Override
						public void run()
						{
							isShakeResponseActive = true;
						}
					};
					worker.schedule(task, ENABLE_SHAKE_INTERVAL, TimeUnit.MILLISECONDS);
				}

			}
		});
	}

	protected void initializeShakeListener()
	{

		shakeListener = new ShakeListener(this);
		setOnShakeResponse();
	}

	protected void disableShake()
	{
		isShakeEnabled = false;
	}

	protected void enableShake()
	{
		isShakeEnabled = true;
	}

	protected boolean isShakeEnabled()
	{
		return isShakeEnabled;
	}

	/**
	 * This method is called when shake event is detected , it scrambles the thumbnails in the gridview with animation
	 **/
	protected void mixThumbnailsWithAnimation()
	{
		setLogosClickableState(false); // user can't click logos
		playSoundWhenMixing();
		GridViewSwapAnimator swapAnimator = new GridViewSwapAnimator(gridView, _thumbnailsAdapter, new SwapEndListener()
		{

			@Override
			public void onSwapEnd()
			{ // set logos clickable when swap process is finished.
				setLogosClickableState(true);
			}
		});
		int gridViewChildCount = _thumbnailsAdapter.getCount();
		int[] randomIndicesArr = new int[_thumbnailsAdapter.getCount()];
		// randomIndicesArr[0]=1;
		for (int i = 0; i < gridViewChildCount / 2; i++)
		{
			// Pick first index
			int rdmIdx1 = pickRandomIndx(randomIndicesArr);
			// Pick second index
			int rdmIdx2 = pickRandomIndx(randomIndicesArr);
			final boolean last;
			if (i == gridViewChildCount / 2 - 1)
			{
				last = true;

			}

			else
			{
				last = false;
			}
			swapAnimator.swapAnimation(rdmIdx1, rdmIdx2, last);

		}
	}

	/**
	 * Given an array indicating chosen indices , this method returns an available index to be used
	 **/
	protected int pickRandomIndx(int[] randomIndicesArr)
	{
		int rdmIdx1 = rdmGen.nextInt(randomIndicesArr.length);
		while (1 == randomIndicesArr[rdmIdx1])
		{
			// If number was already picked then keep trying
			rdmIdx1 = rdmGen.nextInt(randomIndicesArr.length);
		}
		randomIndicesArr[rdmIdx1] = 1;
		return rdmIdx1;
	}

	protected void playSoundWhenMixing()
	{
		audioMediaPlayer.start();

	}

	public void initializeMixSoundPlayer()
	{
		int resID = getResources().getIdentifier(AUDIO_PATH, "raw", getPackageName());

		try
		{
			audioMediaPlayer = MediaPlayer.create(this, resID);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// /** Send request for server to get which promotion dialog to show **/
	// private void setPromotionRequest()
	// {
	// if (!App.isAppUpdated())
	// { // TODO : uncomment this
	// // return;
	// }
	// getSharedPreferences(CLASS_TAG_SHARED_PREFERENCES, 0).edit().putBoolean(PROMOTION_DIALOG_WAS_SHOWN_SHARED_PREFERENCE, true).commit();
	//
	// final IResponseListener responseListener = new IResponseListener()
	// {
	// @Override
	// public void onReceivedResponse(final String responseString)
	// {
	// PromotionType promotionDialogType;
	// if (responseString == null || responseString.length() == 0)
	// {
	// promotionDialogType = DEFAULTPROMOTION;
	// }
	// else
	// try
	// {
	// // final int responseInt = 0;
	// final int responseInt = Integer.parseInt(responseString);
	// switch (responseInt)
	// {
	// case 0:
	// promotionDialogType = PromotionType.SOUNDTOUCH2; // should be videotouch
	// break;
	// case 1:
	// promotionDialogType = PromotionType.SOUNDTOUCH2;
	// break;
	// default:
	// promotionDialogType = DEFAULTPROMOTION;
	//
	// }
	// }
	// catch (final Exception e)
	// {
	// promotionDialogType = DEFAULTPROMOTION;
	// }
	// showPromotionDialog(MainActivity.this, promotionDialogType);
	// }
	// };
	// final String url = "http://www.soundtouchinteractive.com/ad-network-chooser.html";// getString(R.string.promotion_dialog_request_url);
	// final Bundle message = null;
	// final PostMethod postMethod = PostMethod.GET;
	// ConnectionManager.postMessage(url, message, postMethod, responseListener);
	// }

}
