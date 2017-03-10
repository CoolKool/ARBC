package uestc.arbc.background;

import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

/**
 * 更新其它activity中的显示时间
 * Created by CK on 2016/11/16.
 */


public class TimeThread extends Thread {
    private final static String TAG = "TimeThread";

    private volatile boolean keepRunning = true;

    TimeThread() {
        Log.i(TAG, "initialed");
    }

    public static String getTime(@Nullable Long currentTime) {
        Calendar calendar = Calendar.getInstance();
        if (null != currentTime) {
            calendar.setTimeInMillis(currentTime);
        }
        int tmpInt;
        String hour, min, sec, time;

        if ((tmpInt = calendar.get(Calendar.HOUR_OF_DAY)) < 10) {
            hour = "0" + String.valueOf(tmpInt);
        } else {
            hour = String.valueOf(tmpInt);
        }
        if ((tmpInt = calendar.get(Calendar.MINUTE)) < 10) {
            min = "0" + String.valueOf(tmpInt);
        } else {
            min = String.valueOf(tmpInt);
        }
        if ((tmpInt = calendar.get(Calendar.SECOND)) < 10) {
            sec = "0" + String.valueOf(tmpInt);
        } else {
            sec = String.valueOf(tmpInt);
        }
        time = calendar.get(Calendar.YEAR) + "年" +
                (calendar.get(Calendar.MONTH) + 1) + "月" +
                calendar.get(Calendar.DAY_OF_MONTH) + "日 " +
                hour + ":" + min + ":" + sec;

        return time;
    }

    void close() {
        keepRunning = false;
    }

    @Override
    public void run() {
        /*Calendar calendar;
        int tmpInt;
        String hour, min, sec;*/


        while (keepRunning) {
            /*calendar = Calendar.getInstance();

            if ((tmpInt = calendar.get(Calendar.HOUR_OF_DAY)) < 10) {
                hour = "0" + String.valueOf(tmpInt);
            } else {
                hour = String.valueOf(tmpInt);
            }
            if ((tmpInt = calendar.get(Calendar.MINUTE)) < 10) {
                min = "0" + String.valueOf(tmpInt);
            } else {
                min = String.valueOf(tmpInt);
            }
            if ((tmpInt = calendar.get(Calendar.SECOND)) < 10) {
                sec = "0" + String.valueOf(tmpInt);
            } else {
                sec = String.valueOf(tmpInt);
            }
            String time = calendar.get(Calendar.YEAR) + "年" +
                    (calendar.get(Calendar.MONTH) + 1) + "月" +
                    calendar.get(Calendar.DAY_OF_MONTH) + "日 " +
                    hour + ":" + min + ":" + sec;*/

            Message message = new Message();
            message.obj = getTime(null);
            message.what = ManageApplication.MESSAGE_TIME;

            ManageApplication.getInstance().sendMessage(message);

            SystemClock.sleep(500);
        }

    }
}

