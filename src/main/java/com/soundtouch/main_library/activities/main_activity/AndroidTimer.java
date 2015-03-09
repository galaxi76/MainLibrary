package com.soundtouch.main_library.activities.main_activity;

import android.os.Handler;


// based on http://developer.android.com/resources/articles/timed-ui-updates.html
/** a class that allows using a timer , which calls a task after a certain amount of time . */
public class AndroidTimer
{
  private final Handler  _handler = new Handler();
  private final long     _delay;
  /** the input task of whoever uses this class */
  final Runnable         _taskToRun;
  /** a task that will also remove the task once it has called, so it won't repeat itself */
  private final Runnable _updateTimeTask;

  enum TimerStatus
  {
    UNINITIALIZED, SCHECHULED, FINISHED, CANCELLED
  };

  /** the current status of the timer */
  private TimerStatus currentTimeStatus = TimerStatus.UNINITIALIZED;

  /**
   * ctor.will not start the timer till you use a method to start it (like startSchedule)
   * @param taskToSchedule the task to run after the specified time
   * @param delay the time(in ms) till the task will start , since the time that you ran a method to start the timer
   */
  public AndroidTimer(final Runnable taskToSchedule, final long delay)
  {
    _taskToRun = taskToSchedule;
    _updateTimeTask = new Runnable()
    {
      @Override
      public void run()
      {
        _taskToRun.run();
        // mHandler.removeCallbacks(mUpdateTimeTask);
      }
    };
    this._delay = delay;
  }

  /** returns the current status of the timer */
  public TimerStatus getCurrentTimerStatus()
  {
    return currentTimeStatus;
  }

  /** stops the timer and then immediatly runs its task */
  public void stopScheduleAndRunTask()
  {
    stopSchedule();
    _taskToRun.run();
    currentTimeStatus = TimerStatus.FINISHED;
  }

  /**
   * resets the timer to wait the delay time again from the time you call this method.it also works if the timer already did its task . for example, if it started 2 seconds ago and had a delay of 3 , and now you call it again , it will call the task at time 5
   */
  public void reschedule()
  {
    if (getCurrentTimerStatus() == TimerStatus.SCHECHULED)
      stopSchedule();
    startSchedule();
  }

  /**
   * starts the timer, so that after the specified time, it will run its task.note that you shouldn't run this method once it has already started in the past
   */
  public void startSchedule()
  {
    currentTimeStatus = TimerStatus.SCHECHULED;
    _handler.postDelayed(_updateTimeTask, _delay);
  }

  /**
   * stops the scheduling of the task . once this method is called, the timer doesn't do anything .
   */
  public void stopSchedule()
  {
    _handler.removeCallbacks(_updateTimeTask);
    currentTimeStatus = TimerStatus.CANCELLED;
  }
}
