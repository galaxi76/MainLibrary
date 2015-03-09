package com.soundtouch.main_library.activities.downloader_activity;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.zip.CRC32;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Messenger;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.vending.expansion.downloader.DownloadProgressInfo;
import com.google.android.vending.expansion.downloader.DownloaderClientMarshaller;
import com.google.android.vending.expansion.downloader.DownloaderServiceMarshaller;
import com.google.android.vending.expansion.downloader.Helpers;
import com.google.android.vending.expansion.downloader.IDownloaderClient;
import com.google.android.vending.expansion.downloader.IDownloaderService;
import com.google.android.vending.expansion.downloader.IStub;
import com.google.android.vending.expansion.downloader.impl.DownloadNotification;
import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.R;
import com.soundtouch.main_library.XmlTag;


public class DownloaderActivity extends Activity implements IDownloaderClient
{
	private static final boolean								SKIP_LICENSE_VALIDATION	= false;
	private ProgressBar											_progressBar;
	private IDownloaderService									_remoteService;
	private IStub												_downloaderClientStub;
	private AlertDialog											_currentAlertDialog;
	private TextView											_progressTextView;
	private TextView											_categoryProgressTextView;
	private TextView											_categoryNameTextView;
	private ImageView											_categoryIconImageView;
	private ArrayList<XmlTag>									_allCategoriesTags;
	private int													_numberOfCategories,
			_categoriesSteps;
	private int													_currentCategoryIndex	= -1;
	private AsyncTask<Object, DownloadProgressInfo, Boolean>	_validationTask;
	boolean														_isActivityVisible		= false;
	private DownloadStatus										_downloadStatus			= DownloadStatus.NO_ERROR;
	boolean														alredyDelete			= false;

	enum DownloadStatus
	{
		NO_ERROR, ERROR_UNKNOWN, ERROR_EXTERNAL_STORAGE_UNAVAILABLE, ERROR_WITH_CONNECTION, ERROR_NOT_ENOUGH_SPACE
	}

	/**
	 * Called when the activity is first create; we wouldn't create a layout in the case where we have the file and are moving to another activity without downloading.
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		_isActivityVisible = true;
		/**
		 * Before we do anything, are the files we expect already here and delivered (presumably by Market) For free titles, this is probably worth doing. (so no Market request is necessary)
		 */

