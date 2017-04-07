package uestc.arbc.background;


import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import ck.bletool.BLE;
import ck.bletool.BLEConnector;
import ck.bletool.BLEScanner;
import uestc.arbc.R;

/**
 * BLEManage
 * Created by CK on 2017/4/4.
 */

public class BLEManage {

    private final static String TAG = "BLEManage";

    private final static UUID SERVICE_UUID = UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
    private final static UUID CHARACTERISTIC_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
    private final static UUID DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private final static String ACTION_CHARACTERISTIC_CHANGE = "android.intent.action.CHARACTERISTIC_CHANGE";

    private final static String[] RESPONSE_MESSAGE = new String[]{"成功", "版本号不正确，此协议只接受1", "长度信息和命令要求不匹配", "类型信息和命令要求不匹配", "命令不存在", "序列号不正常", "设备已经被绑定", "绑定信息和设备内部不匹配，无法删除绑定", "登录信息和设备内部不匹配，无法登录", "还没有登录，先登录先", "指令不支持，很多指令是设备发出去的，并不能接收，参考具体指令介绍", "指针移动失败，一般命令格式不对或者是指针已经移动到最末尾位置", "包数据不完整", "Data 不正确", "Param 不正确", "内存不够", "指令内部返回，不走标准返回模式"};

    private final static int CMD_SU_LOGIN = 0x24;
    private final static int CMD_SET_TIME = 0x01;
    private final static int CMD_GET_HEART = 0x41;
    private final static int CMD_GET_TEMP = 0x44;
    private final static int CMD_HEART_BACK = 0x47;
    private final static int CMD_TEMP_BACK = 0x48;

    private Context context;
    private WindowManager windowManager;
    private BLE ble;
    private volatile boolean keepConnected = false;
    private volatile boolean isDeviceConnected = false;

    private View viewFloatBall;
    private boolean isFloatBallShowing = false;
    private View viewBLEMenu;
    private boolean isBLEMenuShowing = false;
    private AlertDialog alertDialogDeviceList;
    private boolean isAlertDialogDeviceListShowing = false;
    private MyBroadcastReceiver broadcastReceiver;

    class MyBroadcastReceiver extends BroadcastReceiver {

