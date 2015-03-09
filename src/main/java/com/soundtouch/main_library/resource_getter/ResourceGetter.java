package com.soundtouch.main_library.resource_getter;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;

import com.resources_extractor.FileInfo;
import com.resources_extractor.ResourceExtractorInputStream;
import com.resources_extractor.ResourcesExtractor;
import com.resources_extractor.ResourcesExtractor.ExtractorInputStreamManager;
import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.activities.main_activity.CategoriesThumbnailsManager.Category;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


public class ResourceGetter
{
	protected static final String	CLASS_TAG								= ResourceGetter.class.getCanonicalName();
	private static final String		FULL_SCREEN_IMAGE_FILE_FORMAT			= "%s%d.jpg";
	protected static final String	FULL_SCREEN_SOUND_FILE_FORMAT			= "%s%d.mp3";
	private static final String		FULL_SCREEN_SPEECH_FILE_FORMAT			= "%s%d_speak_l_%s.mp3";
	protected static final String	FULL_SCREEN_SPECIAL_SOUND_FILE_FORMAT	= "%s%d_%s.mp3";
	private static final String		FULL_SCREEN_SPECIAL_IMAGE_FILE_FORMAT	= "%s%d_%s.jpg";
	private static final String		SAVED_FILES_MAPPING_BEFORE				= "savedFilesMappingBefore";
	private ResourcesExtractor		_resourcesExtractor;
	private final String			_fullPathToMergedFile;
	private boolean					_isInitialized							= false;

	public boolean isInitialized()
	{
		return _isInitialized;
	}

	public String getFullPathToMergedFile()
	{
		return _fullPathToMergedFile;
	}

	public void setIsInitialized(final boolean isInitialized)
	{
		_isInitialized = isInitialized;
	}

	public ResourceGetter(final Context context)
	{
		_fullPathToMergedFile = getFullPathToMergedFile(context);
	}

	public static void resetResourceGetter(final Context context)
	{
		final SharedPreferences sharedPreferences = context.getSharedPreferences(CLASS_TAG, Context.MODE_PRIVATE);
		sharedPreferences.edit().putBoolean(SAVED_FILES_MAPPING_BEFORE, false).commit();
	}

	@SuppressWarnings("static-method")
	protected ExtractorInputStreamManager createExtractprInputStream(final String fullPathToMergedFile)
	{
		Logger.log(LogLevel.DEBUG, "got full path to merged file : " + fullPathToMergedFile);
		return new ExtractorInputStreamManager()
		{
			@Override
			public InputStream getNewInputStream()
			{
				FileInputStream fileInputStream = null;
				try
				{
					fileInputStream = new FileInputStream(fullPathToMergedFile);
				}
				catch (final FileNotFoundException e)
				{
					Logger.log(LogLevel.WARNING, "error while opening merged file " + fullPathToMergedFile + ":" + e);
				}
				return fileInputStream;
			}
		};
	}

	public boolean loadFileMappingIfPossible(final Context context)
	{
		if (isInitialized())
			return true;
		final SharedPreferences sharedPreferences = context.getSharedPreferences(CLASS_TAG, Context.MODE_PRIVATE);
		final boolean savedFilesMappingBefore = sharedPreferences.getBoolean(SAVED_FILES_MAPPING_BEFORE, false);
		final FileMappingDbHelper dbHelper = new FileMappingDbHelper(context);
		// if we already saved the file info mapping in the past, fetch it from the db:
		if (savedFilesMappingBefore)
		{
			final HashMap<String, FileInfo> fileMapping = dbHelper.getFileMapping();
			_resourcesExtractor = new ResourcesExtractor(createExtractprInputStream(_fullPathToMergedFile), fileMapping);
			setIsInitialized(true);
			return true;
		}
		return false;
	}

	/** fetches file mapping from the merged file and stores the mapping for future use, or , if already saved before , just loads it . */
	public void initializeFilesMapping(final Context context)
	{
		if (loadFileMappingIfPossible(context))
		{
			Logger.log(LogLevel.DEBUG, "loading file mapping is possible , returning");
			return;

		}
		final SharedPreferences sharedPreferences = context.getSharedPreferences(CLASS_TAG, Context.MODE_PRIVATE);
		final FileMappingDbHelper dbHelper = new FileMappingDbHelper(context);
		// create the file info mapping and save it into the db
		try
		{
			final HashMap<String, FileInfo> fileInfoMap = getResourcesExtractor().getFileInfoMap();
			dbHelper.addFileMapping(fileInfoMap);
			sharedPreferences.edit().putBoolean(SAVED_FILES_MAPPING_BEFORE, true).commit();
			setIsInitialized(true);
		}
		catch (final Exception e)
		{
			Logger.log(LogLevel.WARNING, "error while mapping file info:" + e);
		}
	}

	public static String getFullPathToMergedFile(final Context context)
	{
		final String result = App.getMergedFileFullPath();
		return result;
	}

	public boolean hasInnerFile(final String fileName)
	{
		final boolean result = getResourcesExtractor().hasInnerFile(fileName);
		return result;
	}

	public FileInfo getFileInfo(final String fileName)
	{
		final HashMap<String, FileInfo> fileInfoMap = getResourcesExtractor().getFileInfoMap();
		if (fileInfoMap == null)
			return null;
		final FileInfo fileInfo = fileInfoMap.get(fileName);
		return fileInfo;
	}

	protected ResourcesExtractor getResourcesExtractor()
	{
		if (_resourcesExtractor == null)
			_resourcesExtractor = new ResourcesExtractor(createExtractprInputStream(_fullPathToMergedFile));
		return _resourcesExtractor;
	}