		final boolean isExpansionFilesDelivered = expansionFileDelivered();
		Logger.log(LogLevel.DEBUG, "is expansion delivered  " + isExpansionFilesDelivered);
		if (isExpansionFilesDelivered)
		{
			goToNextActivityAndFinish();
			return;
		}
		if (!alredyDelete)
			deleteOldMergedFile();
		if (SKIP_LICENSE_VALIDATION)
		{
			initializeDownloadUI();
			closeAlertDialogIfNeeded();
			_downloadStatus = DownloadStatus.NO_ERROR;
			verifyFileIntegrity();
			setIndeterminate(true);
			return;
		}
		_allCategoriesTags = XmlTag.getXmlRootTagOfXmlFileResourceId(this, R.xml.downloader_categories).getInnerXmlTags();
		_numberOfCategories = _allCategoriesTags.size();
		_categoriesSteps = 100 / _numberOfCategories;
		try
		{
			final Intent launchIntent = DownloaderActivity.this.getIntent();
			final Intent intentToLaunchThisActivityFromNotification = new Intent(DownloaderActivity.this, DownloaderActivity.this.getClass());
			intentToLaunchThisActivityFromNotification.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentToLaunchThisActivityFromNotification.setAction(launchIntent.getAction());
			if (launchIntent.getCategories() != null)
				for (final String category:launchIntent.getCategories())
					intentToLaunchThisActivityFromNotification.addCategory(category);
			// Build PendingIntent used to open this activity from Notification
			final PendingIntent pendingIntent = PendingIntent.getActivity(DownloaderActivity.this, 0, intentToLaunchThisActivityFromNotification, PendingIntent.FLAG_UPDATE_CURRENT);
			// Request to start the download
			final int startResult = DownloaderClientMarshaller.startDownloadServiceIfRequired(this, pendingIntent, DownloaderService.class);
			if (startResult != DownloaderClientMarshaller.NO_DOWNLOAD_REQUIRED)
			{
				// The DownloaderService has started downloading the files, show progress
				initializeDownloadUI();
				return;
			}
		}
		catch (final NameNotFoundException e)
		{
			Logger.log(LogLevel.WTF, "Cannot find own package:" + e);
		}
		initializeDownloadUI();
		// we assume that since we didn't start to download anything , download is already completed
		onDownloadStateChanged(IDownloaderClient.STATE_COMPLETED);
	}

	private boolean expansionFileDelivered()
	{
		// use the latest expansion version , based on the current
		int fileVersion = 0;
		final int versionCode = App.getAppVersionCode();
		fileVersion = versionCode;
		final String fileName = Helpers.getExpansionAPKFileName(this, true, fileVersion);
		Logger.log(LogLevel.DEBUG, "upd looking for file name : " + fileName);
		final long fileSize = Long.parseLong(getResources().getString(R.string.merged_file_size));
		Logger.log(LogLevel.DEBUG, "does file exist " + Helpers.doesFileExist(this, fileName, fileSize, false));
		if (!Helpers.doesFileExist(this, fileName, fileSize, false))
			return false;
		if (App.global().getResourcesManager().loadFileMappingIfPossible(this))
			return true;
		return false;
	}

	protected static long getCrcValue(final String filePath) throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			final CRC32 crc = new CRC32();
			final byte[] buf = new byte[1024 * 256];
			raf = new RandomAccessFile(filePath, "r");
			long totalBytesRemaining = raf.length();
			while (totalBytesRemaining > 0)
			{
				final int seek = (int) (totalBytesRemaining > buf.length ? buf.length : totalBytesRemaining);
				raf.readFully(buf, 0, seek);
				crc.update(buf, 0, seek);
				totalBytesRemaining -= seek;
				// Logger.log(LogLevel.DEBUG, "validation bytes left: " + totalBytesRemaining);
			}
			final long crcValue = crc.getValue();
			return crcValue;
		}
		catch (final IOException e)
		{
			throw e;
		}
		finally
		{
			if (raf != null)
				raf.close();
		}
	}

	/**
	 * validates that the downloaded file is ok , and only if it is , goes to the next activity
	 */
	void verifyFileIntegrity()
	{
		setIndeterminate(true);
		if (_validationTask != null)
			return;
		Logger.log(LogLevel.DEBUG, "verifying file integrity...");
		_validationTask = new AsyncTask<Object, DownloadProgressInfo, Boolean>()
		{
			@Override
			protected Boolean doInBackground(final Object ... params)
			{
				if (_remoteService == null && !SKIP_LICENSE_VALIDATION)
					return false;
				Logger.log(LogLevel.DEBUG, "crc check...");
				final String filePath = App.getMergedFileFullPath();
				if (filePath == null)
					return false;
				// do crc check:
				try
				{
					final long crcValue = getCrcValue(filePath);
					final long correctCrcValue = Long.parseLong(getResources().getString(R.string.merged_file_crc));
					final boolean passesCrcCheck = crcValue == correctCrcValue;
					Logger.log(LogLevel.DEBUG, "crc:" + crcValue + " isCrcOk? " + passesCrcCheck);
					if (!passesCrcCheck)
						return false;
				}
				catch (final Exception e)
				{
					Logger.log(LogLevel.WARNING, "cannot verify file:" + e);
					return false;
				}
				Logger.log(LogLevel.DEBUG, "loading file...");
				// Logger.log(LogLevel.DEBUG, "initializeFilesMapping");
				App.global().getResourcesManager().initializeFilesMapping(DownloaderActivity.this);
				// final check that initializion of the files mapping went well and is now available to be used:
				final boolean isFileMappingAvailable = App.global().isFileMappingAvailable();
				// Logger.log(LogLevel.DEBUG, "isInitialized:" + isFileMappingAvailable);
				return isFileMappingAvailable;
			}

			@Override
			protected void onPostExecute(final Boolean result)
			{
				Logger.log(LogLevel.DEBUG, "done validating file integrity.is file ok? " + result);
				if (result)
				{
					goToNextActivityAndFinish();
					return;
				}
				final String mergedFileFullPath = App.getMergedFileFullPath();
				if (mergedFileFullPath != null)
					new File(mergedFileFullPath).delete();
				_validationTask = null;
				if(_remoteService !=null){
					_remoteService.requestContinueDownload();
				}
				else{
					return;
				}
				super.onPostExecute(result);
			}
		};
		_validationTask.execute(new Object());
	}

	boolean showErrorDialogIfNeeded(final DownloadStatus downloadStatus)
	{
		// Logger.log(LogLevel.DEBUG, "showing error dialog:" + downloadStatus);
		if (_downloadStatus == downloadStatus)
			return false;
		if (!_isActivityVisible)
		{
			finish();
			return false;
		}
		_downloadStatus = downloadStatus;
		String title = null, message = null;
		switch (downloadStatus)
		{
			case ERROR_WITH_CONNECTION:
				title = getString(R.string.downloader_activity_connection_error_dialog_title);
				message = getString(R.string.downloader_activity_connection_error_dialog_desc);
				break;
			case ERROR_NOT_ENOUGH_SPACE:
				title = getString(R.string.downloader_activity_not_enough_space_error_dialog_title);
				message = getString(R.string.downloader_activity_not_enough_space_error_dialog_desc);
				break;
			case ERROR_EXTERNAL_STORAGE_UNAVAILABLE:
				title = getString(R.string.downloader_activity_external_storage_unavailable_error_dialog_title);
				message = getString(R.string.downloader_activity_external_storage_unavailable_error_dialog_desc);
				break;
			case ERROR_UNKNOWN:
			default:
				title = getString(R.string.downloader_activity_unknown_error_dialog_title);
				message = getString(R.string.downloader_activity_unknown_error_dialog_desc);
				break;
		}
		closeAlertDialogIfNeeded();
		final AlertDialog.Builder builder = new AlertDialog.Builder(DownloaderActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton(R.string.downloader_activity_error_option_retry, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton)
			{
				// user chose to retry download
				_downloadStatus = DownloadStatus.NO_ERROR;
				_remoteService.requestContinueDownload();
			}
		});
		builder.setNegativeButton(R.string.downloader_activity_error_option_exit, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton)
			{
				// user chose to exit app
				_downloadStatus = DownloadStatus.NO_ERROR;
				DownloaderActivity.this.finish();
				if (_remoteService != null)
					_remoteService.requestAbortDownload();
				cancelDownloadNotification(DownloaderActivity.this);
				dialog.dismiss();
				_currentAlertDialog = null;
			}
		});
		builder.setCancelable(false);
		builder.setTitle(title);
		builder.setMessage(message);
		_currentAlertDialog = builder.create();
		_currentAlertDialog.show();
		return true;
	}

	protected void closeAlertDialogIfNeeded()
	{
		if (_currentAlertDialog != null)
		{
			if (_currentAlertDialog.isShowing())
				_currentAlertDialog.dismiss();
			_currentAlertDialog = null;
		}
	}

	/**
	 * If the download isn't present, we initialize the download UI. This ties all of the controls into the remote service calls.
	 */
	private void initializeDownloadUI()
	{
		_downloaderClientStub = DownloaderClientMarshaller.CreateStub(this, DownloaderService.class);
		setContentView(R.layout.downloader_activity);
		_progressBar = (ProgressBar) findViewById(R.id.downloaderActivity_downloadingProgressBar);
		_progressTextView = (TextView) findViewById(R.id.downloaderActivity_downloadingProgressTextView);
		_progressTextView.setText("");
		_categoryProgressTextView = (TextView) findViewById(R.id.downloaderActivity_categoryProgressTextView);
		_categoryNameTextView = (TextView) findViewById(R.id.downloaderActivity_categoryNameTextView);
		_categoryIconImageView = (ImageView) findViewById(R.id.downloaderActivity_categoryIconImageView);
		//
		setIndeterminate(true);
	}

	protected void setIndeterminate(final boolean indeterminate)
	{
		final int enableVisibility = indeterminate ? View.GONE : View.VISIBLE;
		_categoryNameTextView.setVisibility(enableVisibility);
		_categoryProgressTextView.setVisibility(enableVisibility);
		_progressTextView.setVisibility(enableVisibility);
		_categoryIconImageView.setVisibility(enableVisibility);
		_progressBar.setIndeterminate(indeterminate);
	}

	protected void show3gWarningIfNeeded()
	{
		// if using 3g , show warning and only when the user has agreed , continue:
		final String title = getString(R.string.downloader_activity_wifi_check_dialog_title);
		final String message = getString(R.string.downloader_activity_wifi_check_dialog_desc);
		final AlertDialog.Builder builder = new AlertDialog.Builder(DownloaderActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_alert);
		builder.setPositiveButton(R.string.downloader_activity_wifi_check_dialog_ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton)
			{
				_remoteService.setDownloadFlags(IDownloaderService.FLAGS_DOWNLOAD_OVER_CELLULAR);
				_remoteService.requestContinueDownload();
			}
		});
		builder.setCancelable(false);
		builder.setTitle(title);
		builder.setMessage(message);
		closeAlertDialogIfNeeded();
		_currentAlertDialog = builder.create();
		_currentAlertDialog.show();
	}

	/**
	 * Connect the stub to our service on start.
	 */
	@Override
	protected void onStart()
	{
		if (null != _downloaderClientStub)
			_downloaderClientStub.connect(this);
		super.onStart();
	}

	/**
	 * Disconnect the stub from our service on stop
	 */
	@Override
	protected void onStop()
	{
		if (null != _downloaderClientStub)
			_downloaderClientStub.disconnect(this);
		super.onStop();
	}

	@Override
	protected void onPause()
	{
		// Logger.log(LogLevel.DEBUG, "");
		_isActivityVisible = false;
		if (null != _downloaderClientStub)
			_downloaderClientStub.disconnect(this);
		closeAlertDialogIfNeeded();
		super.onPause();
	}

	/**
	 * Connect the stub to our service on resume.
	 */
	@Override
	protected void onResume()
	{
		_isActivityVisible = true;
		closeAlertDialogIfNeeded();
		_downloadStatus = DownloadStatus.NO_ERROR;
		if (null != _downloaderClientStub)
			_downloaderClientStub.connect(this);
		super.onResume();
	}

	/**
	 * Critical implementation detail. In onServiceConnected we create the remote service and marshaler. This is how we pass the client information back to the service so the client can be properly notified of changes. We must do this every time we reconnect to the service.
	 */
	@Override
	public void onServiceConnected(final Messenger m)
	{
		_remoteService = DownloaderServiceMarshaller.CreateProxy(m);
		_remoteService.onClientUpdated(_downloaderClientStub.getMessenger());
	}

	/**
	 * deletes old expansion files (all those that are older then the one of the app version.<br/>
	 * TODO in case of more than one kind of expansion file , change this function , since it deletes all kinds and not just one.
	 */
	private void deleteOldMergedFile()
	{
		alredyDelete = true;
		// ResourceGetter.resetResourceGetter(this);
		int fileVersion = 0;
		final int versionCode = App.getAppVersionCode();
		fileVersion = versionCode;
		final String fileName = Helpers.getExpansionAPKFileName(this, true, fileVersion);
		final File fileForNewFile = new File(Helpers.generateSaveFileName(this, fileName));
		final File parentFile = fileForNewFile.getParentFile();
		if (!parentFile.exists())
			return;
		final File[] listFiles = parentFile.listFiles();
		for (final File file:listFiles)
		{
			final String name = file.getName();
			if (name.startsWith(fileName) || !name.startsWith("main"))
				continue;
			file.delete();
		}
	}

	/**
	 * The download state should trigger changes in the UI --- it may be useful to show the state as being indeterminate at times. This sample can be considered a guideline.
	 */
	@Override
	public void onDownloadStateChanged(final int newState)
	{
		boolean indeterminate;
		Logger.log(LogLevel.DEBUG, "newState:" + newState);
		switch (newState)
		{
			case IDownloaderClient.STATE_IDLE:
				// STATE_IDLE means the service is listening, so it's
				// safe to start making calls via mRemoteService.
				indeterminate = true;
				_downloadStatus = DownloadStatus.NO_ERROR;
				closeAlertDialogIfNeeded();
				break;
			case IDownloaderClient.STATE_FETCHING_URL:
			case IDownloaderClient.STATE_CONNECTING:
				indeterminate = true;
				closeAlertDialogIfNeeded();
				_downloadStatus = DownloadStatus.NO_ERROR;
				break;
			case IDownloaderClient.STATE_DOWNLOADING:
				indeterminate = false;
				closeAlertDialogIfNeeded();
				_downloadStatus = DownloadStatus.NO_ERROR;
				break;
			case IDownloaderClient.STATE_PAUSED_NETWORK_SETUP_FAILURE:
			case IDownloaderClient.STATE_PAUSED_NETWORK_UNAVAILABLE:
				indeterminate = true;
				showErrorDialogIfNeeded(DownloadStatus.ERROR_WITH_CONNECTION);
				break;
			case IDownloaderClient.STATE_FAILED_CANCELED:
			case IDownloaderClient.STATE_FAILED:
			case IDownloaderClient.STATE_FAILED_FETCHING_URL:
				indeterminate = false;
				showErrorDialogIfNeeded(DownloadStatus.ERROR_WITH_CONNECTION);
				break;
			case IDownloaderClient.STATE_FAILED_UNLICENSED:
				indeterminate = false;
				showErrorDialogIfNeeded(DownloadStatus.ERROR_UNKNOWN);
				break;
			case IDownloaderClient.STATE_PAUSED_NEED_CELLULAR_PERMISSION:
			case IDownloaderClient.STATE_PAUSED_WIFI_DISABLED_NEED_CELLULAR_PERMISSION:
				indeterminate = false;
				show3gWarningIfNeeded();
				break;
			case IDownloaderClient.STATE_PAUSED_BY_REQUEST:
				indeterminate = false;
				break;
			case IDownloaderClient.STATE_FAILED_SDCARD_FULL:
				indeterminate = false;
				showErrorDialogIfNeeded(DownloadStatus.ERROR_NOT_ENOUGH_SPACE);
				break;
			case IDownloaderClient.STATE_PAUSED_ROAMING:
			case IDownloaderClient.STATE_PAUSED_SDCARD_UNAVAILABLE:
				indeterminate = false;
				showErrorDialogIfNeeded(DownloadStatus.ERROR_EXTERNAL_STORAGE_UNAVAILABLE);
				break;
			case IDownloaderClient.STATE_COMPLETED:
				indeterminate = true;
				closeAlertDialogIfNeeded();
				_downloadStatus = DownloadStatus.NO_ERROR;
				verifyFileIntegrity();
				return;
			default:
				indeterminate = true;
				Logger.log(LogLevel.WARNING, "got unknown state from the service:" + newState);
		}
		setIndeterminate(indeterminate);
	}

	/**
	 * Sets the state of the various controls based on the progressinfo object sent from the downloader service.
	 */
	@Override
	public void onDownloadProgress(final DownloadProgressInfo progress)
	{
		setIndeterminate(false);
		_categoryProgressTextView.setVisibility(View.VISIBLE);
		_progressTextView.setVisibility(View.VISIBLE);
		_categoryIconImageView.setVisibility(View.VISIBLE);
		_categoryNameTextView.setVisibility(View.VISIBLE);
		_progressBar.setMax((int) (progress.mOverallTotal >> 8));
		_progressBar.setProgress((int) (progress.mOverallProgress >> 8));
		final long progressToShow = progress.mOverallProgress * 100 / progress.mOverallTotal;
		_progressTextView.setText(getString(R.string.downloader_activity_progress_format, progressToShow));
		final int categoryIndexToPublish = Math.max(0, Math.min(_numberOfCategories - 1, (int) progressToShow / _categoriesSteps));
		if (_currentCategoryIndex != categoryIndexToPublish)
		{
			final XmlTag currentCategoryInfo = _allCategoriesTags.get(categoryIndexToPublish);
			final String categoryNameStr = currentCategoryInfo.getTagAttributes().get("name");
			final int categoryNameResId = getResources().getIdentifier(categoryNameStr, "strings", getPackageName());
			final String categoryName = getResources().getString(categoryNameResId);// currentCategoryInfo.getTagAttributes().get("name");
			final String associatedIconImage = currentCategoryInfo.getTagAttributes().get("associatedIconImage");
			final int resID = getResources().getIdentifier(associatedIconImage, "drawable", getPackageName());
			_categoryIconImageView.setImageResource(resID);
			//
			final String categoryProgressTitle = getString(R.string.downloader_activity_category_number_title, categoryIndexToPublish + 1, _numberOfCategories);
			_categoryProgressTextView.setText(categoryProgressTitle);
			_categoryNameTextView.setText(categoryName);
			// Logger.log(LogLevel.DEBUG, "progress:" + progress + "% categoryIndexToPublish:" + categoryIndexToPublish + " categoryName:" + categoryName + " associatedIconImage:" + associatedIconImage);
		}
		_currentCategoryIndex = categoryIndexToPublish;
	}

	boolean isActivityVisible()
	{
		return _isActivityVisible;
	}

	protected void goToNextActivityAndFinish()
	{
		// Logger.log(LogLevel.DEBUG, "");
		if (isActivityVisible())
		{
			cancelDownloadNotification(DownloaderActivity.this);
			final Intent intent = App.global().getMainActivityIntent(this);
			// TODO:handle resuming of the app by other third party apps
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		}
		finish();
	}

	public static void cancelDownloadNotification(final Context context)
	{
		final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(DownloadNotification.NOTIFICATION_ID);
	}
}
