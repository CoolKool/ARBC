package ck.bletool;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * BLE
 * Created by CK on 2017/3/27.
 */

public class BLE {

    private final static String TAG = "BLE";
    public final static int BLE_CHECKER_REQUEST_ENABLE_BT = 1;
    public final static int BLE_CHECKER_REQUEST_PERMISSION_COARSE_LOCATION = 2;
    public final static int BLE_CHECKER_REQUEST_SET_LOCATION = 3;

    private Context context;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;

    private BLEChecker mBLEChecker;
    private BLEScanner mBLEScanner;


    public BLE(@NonNull Context context) {
        this.context = context;
        mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (null != mBluetoothManager) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        } else {
            mBluetoothAdapter = null;
        }

        this.mBLEChecker = new BLEChecker(this);
        this.mBLEScanner = new BLEScanner(this);
    }

    Context getContext() {
        return context;
    }

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public BLEChecker getBLEChecker() {
        return mBLEChecker;
    }

    public BLEScanner getBLEScanner() {
        return mBLEScanner;
    }

    public BLEConnector getBLEConnector(BluetoothDevice bluetoothDevice) {
        return new BLEConnector(this, bluetoothDevice);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
