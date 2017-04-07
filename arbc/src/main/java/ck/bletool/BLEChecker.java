package ck.bletool;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import uestc.arbc.background.L;

/**
 * BLEChecker
 * Created by CK on 2017/4/4.
 */

public class BLEChecker {
    private final static String TAG = "BLEChecker";
    private Context context;
    private BLE ble;
    private boolean isUpperVersion;

    BLEChecker(BLE ble) {
        this.context = ble.getContext();
        this.ble = ble;
    }

    public boolean bluetoothCheck() {

        // 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            L.e(TAG, "BLE not support！");
            return false;
        }


        if (ble.getBluetoothManager() == null) {
            L.e(TAG, "bluetoothManager null！");
            return false;
        }


        if (ble.getBluetoothAdapter() == null) {
            L.e(TAG, "bluetoothAdapter null！");
            return false;
        }

        if (!ble.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(enableBtIntent);
        }

        isUpperVersion = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        return true;
    }

    public boolean isUpperVersion() {
        return isUpperVersion;
    }
}