        private static final String TAG = "MyBroadcastReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] value = intent.getByteArrayExtra("value");
            L.i(TAG, BLE.bytesToHexString(value));
            int cmd = value[1];
            if (CMD_SU_LOGIN == cmd) {
                L.i(TAG, "suLogin：" + RESPONSE_MESSAGE[value[4]]);
                if (0x00 == value[4]) {
                    Toast.makeText(context, "连接成功！", Toast.LENGTH_SHORT).show();
                    isDeviceConnected = true;
                    cmdSetTime();
                } else {
                    Toast.makeText(context, "登录出错：" + RESPONSE_MESSAGE[value[4]], Toast.LENGTH_SHORT).show();
                }
            } else if (CMD_SET_TIME == cmd) {
                L.i(TAG, "setTime：" + RESPONSE_MESSAGE[value[4]]);
                if (0x00 == value[4]) {
                    cmdGetData();
                }
            } else if (CMD_HEART_BACK == cmd) {
                L.i(TAG, "心率返回：" + value[4]);
            } else if (CMD_TEMP_BACK == cmd) {
                float temp = (value[5] * 0xFF + value[4]) / 10.0F;
                L.i(TAG, "体温返回：" + temp + "====Raw:" + BLE.bytesToHexString(value));
            } else {
                L.i(TAG, BLE.bytesToHexString(value));
            }
        }

    }

    private View.OnTouchListener dragOnTouchListener = new View.OnTouchListener() {
        float startX;
        float startY;
        float tempX;
        float tempY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();

                    tempX = event.getRawX();
                    tempY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float x = event.getRawX() - startX;
                    float y = event.getRawY() - startY;
                    //计算偏移量，刷新视图
                    LayoutParams layoutParams = (LayoutParams) v.getTag();
                    layoutParams.x -= x;
                    layoutParams.y += y;
                    v.setTag(layoutParams);
                    windowManager.updateViewLayout(v, layoutParams);
                    startX = event.getRawX();
                    startY = event.getRawY();
                    break;
                case MotionEvent.ACTION_UP:
                    if (Math.abs(event.getRawX() - tempX) > 10 || Math.abs(event.getRawY() - tempY) > 10) {
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void init(@NonNull Context context) {

        this.context = context;

        broadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BLEManage.ACTION_CHARACTERISTIC_CHANGE);
        context.registerReceiver(broadcastReceiver, filter);

        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (null == windowManager) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(context);
        viewFloatBall = inflater.inflate(R.layout.layout_floatball, null);
        viewFloatBall.setOnTouchListener(dragOnTouchListener);
        viewFloatBall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBLEMenu();
            }
        });
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.x = 30;
        layoutParams.y = 150;
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        viewFloatBall.setTag(layoutParams);

        ble = new BLE(context);
        ble.getBLEChecker().bluetoothCheck();
        ble.getBLEScanner().setBLEScanCallBack(new BLEScanner.BLEScanCallBack() {
            @Override
            public void onScan(final BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                if (!bluetoothDeviceList.contains(bluetoothDevice)) {
                    bluetoothDeviceList.add(bluetoothDevice);
                    ManageApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bluetoothDeviceAdapter.notifyDataSetChanged();
                        }
                    });

                }
            }
        });
    }

    private void updateUI() {
        if (isAlertDialogDeviceListShowing) {
            cancelBLEMenu();
            showFloatBall(false);
        } else if (isBLEMenuShowing) {
            showFloatBall(false);
        } else if (!isFloatBallShowing) {
            showFloatBall(true);
        }

    }

    private void cancelBLEMenu() {
        try {
            windowManager.removeView(viewBLEMenu);
            isBLEMenuShowing = false;
        } catch (Exception e) {
            L.i(TAG, "bleMenu not attached");
        }
    }

    public void showFloatBall(boolean show) {
        if (null == viewFloatBall || null == windowManager) {
            return;
        }

        try {
            if (show) {
                if (isFloatBallShowing) {
                    return;
                }
                windowManager.addView(viewFloatBall, (LayoutParams) viewFloatBall.getTag());
                isFloatBallShowing = true;
            } else {
                if (!isFloatBallShowing) {
                    return;
                }
                windowManager.removeView(viewFloatBall);
                isFloatBallShowing = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showBLEMenu() {
        LayoutInflater inflater = LayoutInflater.from(context);
        viewBLEMenu = inflater.inflate(R.layout.layout_ble_menu, null);
        TextView textViewConnectState = (TextView) viewBLEMenu.findViewById(R.id.textViewConnectState);
        Button buttonScan = (Button) viewBLEMenu.findViewById(R.id.buttonScan);
        Button buttonDisconnect = (Button) viewBLEMenu.findViewById(R.id.buttonDisconnect);
        Button buttonCancel = (Button) viewBLEMenu.findViewById(R.id.buttonCancel);
        LayoutParams layoutParams = new LayoutParams();
        layoutParams.type = LayoutParams.TYPE_SYSTEM_ALERT;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.END | Gravity.TOP;
        layoutParams.x = ((LayoutParams) viewFloatBall.getTag()).x + viewBLEMenu.getWidth();
        layoutParams.y = ((LayoutParams) viewFloatBall.getTag()).y;

        viewBLEMenu.setTag(layoutParams);

        if (isDeviceConnected) {
            textViewConnectState.setText(R.string.ble_connected);
        } else {
            textViewConnectState.setText(R.string.ble_disconnected);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });
        buttonDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keepConnected = false;
                disconnectDevice();
                isDeviceConnected = false;
                Toast.makeText(context, "连接已断开!", Toast.LENGTH_SHORT).show();
                windowManager.removeView(viewBLEMenu);
                showFloatBall(true);
            }
        });
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelBLEMenu();
                updateUI();
            }
        });
        windowManager.addView(viewBLEMenu, layoutParams);

        isBLEMenuShowing = true;
        updateUI();
    }

    private List<BluetoothDevice> bluetoothDeviceList = new ArrayList<>();
    private BaseAdapter bluetoothDeviceAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return bluetoothDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return bluetoothDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = LayoutInflater.from(context).inflate(R.layout.layout_ble_devicelist_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceName = (TextView) convertView.findViewById(R.id.textViewDeviceName);
                viewHolder.deviceAddr = (TextView) convertView.findViewById(R.id.textViewDeviceAddr);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.deviceName.setText(bluetoothDeviceList.get(position).getName());
            viewHolder.deviceAddr.setText(bluetoothDeviceList.get(position).getAddress());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelDeviceList();
                    connectDevice(bluetoothDeviceList.get(position));
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView deviceName;
            TextView deviceAddr;
        }
    };
    private BLEConnector bleConnector;
    private volatile int failedTime = 0;
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            L.i("Gatt", "onConnectionStateChange()");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                failedTime = 0;
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    L.i("Gatt", "connected:" + gatt.getDevice().getName() + " " + gatt.getDevice().getAddress());
                    L.i("Gatt", "discovering services");
                    bleConnector.setBluetoothGatt(gatt);
                    if (bleConnector.getServices().isEmpty()) {
                        bleConnector.discoverServices();
                    }
                } else {
                    L.i("Gatt", "disconnected:" + gatt.getDevice().getName() + " " + gatt.getDevice().getAddress());
                    isDeviceConnected = false;
                    ManageApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "连接已断开!", Toast.LENGTH_SHORT).show();
                            if (keepConnected) {
                                Toast.makeText(context, "重新连接中。。。", Toast.LENGTH_SHORT).show();
                                bleConnector.connect();
                            }
                        }
                    });
                }
            } else {
                isDeviceConnected = false;
                if (failedTime > 5) {
                    ManageApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "连接失败！", Toast.LENGTH_SHORT).show();
                        }
                    });
                    failedTime = 0;
                    return;
                }
                L.i("Gatt", "Connect failed :retrying...");

                ManageApplication.getInstance().getCurrentActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "连接失败，重试中。。。", Toast.LENGTH_SHORT).show();
                        bleConnector.connect();
                    }
                });
                failedTime++;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (BLEConnector.STATUS_SUCCESS == status) {
                L.i("Gatt", "onServicesDiscovered() succeed");
                if (bleConnector.enableNotify(SERVICE_UUID, CHARACTERISTIC_UUID, DESCRIPTOR_UUID)) {
                    L.i("Gatt", "enableNotify() succeed");
                } else {
                    L.i("Gatt", "enableNotify() failed");
                }
            } else {
                L.i("Gatt", "onServicesDiscovered() failed : status = " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            L.i("Gatt", "onCharacteristicWrite():" + BLE.bytesToHexString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            L.i("Gatt", "onCharacteristicChanged():" + BLE.bytesToHexString(characteristic.getValue()));
            Intent intent = new Intent(BLEManage.ACTION_CHARACTERISTIC_CHANGE);
            intent.putExtra("value", characteristic.getValue());
            context.sendBroadcast(intent);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            L.i("Gatt", "onDescriptorWrite():" + BLE.bytesToHexString(descriptor.getValue()));
            cmdSuLogin();
        }
    };

    private void connectDevice(BluetoothDevice bluetoothDevice) {
        bleConnector = ble.getBLEConnector(bluetoothDevice);
        bleConnector.setBluetoothGattCallback(bluetoothGattCallback);
        keepConnected = true;

        bleConnector.connect();
        Toast.makeText(context, "连接中。。。", Toast.LENGTH_SHORT).show();
    }

    private void scan() {

        Activity currentActivity = ManageApplication.getInstance().getCurrentActivity();
        if (null == currentActivity) {
            return;
        }

        bluetoothDeviceList.clear();
        cancelBLEMenu();
        View viewDeviceList = LayoutInflater.from(context).inflate(R.layout.layout_ble_devicelist_frame, null);
        ListView listViewWatchList = (ListView) viewDeviceList.findViewById(R.id.listViewBLEList);
        listViewWatchList.setAdapter(bluetoothDeviceAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(currentActivity);
        builder.setTitle("扫描到设备");
        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ble.getBLEScanner().stopScan();
                isAlertDialogDeviceListShowing = false;
                updateUI();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cancelDeviceList();
            }
        });
        builder.setView(viewDeviceList);
        alertDialogDeviceList = builder.create();
        alertDialogDeviceList.show();
        ble.getBLEScanner().startScan();
        isAlertDialogDeviceListShowing = true;
        updateUI();
    }

    private void disconnectDevice() {
        keepConnected = false;
        isDeviceConnected = false;
        if (null != bleConnector) {
            bleConnector.close();
        }
    }

    private void cancelDeviceList() {
        if (null != alertDialogDeviceList) {
            alertDialogDeviceList.cancel();
        }
    }

    public void close() {
        try {
            ble.getBLEScanner().stopScan();
        } catch (Exception e) {
            e.printStackTrace();
        }
        disconnectDevice();
        if (null != broadcastReceiver) {
            context.unregisterReceiver(broadcastReceiver);
        }
        try {
            alertDialogDeviceList.cancel();
        } catch (Exception e) {
            L.i(TAG, "alertDialogDeviceList cancel error");
        }
        try {
            windowManager.removeView(viewBLEMenu);
        } catch (Exception e) {
            L.i(TAG, "viewBLEMenu remove error");
        }
        try {
            windowManager.removeView(viewFloatBall);
        } catch (Exception e) {
            L.i(TAG, "viewFloatBall remove error");
        }
    }

    private void cmdSend(int cmd, byte[] data) {
        int length = data.length;
        byte v = 1;
        byte t = 0;
        final byte[] value = new byte[20];
        value[0] = (byte) (v << 5 | length - 1 << 1 | t);
        value[1] = (byte) (cmd & 255);
        value[2] = 0;
        value[3] = 0;
        System.arraycopy(data, 0, value, 4, data.length);

        bleConnector.writeCharacteristic(SERVICE_UUID, CHARACTERISTIC_UUID, value);
    }

    private void cmdSuLogin() {
        L.i(TAG, "cmdSuLogin()");
        byte[] SUPER_LOGIN_DATA = {0x01, 0x23, 0x45, 0X67,
                (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
                (byte) 0xFE, (byte) 0xDC, (byte) 0xBA, (byte) 0x98,
                0x76, 0x54, 0x32, 0x10};
        cmdSend(CMD_SU_LOGIN, SUPER_LOGIN_DATA);
    }

    private void cmdSetTime() {
        byte[] value = nowTimeToBytes();
        L.i(TAG, "cmdSetTime()");
        cmdSend(CMD_SET_TIME, value);
    }

    private static byte[] nowTimeToBytes() {
        byte[] result = new byte[4];
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 2016;
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        result[0] = (byte) (year << 2 | month >> 2 & 3);
        result[1] = (byte) ((month & 3) << 6 | day << 1 | hour >> 4 & 1);
        result[2] = (byte) ((hour & 15) << 4 | minute >> 2 & 15);
        result[3] = (byte) ((minute & 3) << 6 | second);
        return result;
    }

    private void cmdGetHeart() {
        byte[] value = {0x01};
        L.d(TAG, "cmdGetHeart()");
        cmdSend(CMD_GET_HEART, value);
    }

    private void cmdGetTemp() {
        byte[] value = {0x01};
        L.d(TAG, "cmdGetTemp()");
        cmdSend(CMD_GET_TEMP, value);
    }

    private void cmdGetData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isDeviceConnected) {
                    cmdGetHeart();
                    SystemClock.sleep(2000);
                    cmdGetTemp();
                    SystemClock.sleep(2000);
                }
            }
        }).start();
    }
}
