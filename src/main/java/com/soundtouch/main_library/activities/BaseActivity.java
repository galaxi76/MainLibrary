package com.soundtouch.main_library.activities;

import java.io.File;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.InversableDialog;
import com.soundtouch.main_library.R;
import com.soundtouch.main_library.activities.downloader_activity.DownloaderActivity;
import com.soundtouch.main_library.activities.settings_activity.SettingsActivity;


public class BaseActivity extends Activity
{
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	/**
	 * This is used to rotate the screen 180 degrees , with keeping the soft keys of the device(if available) in their place. Devices with API lower than 11 will have the whole screen inversed(including the softkeys ) as a result of the lack of support for setScale() method.
	 * 
	 * @param v
	 */
	public void setOrientation(ViewGroup v)
	{

		if (SettingsActivity.isScreenOrientationChecked())
		{

			if (!App.isScaleSupported())
			{
				if (!App.isReversePortraitSupported())
				{
					return;
				}
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
				return;
			}
			v.setScaleX(-1);
			v.setScaleY(-1);

		}
		else
		{

			if (!App.isScaleSupported())
			{

				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				return;
			}

			v.setScaleX(1);
			v.setScaleY(1);
		}

	}



	@Override
	public void onAttachedToWindow()
	{
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// call the super.onAttachedToWindow (doesn't work on some versions , so
		// used reflection) :
		final Class<?> activityClass = getClass().getSuperclass();
		try
		{
			final Method superOnAttachedToWindow = activityClass.getMethod("onAttachedToWindow");
			superOnAttachedToWindow.invoke(this);
		}
		catch (final Exception ex)
		{
		}
	}

	// TODO : put in comments when testing
	public static void goToDownloaderActivity(final Context context)
	{
//		final Intent intent = new Intent(context, DownloaderActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//		context.startActivity(intent);
//		if (context instanceof Activity)
//			((Activity) context).finish();
	}

	/**
	 * validates the merged file . if file seems ok , it runs runOnValidationSucceeded , otherwise , it shows a dialog which allows to either retry or close the given <br />
	 * activity and go to the downloader activity.
	 */
	public static void checkValidityOfMergedFile(final Activity context, final Runnable runOnValidationSucceeded)
	{
		final boolean mergedFileAvailable = App.global().getResourcesManager().isMergedFileAvailable(context);
		if (!mergedFileAvailable)
		{

			final InversableDialog dialog = new InversableDialog(context);
			dialog.setTitle(context.getResources().getString(R.string.base_activity_problem_with_content_file_dialog_title));
			dialog.setText(context.getResources().getString(R.string.base_activity_problem_with_content_file_dialog_desc));
			dialog.setPositiveButton(context.getResources().getString(R.string.base_activity_problem_with_content_file_option_retry), new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					dialog.dismiss();
					checkValidityOfMergedFile(context, runOnValidationSucceeded);
				}
			});

			dialog.setNegativeButton(context.getResources().getString(R.string.base_activity_problem_with_content_file_option_restart), new View.OnClickListener()
			{

				@Override
				public void onClick(View v)
				{
					// Logger.log(LogLevel.DEBUG,
					// "forgetting about the file location and going back to the downloader activity");
					// FileDownloadService.resetDownloadStatus(context);
					final String mergedFileFullPath = App.getMergedFileFullPath();
					if (mergedFileFullPath != null)
						new File(mergedFileFullPath).delete();
					dialog.dismiss();
					goToDownloaderActivity(context);
				}
			});
			dialog.setCancelable(false);
			dialog.show();

			return;
		}
		// Logger.log(LogLevel.DEBUG, "file is found , so reading from it ");
		if (runOnValidationSucceeded != null)
			runOnValidationSucceeded.run();
		return;
	}
	
	protected boolean deviceHasPermanentMenuKey(){
		int api = Build.VERSION.SDK_INT;
		if(api <= 10){
			return true;
		}
		if(api >= 14){
			return ViewConfiguration.get(this).hasPermanentMenuKey();
		}
		else{
			return false;
		}
	}
}
