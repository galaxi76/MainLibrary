package com.soundtouch.main_library.activities.settings_activity;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.KeyEvent;
import android.widget.Toast;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;
import com.soundtouch.main_library.R;
import com.soundtouch.main_library.SoundTouch2PromotionDialog;
import com.soundtouch.main_library.VideoTouchPromotionalDesignedDialog;
import com.soundtouch.main_library.activities.BaseActivity;
import com.soundtouch.main_library.activities.main_activity.MainActivity;
import com.soundtouch.main_library.activities.main_activity.MainActivity.AutoPilotState;
import com.soundtouch.main_library.resource_getter.ResourcesManager;


public class SettingsActivity extends PreferenceActivity
{
	private static final String	NEW_HIDE_EVENET	= "newHideEvenet";
	public static final String			EXIT_APP_EXTRA			= "exit_app";
	protected static final String		CLASS_NAME				= SettingsActivity.class.getCanonicalName();
	protected HashMap<String, Language>	_localAndLanguageMap	= new HashMap<String, Language>();
	protected boolean					_fullVersion;
	protected int						lockScreenPeriod		= 0;
	protected static boolean			_isAmountChanged		= false;
	protected static final String		CHECKED_PERIOD_INDX		= "checkedPeriodIndx";

	public enum CloseItemBy
	{
		TOUCH, BUTTON
	}

