package com.soundtouch.main_library;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Bundle;
import android.os.Handler;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;

/** A class that manages internet connections */
public class ConnectionManager
  {
  public interface IResponseListener
    {
    void onReceivedResponse(String responseString);
    }

  public static enum PostMethod
    {
    GET,POST
    }

  /** Sends the bundle of the message to be sent to the server. */
  public static void postMessage(final String url,final Bundle message,final PostMethod postMethod,final IResponseListener responseListener)
    {
    final Handler handler=new Handler();
    final Thread thread=new Thread(new Runnable()
      {
        @Override
        public void run()
          {
          final String responseString=ConnectionManager.getStrResponse(message,url,postMethod);
          Logger.log(LogLevel.DEBUG,"got response from ad server : " + responseString);
          if(responseListener!=null)
            handler.post(new Runnable()
              {
                @Override
                public void run()
                  {
                  responseListener.onReceivedResponse(responseString);//responseString);	//responseString // TODO : this should be changed when testing ads
                  }
              });
          }
      });
    thread.start();
    }

  /**
   * sends a bundle of parameters to the given url, in the specified method (POST/GET)<br/>
   * returns null in case of error
   */
  private static String getStrResponse(final Bundle parameters,String url,final PostMethod method)
    {
    String strResponse=null;
    HttpURLConnection conn=null;
    if(method==PostMethod.GET)
      url=url+"?"+encodeUrl(parameters);
    try
      {
      conn=(HttpURLConnection)new URL(url).openConnection();
      if(method==PostMethod.POST)
        {
        // use method override
        parameters.putString("method","POST");
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(encodeUrl(parameters).getBytes("UTF-8"));
        }
      strResponse=read(conn.getInputStream());
      }
    catch(final Exception e)
      {}
    return strResponse;
    }

  /** encodes the bundle of parameters to a url */
  private static String encodeUrl(final Bundle parameters)
    {
    if(parameters==null||parameters.size()==0)
      return "";
    final StringBuilder sb=new StringBuilder();
    boolean first=true;
    for(final String key : parameters.keySet())
      {
      if(first)
        first=false;
      else sb.append('&');
      sb.append(key+"="+parameters.getString(key));
      }
    return sb.toString();
    }

  /** reads an input stream into a string. used to read the response from the server */
  private static String read(final InputStream in) throws IOException
    {
    final StringBuilder sb=new StringBuilder();
    final BufferedReader r=new BufferedReader(new InputStreamReader(in),1000);
    for(String line=r.readLine();line!=null;line=r.readLine())
      sb.append(line);
    in.close();
    return sb.toString();
    }
  }
