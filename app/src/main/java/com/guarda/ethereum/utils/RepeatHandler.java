package com.guarda.ethereum.utils;

import android.os.Handler;

import java.security.InvalidParameterException;

/**
 * Created by SV on 30.08.2017.
 */

public class RepeatHandler extends Handler {
    private RescheduleRunnable task;
    private long interval;
    private boolean isInterrupted = true;
    private boolean isFirstRunImmediately = false;
    private OnTaskFinishListener mTaskFinishListener;

    /**
     * Creates repeat handler
     *
     * @param task     a {@link Runnable} to repeat
     * @param interval interval in ms to repeat the task Runnable
     */
    public RepeatHandler(Runnable task, long interval) {
        this.task = new RescheduleRunnable(task);
        this.interval = interval;
    }

    /**
     * Start repeating {@link Runnable}
     * <p>
     * passed to constructor {@link RepeatHandler#RepeatHandler(Runnable, long)}
     */
    public void start() {
        isInterrupted = false;
        if (isFirstRunImmediately){
            task.run();
        } else {
            scheduleRepeat();
        }
    }

    /**
     * Tries to interrupt repeating
     * This is done by NOT scheduling new repeats
     * and does not mean stopping current task running
     */
    public void interrupt() {
        isInterrupted = true;
        removeCallbacks(task);
        notifyTaskFinishListener();
    }

    private void scheduleRepeat() {
        if (task != null && interval > 0) {
            postDelayed(task, interval);
        } else {
            throw new InvalidParameterException("You must specify Runnable to " +
                    "run and time interval must be > 0");
        }
    }

    public boolean isInterrupted() {
        return isInterrupted;
    }

    public void setFirstRunImmediately(boolean firstRunImmediately) {
        isFirstRunImmediately = firstRunImmediately;
    }

    public void setOnTaskFinishListener(OnTaskFinishListener listener){
        this.mTaskFinishListener = listener;
    }

    private void notifyTaskFinishListener(){
        if (mTaskFinishListener != null){
            mTaskFinishListener.onFinish();
        }
    }

    private class RescheduleRunnable implements Runnable {

        private Runnable task;

        private RescheduleRunnable(Runnable task) {
            this.task = task;
        }

        @Override
        public void run() {
            if (!isInterrupted) {
                task.run();
                scheduleRepeat();
            }
        }
    }

    interface OnTaskFinishListener{
        void onFinish();
    }
}
