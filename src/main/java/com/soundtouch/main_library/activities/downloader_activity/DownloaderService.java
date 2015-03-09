package com.soundtouch.main_library.activities.downloader_activity;
import com.soundtouch.main_library.R;

/**
 * This class demonstrates the minimal client implementation of the DownloaderService from the Downloader library. Since services must be uniquely registered across all of Android it's a good idea for services to reside directly within your Android application package.
 */
public class DownloaderService extends com.google.android.vending.expansion.downloader.impl.DownloaderService
  {
  // used by the preference obfuscater
  private static final byte[] SALT =new byte[] {1,43,-12,-1,54,98,-100,-12,43,2,-8,-4,9,5,-106,-108,-33,45,-1,84};

  /**
   * This public key comes from your Android Market publisher account, and it used by the LVL to validate responses from Market on your behalf.
   */
  @Override
  public String getPublicKey()
    {
    final String licenseKey=getResources().getString(R.string.app_license_key).trim();
    return licenseKey;
    }

  /**
   * This is used by the preference obfuscater to make sure that your obfuscated preferences are different than the ones used by other applications.
   */
  @Override
  public byte[] getSALT()
    {
    return SALT;
    }

  /**
   * Fill this in with the class name for your alarm receiver. We do this because receivers must be unique across all of Android (it's a good idea to make sure that your receiver is in your unique package)
   */
  @Override
  public String getAlarmReceiverClassName()
    {
    return DownloaderAlarmReceiver.class.getName();
    }
  }
