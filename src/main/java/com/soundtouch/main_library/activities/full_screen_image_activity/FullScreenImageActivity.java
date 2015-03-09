package com.soundtouch.main_library.activities.full_screen_image_activity;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.App.AndroidDeviceType;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.R;
import com.soundtouch.main_library.ViewUtil;
import com.soundtouch.main_library.activities.BaseActivity;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;
import com.soundtouch.main_library.activities.main_activity.MainActivity.AutoPilotState;
import com.soundtouch.main_library.activities.main_activity.ShakeListener;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity.CloseItemBy;
import com.soundtouch.main_library.resource_getter.ResourcesManager;


/**
 * the activity that shows a full screen image while playing a sound of that image
 */
public class FullScreenImageActivity extends BaseActivity
{
	/** a delay to be used after playing speech . used only for kindle fire. */
	// private static final int LOCK_SCREEN_DURATION = 4000;
	private static final int	DELAY_AFTER_SPEECH			= 500;
	private static final int	SCREEN_INITIAL_FREEZE		= 500;
	private static final String	INTENT_THUMBNAIL_NAME		= "thumbnailName";
	private static final String	INTENT_CATEGORY				= "category";
	protected String			_thumbnailName;
	protected Category			_category;
	private ShakeListener		_shakeListener;
	private Handler				_handler;
	/** used to poll the volume of the device */
	private AudioManager		_audioManager;
	private final Runnable		_playSoundRunnable;
	private ContentObserver		_volumeChangedObserver;
	private final boolean		_isListeningToVolumeChanges	= false;
	private View				_fullScreenBackgroundView;
	private Bitmap				_imageToShow;
	private boolean				isOnScreenFreeze			= true;
	private boolean				isOnScreenLock				= false;
	protected int				screenLockDuration			= 0;
	protected boolean			allowFinishingActivity		= true;

