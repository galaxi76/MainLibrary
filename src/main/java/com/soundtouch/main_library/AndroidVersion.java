package com.soundtouch.main_library;

import android.os.Build;


/** a class for checking the current android version of the device , in order to verify that it's enough for some high api levels' functions to run */
public enum AndroidVersion
{
  /** v1.5, api3 */
  CUPCAKE(3),
  /** v1.6, api4 */
  DONUT(4),
  /** v2.0..v2.1 , api5..7 */
  ECLAIR(5),
  /** v2.2, api8 */
  FROYO(8),
  /** v2.3, api9 */
  GINGERBREAD(9),
  /** v3.0..3.2, api10..12 */
  HONEYCOMB(11),
  /** v4.0 api 14,15 */
  ICE_CREAM_SANDWICH(14);
  private final int apiLevel;

  /** CTOR */
  private AndroidVersion(final int apiLevel)
  {
    this.apiLevel = apiLevel;
  }

  /** returns the api level of the version */
  public int getApiLevel()
  {
    return apiLevel;
  }

  /** returns true iff the installed OS of the device is of a version that is at least as the specified version */
  public static boolean hasAtLeastAndroidVersion(final AndroidVersion version)
  {
    final int currentVersionNumber = Build.VERSION.SDK_INT;
    return currentVersionNumber >= version.getApiLevel();
  }
}