	public boolean isFullScreenImageExist(final String subCategoryName, final int clicks)
	{
		final String fullScreenImageFileName = String.format(FULL_SCREEN_IMAGE_FILE_FORMAT, subCategoryName, clicks);
		return hasInnerFile(fullScreenImageFileName);
	}

	public Bitmap getFullScreenImage(final String subCategoryName, final int clicks) throws IOException
	{
		final String locale = SettingsActivity.getSpeechLocale();
		// this led to numbers being formatted to local formats which caused Arabic to fail poorly !
		// String fullScreenImageFileName = String.format(FULL_SCREEN_SPECIAL_IMAGE_FILE_FORMAT, subCategoryName, clicks, locale);
		String fullScreenImageFileName = subCategoryName + clicks + "_" + locale + ".jpg";
		if (hasInnerFile(fullScreenImageFileName))
		{
			Logger.log(LogLevel.DEBUG, "has inner file");
			return readBitmapFromMergedFile(fullScreenImageFileName);

		}
		// fullScreenImageFileName = String.format(FULL_SCREEN_IMAGE_FILE_FORMAT, subCategoryName, clicks);
		// this led to numbers being formatted to local formats which caused Arabic to fail poorly !
		fullScreenImageFileName = subCategoryName + clicks + ".jpg";
		Logger.log(LogLevel.DEBUG, "doesnt have inner file : " + String.format(FULL_SCREEN_IMAGE_FILE_FORMAT, subCategoryName, clicks));
		return readBitmapFromMergedFile(fullScreenImageFileName);
	}

	protected Bitmap readBitmapFromMergedFile(final String innerFileName) throws IOException
	{
		// Logger.log(LogLevel.DEBUG, "reading image file " + innerFileName);
		// TODO: allow downscaling the image (during reading) to fit to the correct size , in order to save memory and reduce loading time .
		if (!getResourcesExtractor().hasInnerFile(innerFileName))
		{
			Logger.log(LogLevel.DEBUG, "doesnt have inner file for " + innerFileName + ", returing null");
			return null;

		}

		// final long startTime = System.currentTimeMillis();
		ResourceExtractorInputStream inputStream = null;
		inputStream = getResourcesExtractor().getInputStream(innerFileName);
		if (inputStream == null)
			throw new IOException();
		Bitmap result = null;
		result = BitmapFactory.decodeStream(inputStream);
		// TODO consider downsampling when possible , especially for large images . downsample to the best size needed
		try
		{
			inputStream.close();
			// final long endTime = System.currentTimeMillis();
			// Logger.log(LogLevel.DEBUG, "done reading image file in " + (endTime - startTime) + " ms");
		}
		catch (final IOException e)
		{
			Logger.log(LogLevel.WARNING, "couldn't input stream " + e.getMessage());
		}
		return result;
	}

	@SuppressWarnings("unused")
	protected void playAudioOfInnerFile(final Context context, final MediaPlayer mediaPlayer, final String innerFileName) throws Exception
	{
		// Logger.log(LogLevel.DEBUG, "playing audio file " + innerFileName);
		final FileInfo fileInfo = getFileInfo(innerFileName);
		if (fileInfo == null)
			return;
		final long offsetToContent = fileInfo.getOffsetToContent();
		final long fileSize = fileInfo.getFileSize();
		final String fullPathToMergedSoundFile = getFullPathToMergedFile();
		final FileInputStream fileInputStream = new FileInputStream(fullPathToMergedSoundFile);
		final FileDescriptor fileDescriptor = fileInputStream.getFD();
		try
		{
			mediaPlayer.setDataSource(fileDescriptor, offsetToContent, fileSize);
			mediaPlayer.setOnPreparedListener(new OnPreparedListener()
			{

				@Override
				public void onPrepared(MediaPlayer mp)
				{
					Logger.log(LogLevel.DEBUG, "does it enter onprepared ? ");
					mediaPlayer.start();
					// TODO : ADDED THIS PIECE OF CODE
				}
			});

			mediaPlayer.prepareAsync(); // ADDED THIS AS WELL.

		}
		finally
		{
			fileInputStream.close();
		}
	}

	/**
	 * @param category
	 * @return
	 */
	public void playAudio(final Context context, final MediaPlayer mediaPlayer, final Category category, final String subCategoryName, final int clicks) throws Exception
	{
		final String locale = SettingsActivity.getSpeechLocale();
//		String audioFileName = String.format(FULL_SCREEN_SPECIAL_SOUND_FILE_FORMAT, subCategoryName, clicks, locale);
		String audioFileName = subCategoryName+clicks+"_"+locale+".mp3";
		if (hasInnerFile(audioFileName))
		{
			playAudioOfInnerFile(context, mediaPlayer, audioFileName);
			return;
		}
//		audioFileName = String.format(FULL_SCREEN_SOUND_FILE_FORMAT, subCategoryName, clicks);
		audioFileName = subCategoryName + clicks +".mp3";
		playAudioOfInnerFile(context, mediaPlayer, audioFileName);
	}

	/**
	 * @param category
	 * @return
	 */
	public void playSpeech(final Context context, final MediaPlayer mediaPlayer, final Category category, final String subCategoryName, final int clicks) throws Exception
	{
		final String locale = SettingsActivity.getSpeechLocale();
//		final String audioFileName = String.format(FULL_SCREEN_SPEECH_FILE_FORMAT, subCategoryName, clicks, locale);
		final String audioFileName = subCategoryName+clicks + "_speak_l_"+locale+".mp3";
		playAudioOfInnerFile(context, mediaPlayer, audioFileName);
	}
}