	public FullScreenImageActivity()
	{
		_playSoundRunnable = new Runnable()
		{
			@Override
			public void run()
			{
				stopSound();
				try
				{
					final ResourcesManager resourcesManager = App.global().getResourcesManager();
					resourcesManager.playAudio(FullScreenImageActivity.this, _category, _thumbnailName);
				}
				catch (final Exception e)
				{
					goToDownloaderActivity(FullScreenImageActivity.this);
				}
			}
		};
	}

	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setScreenLockDuration(SettingsActivity.getLockScreenDuration());
		_volumeChangedObserver = new ContentObserver(new Handler())
		{
			@Override
			public void onChange(final boolean selfChange)
			{
				super.onChange(selfChange);
				showNoSoundIndicatorIfNeeded();
			}
		};
		_handler = new Handler();
		_audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// get data from previous activity , in order to know which image to
		// show:
		_thumbnailName = getIntent().getStringExtra(INTENT_THUMBNAIL_NAME);
		_category = (Category) getIntent().getSerializableExtra(INTENT_CATEGORY);
		//
		freezeScreenOnLaunch();
		setContentView(getWhichLayoutToUse());
		setEnableListenToVolumeChanges(true);
		initializeActivity();

	}

	protected void freezeScreenOnLaunch()
	{
		Logger.log(LogLevel.DEBUG, "no should freeze screen");
		_handler.postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				// stop screen freeze after pre-defined amount of time
				isOnScreenFreeze = false;
				Logger.log(LogLevel.DEBUG, "initial freeze finished");
			}
		}, SCREEN_INITIAL_FREEZE);
	}

	@SuppressWarnings("static-method")
	protected int getWhichLayoutToUse()
	{
		return R.layout.full_screen_image_activity;
	}

	private void initializeActivity()
	{
		try
		{
			showFullScreenImage();
			showLabelIfNeeded();
			setCloseScreenBy();
			// shaking handling:
			setShakeListener();
			playSound();
		}
		catch (final Exception e)
		{
			Logger.log(LogLevel.DEBUG, "exception caught while initializng22");

			checkValidityOfMergedFile(FullScreenImageActivity.this, new Runnable()
			{
				@Override
				public void run()
				{
					initializeActivity();
				}
			});
		}

	}

	protected void setScreenLockDuration(int duration)
	{
		screenLockDuration = duration;
	}

	protected void setShakeListener()
	{
		_shakeListener = new ShakeListener(this);
		_shakeListener.setOnShakeListener(new ShakeListener.OnShakeListener()
		{
			@Override
			public void onShake()
			{
				_handler.removeCallbacks(_playSoundRunnable);
				try
				{
					playSound();
				}
				catch (final Exception e)
				{
					Logger.log(LogLevel.DEBUG, "exception caught while initializng1");

					goToDownloaderActivity(FullScreenImageActivity.this);
				}
			}
		});
	}

	private void showLabelIfNeeded()
	{

		final AutoSizeTextView pictureLabelTextView = (AutoSizeTextView) findViewById(R.id.fullScreenImageActivity_pictureLabelTextView);
		if (pictureLabelTextView == null)
			return;
		if (!SettingsActivity.isLabelEnabled())
		{
			pictureLabelTextView.setVisibility(View.GONE);
		}
		// set constraints
		final Point screenSize = App.global().getScreenSize();
		// min and max font size
		final float maxFontSize = screenSize.x * 0.17f;
		final float minFontSize = screenSize.x * 0.1f;
		pictureLabelTextView.setMaxFontSize(maxFontSize);
		pictureLabelTextView.setMinFontSize(minFontSize);
		// padding
		final int paddingLeftAndRight = (int) (screenSize.x * 0.03f);
		final int paddingTopAndBottom = (int) ViewUtil.convertDpToPixel(5);
		pictureLabelTextView.setPadding(paddingLeftAndRight, paddingTopAndBottom, paddingLeftAndRight, paddingTopAndBottom);
		// margins
		final ViewGroup pictureLabelTextViewContainer = (ViewGroup) findViewById(R.id.fullScreenImageActivity_pictureLabelTextViewContainer);
		final int marginLeftAndRight = (int) (screenSize.x * 0.09f);
		final int marginBottom = (int) (screenSize.y * 0.08f);
		final MarginLayoutParams lp = (MarginLayoutParams) pictureLabelTextViewContainer.getLayoutParams();
		lp.setMargins(marginLeftAndRight, 0, marginLeftAndRight, marginBottom);
		pictureLabelTextViewContainer.setLayoutParams(lp);
		//

		final String pictureLabel = App.global().getResourcesManager().getPictureLabel(_thumbnailName);
		if (pictureLabel == null || pictureLabel.length() == 0)
			pictureLabelTextView.setVisibility(View.GONE);
		pictureLabelTextView.setText(pictureLabel);
	}

	private void setEnableListenToVolumeChanges(final boolean enable)
	{
		if (_isListeningToVolumeChanges == enable)
			return;
		if (enable)
			getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, _volumeChangedObserver);
		else
			getContentResolver().unregisterContentObserver(_volumeChangedObserver);
	}

	public static void addImageRequestToIntent(final Intent intentToThisActivity, final Category currentCategory, final String thumbnailName)
	{
		intentToThisActivity.putExtra(INTENT_THUMBNAIL_NAME, thumbnailName);
		intentToThisActivity.putExtra(INTENT_CATEGORY, currentCategory);
	}

	/**
	 * returns the id of the image to show for the current screen , based on the current category index and the current thumbnail index
	 * 
	 * @throws IOException
	 */
	protected Bitmap getWhichImageToShow() throws IOException
	{
		final Bitmap imageToShow = App.global().getResourcesManager().getFullScreenImage(_category, _thumbnailName);
		return imageToShow;
	}

	protected View.OnClickListener getImageOnClickListener()
	{
		Logger.log(LogLevel.DEBUG, "getting click listener ");

		final View.OnClickListener clickToExitListener = new View.OnClickListener()
		{

			@Override
			public void onClick(final View arg0)
			{
				if (!isOnScreenFreeze && !isOnScreenLock)
				{
					Logger.log(LogLevel.DEBUG, "exit activity is called from onclick");
					exitActivity();
				}
			}
		};
		return clickToExitListener;
	}

	protected void showFullScreenImage() throws IOException
	{
		// prepare how to go back (either from touching background or clicking a
		// button) :
		_fullScreenBackgroundView = findViewById(R.id.fullScreenImageActivity_rootLayout);

		_imageToShow = getWhichImageToShow();
		if (_imageToShow == null)
		{

			exitActivity();

		}
		else
			ViewUtil.setViewBackgroundDrawable(_fullScreenBackgroundView, new BitmapDrawable(getResources(), _imageToShow));
	}

	/**
	 * this method checks if the lock screen is enabled in settings , and delays the setting process of the onclick listener , sets it immediately otherwise
	 */
	public void checkDelayAndExecute()
	{
		isOnScreenLock = true;
		_handler.postDelayed(new Runnable()
		{

			@Override
			public void run()
			{
				isOnScreenLock = false;
			}
		}, screenLockDuration * 1000);
		// enable screen touch response after user-defined amount of time
	}

	public void setCloseScreenBy()
	{
		final CloseItemBy closeItemBy = SettingsActivity.getCloseItemBy();
		final ImageView backButton = (ImageView) findViewById(R.id.fullScreenImageActivity_backButton);

		//

		switch (closeItemBy)
		{
			case BUTTON:
				backButton.setVisibility(View.VISIBLE);
				backButton.setOnClickListener(getImageOnClickListener());
				final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) backButton.getLayoutParams();
				lp.weight *= 1;
				backButton.setLayoutParams(lp);
				/*
				 * ViewUtil.runJustBeforeBeingDrawn(backButton, new Runnable() {
				 * 
				 * @Override public void run() { LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) backButton.getLayoutParams(); lp.weight*=2; backButton.setLayoutParams(lp); } });
				 */
				break;
			case TOUCH:
				_fullScreenBackgroundView.setOnClickListener(getImageOnClickListener());
				backButton.setVisibility(View.GONE);
				break;
		}
	}

	protected void exitActivity()
	{

		Logger.log(LogLevel.DEBUG, "exit activity is called");
		if (isFinishing() || !allowFinishingActivity)
			return;
		//
		if (_shakeListener != null)
			_shakeListener.pause();
		_shakeListener = null;
		stopSound();
		incrementSelectionOfThumbnail();
		setEnableListenToVolumeChanges(false);
		if(SettingsActivity.getAutoPilotCategories() != AutoPilotState.NO_PILOT){
			_handler.postDelayed(new Runnable()
			{
				
				@Override
				public void run()
				{
					cleanScreen();
					finish();

					
				}
			}, 1000);
		}
		else{
			cleanScreen();
			finish();
		}
		// TODO try to fix weird bug that occurs sometimes on xoom , when moving
		// between activities rapidly , which causes the activity to go to
		// landscape for a very short time.
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
	}

	protected void incrementSelectionOfThumbnail()
	{
		if (_thumbnailName != null)
			App.global().getResourcesManager().increamentSelectionOfThumbnail(_thumbnailName);
	}

	protected void cleanScreen()
	{
		if (_fullScreenBackgroundView != null && _imageToShow != null)
		{
			ViewUtil.setViewBackgroundDrawable(_fullScreenBackgroundView, null);
			_imageToShow.recycle();
			_imageToShow = null;
			_fullScreenBackgroundView = null;
		}
	}

	/** shows/hides the sound indicator, based on the current volume state */
	protected void showNoSoundIndicatorIfNeeded()
	{
		final ImageView noSoundIndicator = (ImageView) findViewById(R.id.fullScreenImageActivity_noSoundIndicator);
		if (noSoundIndicator == null)
			return;
		final int musicVolume = _audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		// Logger.log(LogLevel.DEBUG, "new volume:" + musicVolume);
		final boolean isMute = musicVolume <= 0;
		noSoundIndicator.setVisibility(isMute ? View.VISIBLE : View.GONE);
	}

	/** stops any playing sound and also releases the mediaPlayer */
	@SuppressWarnings("static-method")
	protected void stopSound()
	{
		// TODO : uncomment this
		App.global().getResourcesManager().stopAllAudioPlaying();
	}

	/**
	 * plays a sound of the current shown image, but only if no sound is currently playing
	 * 
	 * @throws IOException
	 */
	protected void playSound() throws Exception
	{
		showNoSoundIndicatorIfNeeded();
		stopSound();
		// if speech is disabled, play just the audio , otherwise , play the
		// speech first , and then the audio
		if (!SettingsActivity.isSpeechEnabled())
		{
			App.global().getResourcesManager().setOnCompletionListener(getOnCompletionListenerInPilotMode());
			App.global().getResourcesManager().playAudio(this, _category, _thumbnailName);
		}

		else
		{
			App.global().getResourcesManager().setOnCompletionListener(new OnCompletionListener()
			{
				@Override
				public void onCompletion(final MediaPlayer mp)
				{
					App.global().getResourcesManager().setOnCompletionListener(getOnCompletionListenerInPilotMode());

					// TODO handle kindle fire bug of stopping sound too
					// early
					if (!SettingsActivity.isSpeechOnlyEnabled())
					{
						if (App.getDeviceType() == AndroidDeviceType.KINDLE_FIRE)
							_handler.postDelayed(_playSoundRunnable, DELAY_AFTER_SPEECH);
						else
							_playSoundRunnable.run();
					}
					else if(SettingsActivity.getAutoPilotCategories() != AutoPilotState.NO_PILOT)
					{
						// if speech ONLY is enabled, dont play sounds
						getOnCompletionListenerInPilotMode().onCompletion(mp);

					}

				}
			});
			App.global().getResourcesManager().playSpeech(this, _category, _thumbnailName);
		}
	}

	/** Get onCompletionListener that exitst the activity in case pilot mode is activated **/
	protected OnCompletionListener getOnCompletionListenerInPilotMode()
	{
		if (SettingsActivity.getAutoPilotCategories() != AutoPilotState.NO_PILOT)
		{
			return new OnCompletionListener()
			{

				@Override
				public void onCompletion(MediaPlayer mp)
				{
					exitActivity(); // exit activity in case auto pilot mode is activated, so it could navigate to the next full screen image
				}
			};
		}
		else
		{
			return null;
		}
	}

	protected void startSettingsActivity(){
		
	}
	/**
	 * occurs when the user presses a key. if pressed BACK button , will call exitActivity() to exit the activity
	 */
	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event)
	{
		if (event.getAction() != KeyEvent.ACTION_UP)
			return super.onKeyUp(keyCode, event);
		switch (keyCode)
		{
			case KeyEvent.KEYCODE_BACK:
				if (SettingsActivity.getAutoPilotCategories() == AutoPilotState.NO_PILOT)
				{
					exitActivity(); // exit activity only if auto pilot is not enabled
				}
				else if (!deviceHasPermanentMenuKey())
				{
					incrementSelectionOfThumbnail();
					startActivity(App.global().getSettingsActivityIntent(this));
					return true;
				}

				return true;
			case KeyEvent.KEYCODE_MENU:
				if (SettingsActivity.getAutoPilotCategories() == AutoPilotState.NO_PILOT)
				{
					exitActivity(); // exit activity only if auto pilot is not enabled
				}
				else
				{
					incrementSelectionOfThumbnail();
				}
				startActivity(App.global().getSettingsActivityIntent(this));
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
			case KeyEvent.KEYCODE_VOLUME_MUTE:
			case KeyEvent.KEYCODE_VOLUME_UP:
				_handler.post(new Runnable()
				{
					@Override
					public void run()
					{
						showNoSoundIndicatorIfNeeded();
					}
				});
				return false;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		setOrientation((ViewGroup) findViewById(R.id.fullScreenImageActivity_rootLayout));
		checkValidityOfMergedFile(this, new Runnable()
		{
			@Override
			public void run()
			{
				showNoSoundIndicatorIfNeeded();
				if (_shakeListener != null)
					_shakeListener.resume();
				setEnableListenToVolumeChanges(true);
			}
		});
		checkDelayAndExecute();

	}

	@Override
	public void onPause()
	{
		if (_shakeListener != null)
			_shakeListener.pause();
		stopSound();
		_handler.removeCallbacks(_playSoundRunnable);
		setEnableListenToVolumeChanges(false);
		super.onPause();
	}

}