	protected void setOrientation()
	{

		if (SettingsActivity.isScreenOrientationChecked())
		{
			if (!App.isReversePortraitSupported())
			{
				return;
			}
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);

		}
		else
		{
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}

	}


	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setOrientation();

		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.settings_activity_layout);

		addPreferencesFromResource(R.xml.settings_activity);

		setItemsAmountList();

		_fullVersion = getApplicationContext().getResources().getBoolean(R.bool.settings_activity_picture_label_enabled);

		// Checkout video touch button set
		final Preference checkOutVideoToucHPref = (Preference) findPreference(getString(R.string.settings_activity_check_button_pref));
		final Context context = this;
		checkOutVideoToucHPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				showVideoTouchPromotionDialog(context);
				return true;
			}
		});

		// Checkout video touch button set
		final Preference checkSoundTouchPref = (Preference) findPreference(getString(R.string.settings_activity_check_soundtouch_button_pref));
		checkSoundTouchPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				showSoundTouchPromotionDialog(context);
				return true;
			}
		});

		// / Set exit button
		final Preference exitApp = (Preference) findPreference(getString(R.string.settings_activity_exit_button_pref));
		exitApp.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{

			@Override
			public boolean onPreferenceClick(Preference preference)
			{
				exitApplication();
				return true;
			}
		});

		// Set speech box
		final String speechCheckBoxPrefKey = getString(R.string.settings_activity_speech_checkbox_pref);
		final CheckBoxPreference speechCheckBoxPref = (CheckBoxPreference) findPreference(speechCheckBoxPrefKey);
		speechCheckBoxPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(final Preference paramPreference, final Object paramObject)
			{
				Boolean valueToSet = (Boolean) paramObject;
				if (!valueToSet)
				{
					// If unchecked, uncheck 'speech only' as well
					toggleCheckBox(false, (CheckBoxPreference) findPreference(getString(R.string.settings_activity_speech_only_checkbox_pref)));
				}
				return toggleCheckBox((Boolean) paramObject, speechCheckBoxPref);
			}
		});
		//
		// label checkbox
		//

		final String labelCheckBoxPrefKey = getString(R.string.settings_activity_label_checkbox_pref);
		final CheckBoxPreference labelCheckBoxPref = (CheckBoxPreference) findPreference(labelCheckBoxPrefKey);
		labelCheckBoxPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				return getOnLabelCheckboxOnPrefChangeAction(labelCheckBoxPref, labelCheckBoxPrefKey, newValue);
			}
		});

		//
		// Auto-Pilot List Preference
		final String autoPilotListPrefKey = getString(R.string.settings_activity_auto_pilot_list_pref);
		final ListPreference autoPilotListPref = (ListPreference) findPreference(autoPilotListPrefKey);
		// autoPilotListPref.setSummary(autoPilotListPref.getEntries()[getAutoPilotCategories().ordinal()]);
		autoPilotListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				return onAutoPilotPreferenceChange(newValue, autoPilotListPref);
			}
		});

		// SPeech only checkbox
		final String speechOnlyCheckBoxPrefKey = getString(R.string.settings_activity_speech_only_checkbox_pref);
		final CheckBoxPreference speechOnlyCheckBox = (CheckBoxPreference) findPreference(speechOnlyCheckBoxPrefKey);
		speechOnlyCheckBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Boolean valueToSet = (Boolean) newValue;
				if (valueToSet)
				{
					toggleCheckBox(true, speechCheckBoxPref);
				}

				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(speechOnlyCheckBoxPrefKey, valueToSet).commit();
				return true;
			}
		});

		//

		//
		// lockscreen checkbox
		//
		final String lockScreenPrefKey = getString(R.string.settings_activity_lock_screen_checkbox_pref);
		final ListPreference lockScreenList = (ListPreference) findPreference(lockScreenPrefKey);
		lockScreenList.setSummary(getLockScreenDuration() + " " + getResources().getString(R.string.settings_activity_checkbox_lock_screen_summary));
		lockScreenList.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				final String valueToSet = (String) newValue;
				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putString(lockScreenPrefKey, valueToSet).commit();
				lockScreenList.setSummary(getLockScreenDuration() + "");

				return true;
			}
		});

		final String hideItemsPrefKey = getString(R.string.settings_activity_hide_items_checkbox_pref);
		final CheckBoxPreference hideItemsCheckBox = (CheckBoxPreference) findPreference(hideItemsPrefKey);
		hideItemsCheckBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				final Boolean valueToSet = (Boolean) newValue;
				context.getSharedPreferences(CLASS_NAME, 0).edit().putBoolean(NEW_HIDE_EVENET, true).commit();

				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(hideItemsPrefKey, valueToSet).commit();
				return true;
			}
		});

		// Orientation checkbox
		final String orientationPrefKey = getString(R.string.settings_activity_oreintation_checkbox_pref);
		final CheckBoxPreference screenOrientationCheckBox = (CheckBoxPreference) findPreference(orientationPrefKey);
		screenOrientationCheckBox.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				final Boolean valueToSet = (Boolean) newValue;
				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(orientationPrefKey, valueToSet).commit();
				if (!App.isReversePortraitSupported())
				{
					Toast.makeText(context, getResources().getString(R.string.settings_activity_not_supported), Toast.LENGTH_LONG).show();
				}
				return true;
			}
		});

		//
		// language list
		//
		final String languageListPrefKey = getString(R.string.settings_activity_language_list_pref);
		final ListPreference languageListPref = (ListPreference) findPreference(languageListPrefKey);
		// fill the language list with its values and entries :
		final String[] valuesAndEntries = getResources().getStringArray(R.array.settings_activity_language_list_values_and_entries);
		// final String[] valuesAndEntriesEnglishNames =
		// getResources().getStringArray(R.array.settings_activity_language_list_values_and_english_names);
		final ArrayList<String> entries = new ArrayList<String>(), values = new ArrayList<String>();
		for (final String entryAndValue:valuesAndEntries)
		{
			final String[] splitedValues = entryAndValue.split(":");
			final Language lang = new Language(splitedValues[1], splitedValues.length > 2 ? splitedValues[2] : null, splitedValues.length > 2 ? splitedValues[3] : null);
			// final int indexOfSeperator = entryAndValue.indexOf(':');
			final String locale = splitedValues[0]; // =
													// entryAndValue.substring(0,
													// indexOfSeperator);
			values.add(locale);
			// final String language = entryAndValue.substring(indexOfSeperator
			// + 1);
			entries.add(lang.nativeName);
			_localAndLanguageMap.put(locale, lang);
		}
		languageListPref.setEntries(entries.toArray(new String[entries.size()]));
		languageListPref.setEntryValues(values.toArray(new String[values.size()]));
		// set current entry to be shown
		languageListPref.setSummary(languageListPref.getEntry());
		languageListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(final Preference preference, final Object newValue)
			{
				final String val = newValue.toString();
				final int index = languageListPref.findIndexOfValue(val);
				languageListPref.setSummary(languageListPref.getEntries()[index]);
				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putString(languageListPrefKey, val).commit();
				if (isLabelCheckboxNeedToBeUnTicked(isLabelEnabled(), true))
				{
					labelCheckBoxPref.setChecked(false);
					getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(labelCheckBoxPrefKey, false).commit();
				}
				return true;
			}
		});

		// Items amount list
		//
		//

		//
		// closeItemByKey
		//
		final String closeItemByKey = getString(R.string.settings_activity_close_item_by_pref);
		final ListPreference radioGroupPref = (ListPreference) findPreference(closeItemByKey);
		radioGroupPref.setSummary(radioGroupPref.getEntries()[getCloseItemBy().ordinal()]);
		radioGroupPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(final Preference preference, final Object newValue)
			{
				final Integer valueToSet = Integer.parseInt(newValue.toString());
				radioGroupPref.setSummary(radioGroupPref.getEntries()[valueToSet]);
				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putInt(closeItemByKey, valueToSet).commit();
				return true;
			}
		});
		// Logger.log(LogLevel.DEBUG, "isSpeechEnabled?" + isSpeechEnabled(this)
		// + " getSpeechLocale:" + getSpeechLocale(this) + " getCloseItemBy:" +
		// getCloseItemBy(this));
		isLabelCheckboxNeedToBeUnTicked(isLabelEnabled(), false);
	}

	protected void openMarketLink(String link)
	{
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		try
		{
			startActivity(goToMarket);
		}
		catch (final ActivityNotFoundException e)
		{
			// Logger.log(LogLevel.DEBUG,
			// "market wasn't found, so starting web browser instead...");
			goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			try
			{
				startActivity(goToMarket);
			}
			catch (final ActivityNotFoundException e2)
			{
				Logger.log(LogLevel.WARNING, "can't open simple url , so ignoring request");
			}
		}
	}

	/**
	 * checks if the language for the label feature is supported , and if not (optionally) shows a dialog.
	 * 
	 * @param isLabelCheckboxChecked
	 *            should be ture only if the picture-label checkbox needs to be ticked
	 * @return true iff checkbox should be changed to unticked
	 */
	@SuppressWarnings("deprecation")
	private boolean isLabelCheckboxNeedToBeUnTicked(final boolean isLabelCheckboxChecked, final boolean alsoShowUnavailableDialogIfNeeded)
	{
		if (!isLabelCheckboxChecked)
			return true;
		final String labelCheckBoxPrefKey = getString(R.string.settings_activity_label_checkbox_pref);
		final CheckBoxPreference labelCheckBoxPref = (CheckBoxPreference) findPreference(labelCheckBoxPrefKey);
		if (!labelCheckBoxPref.isEnabled())
			return true;
		final boolean labelIsSupported = ResourcesManager.isLabelSupportedInCurrentLanguage();
		// if the language for the label is not supported , untick the checkbox
		// and optionally show a dialog
		if (!labelIsSupported)
			if (alsoShowUnavailableDialogIfNeeded)
			{
				final String speechLocale = getSpeechLocale();
				String language;
				try
				{
					language = _localAndLanguageMap.get(speechLocale).englishName;
				}
				catch (final Exception e)
				{
					try
					{
						language = _localAndLanguageMap.get(speechLocale.replace("_", "-")).englishName;
					}
					catch (final Exception ex)
					{
						language = "";
					}
				}
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.settings_activity_unavailable_picture_label_language_dialog_title);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				final String descriptionFormat = getString(R.string.settings_activity_unavailable_picture_label_language_dialog_desc);
				final String description = String.format(descriptionFormat, language);
				builder.setMessage(description);
				builder.show();
			}
		return !labelIsSupported;
	}

	public static boolean isAmountOfItemsChanged()
	{
		if (_isAmountChanged == true)
		{
			_isAmountChanged = false;
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			final String testToShow = getString(R.string.main_activity_initializing_progress_dialog_title);
			final ProgressDialog progressDialog = ProgressDialog.show(this, null, testToShow);
			new AsyncTask<Void, Void, Void>()
			{
				@Override
				protected Void doInBackground(final Void ... paramArrayOfParams)
				{
					App.global().getResourcesManager().refreshLabelsSettings(SettingsActivity.this);
					return null;
				}

				@Override
				protected void onPostExecute(final Void result)
				{
					progressDialog.dismiss();
					SettingsActivity.super.onKeyUp(keyCode, event);

				};
			}.execute();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		BaseActivity.checkValidityOfMergedFile(this, null);
	}

	/** Set amount of items preference **/
	public void setItemsAmountList()
	{
		final String itemsAmountListPrefKey = getString(R.string.settings_activity_items_amount_list_pref);
		final ListPreference itemsAmountListPref = (ListPreference) findPreference(itemsAmountListPrefKey);
		itemsAmountListPref.setSummary(getAmountOfItems() + "");
		// fill the items amount list with its values and entries :
		itemsAmountListPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				_isAmountChanged = true;
				int valueToSet = Integer.parseInt(newValue.toString());

				getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putInt(itemsAmountListPrefKey, valueToSet).commit();
				itemsAmountListPref.setSummary(getAmountOfItems() + "");
				return true;
			}
		});
	}

	// /////////////////
	// static methods //
	// /////////////////
	/** returns true iff the speech is enabled */
	public static boolean isSpeechEnabled()
	{
		final Context context = App.global();
		final String speechCheckBoxPrefKey = context.getString(R.string.settings_activity_speech_checkbox_pref);
		final boolean defaultValue = context.getResources().getBoolean(R.bool.settings_activity_speech_checkbox_default_value);
		final boolean result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getBoolean(speechCheckBoxPrefKey, defaultValue);
		return result;
	}

	protected boolean toggleCheckBox(boolean value, CheckBoxPreference checkBox)
	{
		getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(checkBox.getKey(), value).commit();
		checkBox.setChecked(value);
		return true;
	}

	/** returns true iff the speech is enabled */
	public static boolean isLabelEnabled()
	{
		final Context context = App.global();
		final String labelCheckBoxPrefKey = context.getString(R.string.settings_activity_label_checkbox_pref);
		final boolean defaultValue = context.getResources().getBoolean(R.bool.settings_activity_label_checkbox_default_value);
		final boolean result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getBoolean(labelCheckBoxPrefKey, defaultValue);
		return result;
	}

	public static boolean isSpeechOnlyEnabled()
	{
		final Context context = App.global();
		final String speechOnlyCheckBoxPrefKey = context.getString(R.string.settings_activity_speech_only_checkbox_pref);
		final boolean defaultValue = context.getResources().getBoolean(R.bool.settings_activity_speech_only_checkbox_default_value);
		final boolean result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getBoolean(speechOnlyCheckBoxPrefKey, defaultValue);
		return result;
	}

	/** Returns the period of time the screen should be locked. **/
	public static int getLockScreenDuration()
	{
		final Context context = App.global();
		final String lockScreenListPrefKey = context.getString(R.string.settings_activity_lock_screen_checkbox_pref);
		final String defaultValue = context.getResources().getString(R.string.settings_activity_lock_screen_list_default_value);
		final String result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getString(lockScreenListPrefKey, defaultValue);

		return Integer.parseInt(result);
	}

	/** returns true if the screen orientation should be changed **/
	public static boolean isScreenOrientationChecked()
	{
		final Context context = App.global();
		final String screenOrientationPrefKey = context.getString(R.string.settings_activity_oreintation_checkbox_pref);
		final boolean defaultValue = context.getResources().getBoolean(R.bool.settings_activity_orientation_checkbox_default_value);
		final boolean result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getBoolean(screenOrientationPrefKey, defaultValue);
		return result;
	}

	public static String getSpeechLocale()
	{
		final Context context = App.global();
		final String languageListPrefKey = context.getString(R.string.settings_activity_language_list_pref);
		final String defaultValue = context.getString(R.string.settings_activity_language_list_default_value);
		final String result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getString(languageListPrefKey, defaultValue).replace('-', '_');
		return result;
	}

	public static CloseItemBy getCloseItemBy()
	{
		final Context context = App.global();
		final String closeItemByKey = context.getString(R.string.settings_activity_close_item_by_pref);
		final int defaultValue = 0;
		final int result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getInt(closeItemByKey, defaultValue);
		if (result == 1)
			return CloseItemBy.BUTTON;
		return CloseItemBy.TOUCH;
	}

	/** if -1 is returned, then its not selected **/
	public static MainActivity.AutoPilotState getAutoPilotCategories()
	{
		final Context context = App.global();
		final String autoPilotPrefKey = context.getString(R.string.settings_activity_auto_pilot_list_pref);
		final MainActivity.AutoPilotState defaultValue = AutoPilotState.NO_PILOT;
		int result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getInt(autoPilotPrefKey, defaultValue.ordinal());
		AutoPilotState autoPilotState = AutoPilotState.values()[result];
		return autoPilotState;
	}

	protected String getShareLink(){
		if(App.global().isUsingAmazon()){
			return "http://www.amazon.com/gp/mas/dl/android?p="+getPackageName();
		}
		return "https://play.google.com/store/apps/details?id=" + getPackageName();
	}
	public static int getAmountOfItems()
	{
		final Context context = App.global();
		final String itemsAmountKey = context.getString(R.string.settings_activity_items_amount_list_pref);
		final int defaultValue = Integer.parseInt(context.getString(R.string.settings_activity_items_amount_list_default_value));
		final int result = context.getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).getInt(itemsAmountKey, defaultValue);
		return result;
	}

	public void exitApplication()
	{
		Intent intent = App.global().getMainActivityIntent(this);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(EXIT_APP_EXTRA, true);
		finish();
		startActivity(intent);
	}

	protected void showVideoTouchPromotionDialog(Context context)
	{
		VideoTouchPromotionalDesignedDialog vpd = new VideoTouchPromotionalDesignedDialog(context, new OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		vpd.show();
	}

	protected void showSoundTouchPromotionDialog(Context context)
	{
		SoundTouch2PromotionDialog st2 = new SoundTouch2PromotionDialog(context,new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		st2.show();
	}

	protected boolean onAutoPilotPreferenceChange(Object newValue, ListPreference autoPilotListPref)
	{
		final String valueToSet = (String) newValue;
		int intToSet = Integer.parseInt(valueToSet);
		autoPilotListPref.setSummary(autoPilotListPref.getEntries()[intToSet]);
		getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putInt(autoPilotListPref.getKey(), intToSet).commit();
		return true;
	}

	public class Language
	{
		public String	nativeName;
		public String	englishName;
		public String	fullVersionOnly;

		public Language(final String nativeName, final String englishName, final String fullVersionOnly)
		{
			this.nativeName = nativeName;
			this.englishName = englishName;
			this.fullVersionOnly = fullVersionOnly;
		}
	}

	/** This can be overriden by subClasses in order to set how to treat this checkbox **/
	protected boolean getOnLabelCheckboxOnPrefChangeAction(final CheckBoxPreference labelCheckBoxPref, final String labelCheckBoxPrefKey, Object paramObject)
	{

		final Boolean valueToSet = (Boolean) paramObject;
		if (!valueToSet)
		{
			getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(labelCheckBoxPrefKey, false).commit();
			return true;
		}
		// check if we should allow ticking the checkbox:
		final boolean shouldBeUnticked = isLabelCheckboxNeedToBeUnTicked(valueToSet, true);
		getSharedPreferences(CLASS_NAME, Context.MODE_PRIVATE).edit().putBoolean(labelCheckBoxPrefKey, !shouldBeUnticked).commit();
		// if the checkbox is going to be unchecked anyway , we
		// approve changing it
		return !shouldBeUnticked;

	}

	/** This method deals with only one item to hide,as requested. **/
	public static boolean isHideItem()
	{
		final Context context = App.global();
		return context.getSharedPreferences(CLASS_NAME, 0).getBoolean(context.getString(R.string.settings_activity_hide_items_checkbox_pref), context.getResources().getBoolean((R.bool.settings_activity_hide_items_default_value)));
	}
	
	public static boolean isNewHideEvent(){
		final Context context = App.global();
		boolean result = context.getSharedPreferences(CLASS_NAME, 0).getBoolean(NEW_HIDE_EVENET, false);
		Logger.log(LogLevel.DEBUG,"has new hide event occur" + result); 
		context.getSharedPreferences(CLASS_NAME, 0).edit().putBoolean(NEW_HIDE_EVENET, false).commit();
		return result;
	}
}
