package uestc.arbc.background;

import android.util.Log;

/**
 * use to print log
 * Created by CK on 2017/3/18.
 */

public class L {
    static private boolean isDebugMode = true;

    private static boolean check(String TAG, String log) {
        return (null != TAG && null != log);
    }

    public static void v(String TAG, String log) {
        if (check(TAG, log) && isDebugMode) {
            Log.v(TAG, log);
        }
    }

    public static void d(String TAG, String log) {
        if (check(TAG, log) && isDebugMode) {
            Log.d(TAG, log);
        }
    }

    public static void i(String TAG, String log) {
        if (check(TAG, log)) {
            Log.i(TAG, log);
        }
    }

    public static void e(String TAG, String log) {
        if (check(TAG, log)) {
            Log.e(TAG, log);
        }
    }
}
