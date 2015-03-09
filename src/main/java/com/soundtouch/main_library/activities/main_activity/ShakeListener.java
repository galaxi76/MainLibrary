package com.soundtouch.main_library.activities.main_activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import com.soundtouch.main_library.Logger;
import com.soundtouch.main_library.Logger.LogLevel;

/** a class to handle the shake events. used by the FullScreenImageActivity */
public class ShakeListener implements SensorEventListener
  {
  private static final int FORCE_THRESHOLD           =500;
  private static final int TIME_THRESHOLD            =100;
  private static final int SHAKE_TIMEOUT             =500;
  private static final int SHAKE_DURATION            =1000;
  private static final int SHAKE_COUNT               =5;
  private SensorManager    _sensorMgr;
  private float            _lastX                    =-1.0f,_lastY=-1.0f,_lastZ=-1.0f;
  private OnShakeListener  _shakeListener;
  private final Context    _context;
  private int              _shakeCount               =0;
  private long             _lastShake,_lastForce,_lastTime;
  private boolean          _isListeningToShakeEvents =false;

  public interface OnShakeListener
    {
    public void onShake();
    }

  public ShakeListener(final Context context)
    {
    _context=context;
    resume();
    }

  public void setOnShakeListener(final OnShakeListener listener)
    {
    _shakeListener=listener;
    }

  public void resume()
    {
    _sensorMgr=(SensorManager)_context.getSystemService(Context.SENSOR_SERVICE);
    boolean supported=false;
    try
      {
      if(_sensorMgr==null)
        throw new UnsupportedOperationException("Sensors not supported");
      supported=_sensorMgr.registerListener(this,_sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_GAME);
      _isListeningToShakeEvents=true;
      }
    catch(final Exception e)
      {
      Logger.log(LogLevel.WARNING,"Shaking not supported");
      _isListeningToShakeEvents=false;
      }
    if(!supported&&_sensorMgr!=null)
      _sensorMgr.unregisterListener(this);
    }

  public void pause()
    {
    if(_sensorMgr!=null)
      {
      _sensorMgr.unregisterListener(this);
      _sensorMgr=null;
      }
    _isListeningToShakeEvents=false;
    }

  @Override
  public void onAccuracyChanged(final Sensor sensor,final int accuracy)
    {}

  @Override
  public void onSensorChanged(final SensorEvent event)
    {
    if(!_isListeningToShakeEvents||event.sensor.getType()!=Sensor.TYPE_ACCELEROMETER)
      return;
    final long now=System.currentTimeMillis();
    if(now-_lastForce>SHAKE_TIMEOUT)
      _shakeCount=0;
    if(now-_lastTime>TIME_THRESHOLD)
      {
      final long diff=now-_lastTime;
      final float speed=Math.abs(event.values[SensorManager.DATA_X]+event.values[SensorManager.DATA_Y]+event.values[SensorManager.DATA_Z]-_lastX-_lastY-_lastZ)/diff*10000;
      if(speed>FORCE_THRESHOLD)
        {
        if(++_shakeCount>=SHAKE_COUNT&&now-_lastShake>SHAKE_DURATION)
          {
          _lastShake=now;
          _shakeCount=0;
          if(_shakeListener!=null)
            _shakeListener.onShake();
          }
        _lastForce=now;
        }
      _lastTime=now;
      _lastX=event.values[SensorManager.DATA_X];
      _lastY=event.values[SensorManager.DATA_Y];
      _lastZ=event.values[SensorManager.DATA_Z];
      }
    }
  }
