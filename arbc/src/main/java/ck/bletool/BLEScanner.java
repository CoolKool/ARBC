package ck.bletool;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * BLEScanner
 * Created by CK on 2017/4/4.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BLEScanner {
    private final static String TAG = "BLEScanner";
    private BLE ble;
    private BluetoothAdapter mBluetoothAdapter;

    private ScanCallback mScanCallBack;
    private BluetoothAdapter.LeScanCallback mLeScanCallBack;
    private BLEScanCallBack bleScanCallBack;

    BLEScanner(BLE ble) {
        this.ble = ble;
        this.mBluetoothAdapter = ble.getBluetoothAdapter();
    }


    public interface BLEScanCallBack {
        public void onScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord);
    }

    public void setBLEScanCallBack(@NonNull final BLEScanCallBack bleScanCallBack) {
        this.bleScanCallBack = bleScanCallBack;
        if (ble.getBLEChecker().isUpperVersion()) {
            mScanCallBack = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (null == result) {
                        return;
                    }

                    BluetoothDevice bluetoothDevice = result.getDevice();
                    if (null == bluetoothDevice) {
                        return;
                    }

                    int rssi = result.getRssi();
                    ScanRecord scanRecord = result.getScanRecord();
                    if (null == scanRecord) {
                        return;
                    }

                    byte[] bytesScanRecord = scanRecord.getBytes();

                    bleScanCallBack.onScan(bluetoothDevice, rssi, bytesScanRecord);
                }
            };
        } else {
            mLeScanCallBack = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    if (null == device) {
                        return;
                    }
                    bleScanCallBack.onScan(device, rssi, scanRecord);
                }
            };
        }
    }

    public void startScan() {
        if (null == bleScanCallBack) {
            Log.e(BLEScanner.TAG, "startScan(): bleScanCallBack was not set yet");
        }

        if (ble.getBLEChecker().isUpperVersion()) {
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallBack);
        } else {
            mBluetoothAdapter.startLeScan(mLeScanCallBack);
        }
    }

    public void stopScan() {
        if (null == bleScanCallBack) {
            Log.e(BLEScanner.TAG, "stopScan(): bleScanCallBack was not set yet");
        }

        if (ble.getBLEChecker().isUpperVersion()) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallBack);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallBack);
        }
    }

}
