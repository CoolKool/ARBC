package ck.bletool;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;
import java.util.UUID;

/**
 * BLEConnector
 * Created by CK on 2017/4/4.
 */

public class BLEConnector {
    private final static String TAG = "BLEConnector";

    public final static int STATUS_SUCCESS = BluetoothGatt.GATT_SUCCESS;
    public final static int STATE_CONNECTED = BluetoothGatt.STATE_CONNECTED;

    private BLE ble;
    private BluetoothDevice bluetoothDevice;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCallback bluetoothGattCallback;

    BLEConnector(BLE ble, BluetoothDevice bluetoothDevice) {
        this.ble = ble;
        this.bluetoothDevice = bluetoothDevice;
    }

    public void setBluetoothGatt(BluetoothGatt bluetoothGatt) {
        this.bluetoothGatt = bluetoothGatt;
    }

    public void setBluetoothGattCallback(@NonNull BluetoothGattCallback bluetoothGattCallback) {
        this.bluetoothGattCallback = bluetoothGattCallback;
    }

    public synchronized BluetoothGatt connect() {
        return bluetoothGatt = bluetoothDevice.connectGatt(ble.getContext(), false, bluetoothGattCallback);
    }

    public synchronized boolean enableNotify(UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        Log.i(TAG, "enable notify()");
        BluetoothGattService bluetoothGattService = this.bluetoothGatt.getService(serviceUUID);
        if (null == bluetoothGattService) {
            return false;
        }
        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(characteristicUUID);
        if (null == bluetoothGattCharacteristic) {
            return false;
        }
        BluetoothGattDescriptor descriptor = bluetoothGattCharacteristic.getDescriptor(
                descriptorUUID);
        if (null == descriptor) {
            return false;
        }


        return bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, true) && descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE) && bluetoothGatt.writeDescriptor(descriptor);
    }

    public boolean writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, byte[] value) {
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(serviceUUID);
        if (null == bluetoothGattService) {
            return false;
        }

        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(characteristicUUID);
        if (null == bluetoothGattCharacteristic) {
            return false;
        }

        return writeCharacteristic(bluetoothGattCharacteristic, value);
    }

    public synchronized boolean writeCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic, byte[] value) {
        Log.i(TAG, "Value to write is:0x" + BLE.bytesToHexString(value));
        return bluetoothGattCharacteristic.setValue(value) && bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
    }

    public synchronized List<BluetoothGattService> getServices() {
        return bluetoothGatt.getServices();
    }

    public synchronized boolean discoverServices() {
        return bluetoothGatt.discoverServices();
    }

    public synchronized void close() {
        if (null != bluetoothGatt) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

}
