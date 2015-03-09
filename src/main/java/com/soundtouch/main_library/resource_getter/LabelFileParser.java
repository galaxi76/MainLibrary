package com.soundtouch.main_library.resource_getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import com.soundtouch.main_library.App;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;


/** a class that is intended to parse the labels file */
public class LabelFileParser
{
  public static void parsePictureLabels(final int categoryNameResId, final HashMap<String, String> pictureLabelToFill)
  {
    if (pictureLabelToFill == null || categoryNameResId == 0)
    {
      Logger.log(LogLevel.WARNING, "couldn't put picture labels");
      return;
    }
    final InputStream inputStream = App.global().getResources().openRawResource(categoryNameResId);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    try
    {
      while ((line = reader.readLine()) != null)// process the line, stuff to List
      {
        line = line.trim();
        if (line.length() == 0)
          continue;
        final String[] words = line.split("\"");
        String key = null, value = null;
        for (int i = 0; i < words.length; ++i)
        {
          final String word = words[i];
          if (word.length() == 0)
            continue;
          final char firstChar = word.charAt(0);
          if (Character.isLetter((int) firstChar))
            if (key == null)
              key = word;
            else
            {
              value = word;
              break;
            }
        }
        if (key != null && value != null)
          pictureLabelToFill.put(key.toLowerCase(), value);
      }
    }
    catch (final IOException e)
    {
      Logger.log(LogLevel.WARNING, "couldn't parse picture label file:" + e);
    }
    finally
    {
      try
      {
        reader.close();
      }
      catch (final IOException e)
      {
        Logger.log(LogLevel.WARNING, "couldn't close picture label file:" + e);
      }
    }
  }
}
