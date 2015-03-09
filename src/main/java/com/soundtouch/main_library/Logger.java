package com.soundtouch.main_library;

import android.annotation.TargetApi;
import android.util.Log;


public class Logger
{
  private static final boolean DISPLAY_LOGS                                         = true;
  /** the format of the message to log , when the first string is the method name , and the second is the input message itself */
  private static final String  LOG_MESSAGE_FORMAT_WHEN_APPLICATION_TAG_DOESNT_EXIST = "%s:%s";
  /** the format of the message to log when the logger has an application tag, when the first string is the class name , the second is the method name , and the third is the message itself */
  private static final String  LOG_MESSAGE_FORMAT_WHEN_APPLICATION_TAG_EXISTS       = "%s.%s:%s";
  private static String        _applicationTag                                      = null;

  public enum LogLevel
  {
    VERBOSE, DEBUG, INFO, WARNING, ERROR, WTF
  }

  /**
   * sets an application tag to be used , so that all log messages will be inside this tag.<br />
   * if the application tag is null (or if never called to this function) , each log message will have a tag that matches the class of the function that called the log function, <br />
   * otherwise, all log messages will be inside the application tag
   */
  public static void setApplicationTag(final String applicationTag)
  {
    _applicationTag = applicationTag;
  }

  @TargetApi(8)
  public static void log(final LogLevel logLevel, final String message)
  {
    if (!DISPLAY_LOGS)
      return;
    final StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
    final String fullClassName = stackTraceElement.getClassName();
    final String simpleClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
    final String fullMessage, logTag;
    final String currentMethodName = stackTraceElement.getMethodName();
    if (_applicationTag == null)
    {
      logTag = simpleClassName;
      fullMessage = String.format(LOG_MESSAGE_FORMAT_WHEN_APPLICATION_TAG_DOESNT_EXIST, currentMethodName, message);
    }
    else
    {
      logTag = _applicationTag;
      fullMessage = String.format(LOG_MESSAGE_FORMAT_WHEN_APPLICATION_TAG_EXISTS, simpleClassName, currentMethodName, message);
    }
    switch (logLevel)
    {
      case DEBUG:
        Log.d(logTag, fullMessage);
        break;
      case ERROR:
        Log.e(logTag, fullMessage);
        break;
      case INFO:
        Log.i(logTag, fullMessage);
        break;
      case VERBOSE:
        Log.v(logTag, fullMessage);
        break;
      case WARNING:
        Log.w(logTag, fullMessage);
        break;
      case WTF:
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO)
          Log.wtf(logTag, fullMessage);
        else
          Log.e(logTag, "WTF:" + fullMessage);
        break;
    }
  }
}
