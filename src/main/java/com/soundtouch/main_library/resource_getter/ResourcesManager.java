package com.soundtouch.main_library.resource_getter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


/** a class that manages the image to show and sound to play for the activity that shows them (FullScreenImageActivity) */
public class ResourcesManager
{
	private static final int				DEFAULT_FIRST_INDEX_IN_FILES	= 1;
	private static final String				PICTURES_LABELS_FILE_FORMAT		= "pictures_labels_%s";
	protected final HashMap<String, String>	_picturesLabels					= new HashMap<String, String>();
	protected int							_thumbnailsClicks				= DEFAULT_FIRST_INDEX_IN_FILES;
	protected final ResourceGetter			_resourceGetter;
	private MediaPlayer						_mediaPlayer;
	private OnCompletionListener			_onCompletionListener;

	public ResourcesManager(final Context context)
	{
		_resourceGetter = getNewResourceGetter(context);
	}

	@SuppressWarnings("static-method")
	protected ResourceGetter getNewResourceGetter(final Context context)
	{
		return new ResourceGetter(context);
	}

	public void initializeFilesMapping(final Context context)
	{
		// TODO handle exceptions in a more elegant way
		refreshLabelsSettings(context);
		if (_resourceGetter != null)
		{
			_resourceGetter.initializeFilesMapping(context);
			
		}
	}

	protected ResourceGetter getResourceGetter()
	{
		return _resourceGetter;
	}

	public boolean isInitialized()
	{
		final boolean isInitialized = getResourceGetter().isInitialized();
		return isInitialized;
	}

	/** tries to read from previously saved file mapping .return true iff it exists and loaded successfully */
	public boolean loadFileMappingIfPossible(final Context context)
	{
		final boolean isInitialized = getResourceGetter().loadFileMappingIfPossible(context);
		return isInitialized;
	}

	public Bitmap getFullScreenImage(final Category category, final String thumbnailName) throws IOException
	{
		final int clicks = getNumberOfClicks(thumbnailName);
		Logger.log(LogLevel.DEBUG, "showing image.category:" + category + " thumbnailName:" + thumbnailName + " clicks:" + clicks);
		final Bitmap result = getResourceGetter().getFullScreenImage(thumbnailName, clicks);
		return result;
	}

	@SuppressWarnings("unused")
	protected synchronized int getNumberOfClicks(final String subCategoryName)
	{
		return _thumbnailsClicks;
	}

	public void playAudio(final Context context, final Category category, final String subCategoryName) throws Exception
	{
		final int clicks = getNumberOfClicks(subCategoryName);
		Logger.log(LogLevel.DEBUG, "playing audio.category:" + category + " subCategory:" + subCategoryName + " clicks:" + clicks);
		stopAllAudioPlaying();
		final MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(getOnCompletionListener());
		setMediaPlayer(mediaPlayer);
		getResourceGetter().playAudio(context, _mediaPlayer, category, subCategoryName, clicks);
	}

	public void stopAllAudioPlaying()
	{
		if (_mediaPlayer == null)
			return;
		_mediaPlayer.setOnCompletionListener(null);
		if (_mediaPlayer.isPlaying())
			_mediaPlayer.stop();
		_mediaPlayer.reset();
		_mediaPlayer.release();
		_mediaPlayer = null;
	}

	public void playSpeech(final Context context, final Category category, final String subCategoryName) throws Exception
	{
		final int clicks = getNumberOfClicks(subCategoryName);
		Logger.log(LogLevel.DEBUG, "playing speech .category:" + category + " subCategory:" + subCategoryName + " clicks:" + clicks);
		stopAllAudioPlaying();
		final MediaPlayer mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnCompletionListener(getOnCompletionListener());
		setMediaPlayer(mediaPlayer);
		getResourceGetter().playSpeech(context, _mediaPlayer, category, subCategoryName, clicks);
	}

	protected boolean isThumbnailAvailable(final String thumbnailName, final int clicks)
	{
		return getResourceGetter().isFullScreenImageExist(thumbnailName, clicks);
	}

	public synchronized void increamentSelectionOfThumbnail(final String thumbnailName)
	{
		Logger.log(LogLevel.DEBUG, "now should increment selection of thumbnails");
		if (thumbnailName == null)
			return;
		++_thumbnailsClicks;
		if (!isThumbnailAvailable(thumbnailName, _thumbnailsClicks))
			_thumbnailsClicks = DEFAULT_FIRST_INDEX_IN_FILES;
		Logger.log(LogLevel.DEBUG, "new clicks count for " + thumbnailName + " was changed to " + _thumbnailsClicks);
		return;
	}

	@SuppressWarnings("static-method")
	public boolean isMergedFileAvailable(final Context context)
	{
		final String fullPathToOutputFile = ResourceGetter.getFullPathToMergedFile(context);
		if (fullPathToOutputFile == null)
			return false;
		final File f = new File(fullPathToOutputFile);
		return f.exists();
	}

	protected void setMediaPlayer(final MediaPlayer mediaPlayer)
	{
		_mediaPlayer = mediaPlayer;
	}

	public void setOnCompletionListener(final OnCompletionListener onCompletionListener)
	{
		_onCompletionListener = onCompletionListener;
	}

	public OnCompletionListener getOnCompletionListener()
	{
		return _onCompletionListener;
	}

	// //////////////////
	// labels handling //
	// //////////////////
	/** returns true iff the label feature is supported for the current language */
	public static boolean isLabelSupportedInCurrentLanguage()
	{
		final Context context = App.global();
		final String speechLocale = SettingsActivity.getSpeechLocale();
		final String picturesLabelResName = String.format(PICTURES_LABELS_FILE_FORMAT, speechLocale.toLowerCase());
		final int categoryNameResId = context.getResources().getIdentifier(picturesLabelResName, "raw", context.getPackageName());
		final boolean isSupported = categoryNameResId != 0;
		return isSupported;
	}

	public void refreshLabelsSettings(final Context context)
	{
		final String speechLocale = SettingsActivity.getSpeechLocale();
		final String picturesLabelResName = String.format(PICTURES_LABELS_FILE_FORMAT, speechLocale.toLowerCase());
		_picturesLabels.clear();
		if (!SettingsActivity.isLabelEnabled())
			return;
		final int categoryNameResId = context.getResources().getIdentifier(picturesLabelResName, "raw", context.getPackageName());
		if (categoryNameResId == 0)
			return;
		LabelFileParser.parsePictureLabels(categoryNameResId, _picturesLabels);
	}

	public String getPictureLabel(final String subCategoryName)
	{
		if (subCategoryName == null || subCategoryName.length() == 0 || _picturesLabels.isEmpty())
			return "";
		final int clicks = getNumberOfClicks(subCategoryName);
		final String key = subCategoryName.toLowerCase() + clicks;
		if (!_picturesLabels.containsKey(key))
			return "";
		final String result = _picturesLabels.get(key);
		return result;
	}
}
