package uestc.arbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uestc.arbc.background.CloudManage;
import uestc.arbc.background.Interface;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;
import uestc.arbc.background.TimeThread;

/**
 * activity working
 * Created by CK on 2016/11/6.
 */

public class WorkMainActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "WorkMainActivity";

    TextView textViewStoreName;
    TextView textViewBedID;
    TextView textViewCustomerInfo;

    ImageButton imageButtonMainBoxCtrlUP;
    ImageButton imageButtonMainBoxCtrlDown;
    ImageButton imageButtonMainBoxCtrlStop;

    ImageButton imageButtonHeatFront;
    ImageButton imageButtonHeatBack;
    ImageButton imageButtonHeatStop;

    ImageButton imageButtonIgniteMain;
    ImageButton imageButtonIgniteBackup;
    ImageButton imageButtonIgniteStop;

    ImageButton imageButtonFanOn;
    ImageButton imageButtonFanOff;
    ImageButton imageButtonFanStop;

    ImageButton imageButtonCancel;
    Button buttonPause;
    Button buttonCheckout;

    TextView textViewMainBoxPosition;
    TextView textViewWorkTimeMin;
    TextView textViewWorkTimeSec;
    TextView textViewBedTemperatureFront;
    TextView textViewBedTemperatureBack;
    TextView textViewDegreeBody;
    TextView textViewHeartRate;
    ImageView imageViewHeatBoardWorkStateFL;
    ImageView imageViewHeatBoardWorkStateFR;
    ImageView imageViewHeatBoardWorkStateBL;
    ImageView imageViewHeatBoardWorkStateBR;
    ImageView imageViewIgniteBoardWorkStateFL;
    ImageView imageViewIgniteBoardWorkStateFR;
    ImageView imageViewIgniteBoardWorkStateBL;
    ImageView imageViewIgniteBoardWorkStateBR;
    ImageView imageViewFanWorkStateOn;
    ImageView imageViewFanWorkStateOff;
    ImageView imageViewFanWorkStateStop;
    TextView textViewTime;

    CloudManage.DeviceState deviceState = null;

    MyHandler handler = new MyHandler(TAG) {

        AlertDialog dialogCloud = null;
        AlertDialog dialogLocal = null;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ManageApplication.MESSAGE_TIME:
                    textViewTime.setText(msg.obj.toString());
                    break;
                case ManageApplication.MESSAGE_SERVER_CONNECTED:
                    if (null != dialogCloud) {
                        dialogCloud.dismiss();
                        dialogCloud = null;
                    }
                    deviceState.startLoop();
                    break;
                case ManageApplication.MESSAGE_SERVER_DISCONNECTED:
                    if (null == dialogCloud) {
                        dialogCloud = new AlertDialog.Builder(WorkMainActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("云端连接异常，请等待连接或退出程序")//设置显示的内容

                                .setCancelable(false).setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                }).create();
                        dialogCloud.show();
                    }
                    deviceState.stopLoop();
                    break;
                case ManageApplication.MESSAGE_DEVICE_CONNECTED:
                    //nothing need to do
                    break;
                case ManageApplication.MESSAGE_DEVICE_DISCONNECTED:
                    if (null == dialogLocal) {
                        dialogLocal = new AlertDialog.Builder(WorkMainActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("艾灸机连接异常")//设置显示的内容

                                .setCancelable(false).create();
                        dialogLocal.show();
                    }
                    break;
                case ManageApplication.MESSAGE_DEVICE_STATE:
                    if (null != dialogLocal) {
                        dialogLocal.dismiss();
                        dialogLocal = null;
                    }
                    if (null != msg.obj) {
                        updateDeviceState((JSONObject) msg.obj);
                    }
                    break;

                case ManageApplication.MESSAGE_WORK_ERROR:
                    try {
                        Toast.makeText(WorkMainActivity.this, Interface.getMessage((JSONObject) msg.obj), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(WorkMainActivity.this, "error！", Toast.LENGTH_LONG).show();
                    }
                    finish();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.workmain);

        init();

    }

    private void init() {

        L.d(TAG, "init start");

        //显示时间的控件
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        L.d(TAG, "time init done");

        //关闭按钮
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);
        imageButtonCancel.setOnClickListener(this);

        //暂停按钮
        buttonPause = (Button) findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(this);

        //结帐按钮
        buttonCheckout = (Button) findViewById(R.id.buttonCheckout);
        buttonCheckout.setOnClickListener(this);

        //接下来一堆控制按钮
        imageButtonMainBoxCtrlUP = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlUp);
        imageButtonMainBoxCtrlUP.setOnClickListener(this);
        imageButtonMainBoxCtrlDown = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlDown);
        imageButtonMainBoxCtrlDown.setOnClickListener(this);
        imageButtonMainBoxCtrlStop = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlStop);
        imageButtonMainBoxCtrlStop.setOnClickListener(this);

        imageButtonIgniteMain = (ImageButton) findViewById(R.id.imageButtonIgniteMain);
        imageButtonIgniteMain.setTag(1);
        imageButtonIgniteMain.setOnClickListener(this);
        imageButtonIgniteBackup = (ImageButton) findViewById(R.id.imageButtonIgniteBackup);
        imageButtonIgniteBackup.setTag(1);
        imageButtonIgniteBackup.setOnClickListener(this);
        imageButtonIgniteStop = (ImageButton) findViewById(R.id.imageButtonIgniteStop);
        imageButtonIgniteStop.setOnClickListener(this);

        imageButtonHeatFront = (ImageButton) findViewById(R.id.imageButtonHeatFront);
        imageButtonHeatFront.setOnClickListener(this);
        imageButtonHeatBack = (ImageButton) findViewById(R.id.imageButtonHeatBack);
        imageButtonHeatBack.setOnClickListener(this);
        imageButtonHeatStop = (ImageButton) findViewById(R.id.imageButtonHeatStop);
        imageButtonHeatStop.setOnClickListener(this);

        imageButtonFanOn = (ImageButton) findViewById(R.id.imageButtonFanOn);
        imageButtonFanOn.setOnClickListener(this);
        imageButtonFanOff = (ImageButton) findViewById(R.id.imageButtonFanOff);
        imageButtonFanOff.setOnClickListener(this);
        imageButtonFanStop = (ImageButton) findViewById(R.id.imageButtonFanStop);
        imageButtonFanStop.setOnClickListener(this);


        L.d(TAG, "button init done");

        textViewStoreName = (TextView) findViewById(R.id.textViewStoreName);
        textViewStoreName.setText(ManageApplication.getInstance().storeName);
        textViewBedID = (TextView) findViewById(R.id.textViewBedID);
        textViewBedID.setText("床号：" + String.valueOf(ManageApplication.getInstance().bedID));
        textViewCustomerInfo = (TextView) findViewById(R.id.textViewCustomerInfo);


        textViewMainBoxPosition = (TextView) findViewById(R.id.textViewMainBoxPosition);
        textViewWorkTimeMin = (TextView) findViewById(R.id.textViewWorkTimeMin);
        textViewWorkTimeSec = (TextView) findViewById(R.id.textViewWorkTimeSec);
        textViewBedTemperatureFront = (TextView) findViewById(R.id.textViewBedTemperatureFront);
        textViewBedTemperatureBack = (TextView) findViewById(R.id.textViewBedTemperatureBack);
        textViewDegreeBody = (TextView) findViewById(R.id.textViewDegreeBody);
        textViewHeartRate = (TextView) findViewById(R.id.textViewHeartRate);
        imageViewHeatBoardWorkStateFL = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateFL);
        imageViewHeatBoardWorkStateFR = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateFR);
        imageViewHeatBoardWorkStateBL = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateBL);
        imageViewHeatBoardWorkStateBR = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateBR);
        imageViewIgniteBoardWorkStateFL = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateFL);
        imageViewIgniteBoardWorkStateFR = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateFR);
        imageViewIgniteBoardWorkStateBL = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateBL);
        imageViewIgniteBoardWorkStateBR = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateBR);
        imageViewFanWorkStateOn = (ImageView) findViewById(R.id.imageViewFanWorkStateOn);
        imageViewFanWorkStateOff = (ImageView) findViewById(R.id.imageViewFanWorkStateOff);
        imageViewFanWorkStateStop = (ImageView) findViewById(R.id.imageViewFanWorkStateStop);

        deviceState = ManageApplication.getInstance().getCloudManage().makeDeviceState(handler);
        deviceState.setDelay(1000);
    }

    private void updateDeviceState(JSONObject jsonObject) {

        try {
            JSONObject jsonData = Interface.getData(jsonObject);

            Interface.MonitorInfo monitorInfo = new Interface.MonitorInfo(jsonData);

            if (!monitorInfo.isWork) {
                finish();
            }

            textViewBedTemperatureBack.setText(String.valueOf(monitorInfo.degreeBack));

            textViewBedTemperatureFront.setText(String.valueOf(monitorInfo.degreeFore));

            textViewMainBoxPosition.setText(String.valueOf(monitorInfo.posMainMotor));

            Boolean ignite3;
            if (0 == monitorInfo.stateIgniteBL) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                ignite3 = false;
            } else if (1 == monitorInfo.stateIgniteBL) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                ignite3 = true;
            } else {
                L.e(TAG, "value of stateDianBackLeft is" + monitorInfo.stateIgniteBL);
                ignite3 = null;
            }

            Boolean ignite4;
            if (0 == monitorInfo.stateIgniteBR) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                ignite4 = false;
            } else if (1 == monitorInfo.stateIgniteBR) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                ignite4 = true;
            } else {
                L.e(TAG, "value of stateDianBackRight is" + monitorInfo.stateIgniteBR);
                ignite4 = null;
            }

            Boolean ignite1;
            if (0 == monitorInfo.stateIgniteFL) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                ignite1 = false;
            } else if (1 == monitorInfo.stateIgniteFL) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                ignite1 = true;
            } else {
                L.e(TAG, "value of stateDianForeLeft is" + monitorInfo.stateIgniteFL);
                ignite1 = null;
            }

            Boolean ignite2;
            if (0 == monitorInfo.stateIgniteFR) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                ignite2 = false;
            } else if (1 == monitorInfo.stateIgniteFR) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                ignite2 = true;
            } else {
                L.e(TAG, "value of stateDianForeRight is" + monitorInfo.stateIgniteFR);
                ignite2 = null;
            }

            if (null != ignite1 && null != ignite2 && null != ignite3 && null != ignite4) {
                if (ignite1 || ignite3) {
                    imageButtonIgniteMain.setImageResource(R.drawable.pic_button_ignite_on);
                    imageButtonIgniteMain.setTag(0);
                } else {
                    imageButtonIgniteMain.setImageResource(R.drawable.pic_button_ignite_off);
                    imageButtonIgniteMain.setTag(1);
                }

                if (ignite2 || ignite4) {
                    imageButtonIgniteBackup.setImageResource(R.drawable.pic_button_ignite_on);
                    imageButtonIgniteBackup.setTag(0);
                } else {
                    imageButtonIgniteBackup.setImageResource(R.drawable.pic_button_ignite_off);
                    imageButtonIgniteBackup.setTag(1);
                }
            }

            Boolean heat3;
            if (0 == monitorInfo.stateHeatBL) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                heat3 = false;
            } else if (1 == monitorInfo.stateHeatBL) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                heat3 = true;
            } else {
                L.e(TAG, "value of stateHotBackLeft is" + monitorInfo.stateHeatBL);
                heat3 = null;
            }

            Boolean heat4;
            if (0 == monitorInfo.stateHeatBR) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                heat4 = false;
            } else if (1 == monitorInfo.stateHeatBR) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                heat4 = true;
            } else {
                L.e(TAG, "value of stateHotBackRight is" + monitorInfo.stateHeatBR);
                heat4 = null;
            }

            Boolean heat1;
            if (0 == monitorInfo.stateHeatFL) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                heat1 = false;
            } else if (1 == monitorInfo.stateHeatFL) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                heat1 = true;
            } else {
                L.e(TAG, "value of stateHotForeLeft is" + monitorInfo.stateHeatFL);
                heat1 = null;
            }

            Boolean heat2;
            if (0 == monitorInfo.stateHeatFR) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                heat2 = false;
            } else if (1 == monitorInfo.stateHeatFR) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                heat2 = true;
            } else {
                L.e(TAG, "value of stateHotForeRight is" + monitorInfo.stateHeatFR);
                heat2 = null;
            }

            if (null != heat1 && null != heat2 && null != heat3 && null != heat4) {
                if (heat1 || heat2) {
                    imageButtonHeatFront.setImageResource(R.drawable.pic_button_heat_on);
                } else {
                    imageButtonHeatFront.setImageResource(R.drawable.pic_button_heat_off);
                }
                if (heat3 || heat4) {
                    imageButtonHeatBack.setImageResource(R.drawable.pic_button_heat_on);
                } else {
                    imageButtonHeatBack.setImageResource(R.drawable.pic_button_heat_off);
                }
            }

            long time = monitorInfo.currentTime - monitorInfo.startTime;
            textViewWorkTimeMin.setText(String.valueOf(time / 60));
            textViewWorkTimeSec.setText(String.valueOf(time % 60));

            if (0 == monitorInfo.stateWind) {
                imageButtonFanOn.setImageResource(R.drawable.pic_button_fan_off);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_fan_off);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lighton);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == monitorInfo.stateWind) {
                imageButtonFanOn.setImageResource(R.drawable.pic_button_fan_on);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_fan_off);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lighton);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lightoff);
            } else if (2 == monitorInfo.stateWind) {
                imageButtonFanOn.setImageResource(R.drawable.pic_button_fan_off);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_fan_on);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lighton);
            } else {
                L.e(TAG, "value of stateWind is" + monitorInfo.stateWind);
            }

            if (0 == monitorInfo.stateMainMotor) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_motor_released);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_motor_released);
            } else if (1 == monitorInfo.stateMainMotor) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_motor_pressed);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_motor_released);
            } else if (2 == monitorInfo.stateMainMotor) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_motor_released);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_motor_pressed);
            } else {
                L.e(TAG, "value of stateMainMotor is" + monitorInfo.stateMainMotor);
            }

            textViewCustomerInfo.setText(monitorInfo.customerName + " " + monitorInfo.customerSex + " " + monitorInfo.customerAge + getString(R.string.quantifier_age));

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        JSONObject jsonObject;
        switch (view.getId()) {
            case R.id.buttonPause:
                jsonObject = Interface.devicePause();
                if (null != jsonObject) {
                    try {
                        if (Interface.isError(jsonObject)) {
                            Toast.makeText(this, Interface.getMessage(jsonObject), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.imageButtonCancel:
                finish();
                break;

            case R.id.buttonCheckout:
                checkout();
                break;

            case R.id.imageButtonMainBoxCtrlUp:
                Interface.bedControlMainMotorUp();
                break;

            case R.id.imageButtonMainBoxCtrlDown:
                Interface.bedControlMainMotorDown();
                break;

            case R.id.imageButtonMainBoxCtrlStop:
                Interface.bedControlMainMotorStop();
                break;

            case R.id.imageButtonIgniteMain:
                jsonObject = Interface.bedControlIgniteMainOn();
                try {
                    if (Interface.isError(jsonObject)) {
                        Toast.makeText(this, Interface.getMessage(jsonObject), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.imageButtonIgniteBackup:
                jsonObject = Interface.bedControlIgniteBackupOn();
                try {
                    if (Interface.isError(jsonObject)) {
                        Toast.makeText(this, Interface.getMessage(jsonObject), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.imageButtonIgniteStop:
                Interface.bedControlIgniteOff();
                break;

            case R.id.imageButtonHeatFront:
                Interface.bedControlHeatFrontOn();
                break;

            case R.id.imageButtonHeatBack:
                Interface.bedControlHeatBackOn();
                break;

            case R.id.imageButtonHeatStop:
                Interface.bedControlHeatOff();
                break;

            case R.id.imageButtonFanOn:
                Interface.bedControlFanOn();
                break;

            case R.id.imageButtonFanOff:
                Interface.bedControlFanOff();
                break;

            case R.id.imageButtonFanStop:
                Interface.bedControlFanStop();
                break;

            default:
                break;
        }
        deviceState.getOnce();
    }

    void checkout() {
        try {
            JSONObject jsonObject = Interface.getCheckoutInfo();
            if (null == jsonObject) {
                L.e(TAG, "get checkout info fail: jsonObject is null");
                return;
            }
            if (Interface.isError(jsonObject)) {
                Toast.makeText(this, Interface.getMessage(jsonObject), Toast.LENGTH_LONG).show();
                return;
            }
            JSONObject jsonData = Interface.getData(jsonObject);
            if (null == jsonData) {
                L.e(TAG, "get checkout info fail: jsonData is null");
                return;
            }

            Interface.CheckoutInfo checkoutInfo = new Interface.CheckoutInfo(jsonData);

            listCheckoutInfo.clear();
            listCheckoutInfo.add(new CheckoutInfo("客户信息", checkoutInfo.CustomerInfo));
            listCheckoutInfo.add(new CheckoutInfo("保健床名", checkoutInfo.bedName));
            listCheckoutInfo.add(new CheckoutInfo("开始时间", TimeThread.getStringTime(checkoutInfo.startTime * 1000)));
            listCheckoutInfo.add(new CheckoutInfo("工作时间", checkoutInfo.workTime / 60 + getString(R.string.quantifier_minutes) + checkoutInfo.workTime % 60 + getString(R.string.quantifier_seconds)));
            listCheckoutInfo.add(new CheckoutInfo("艾绒类型", checkoutInfo.rawType));
            listCheckoutInfo.add(new CheckoutInfo("艾绒价格", String.valueOf(checkoutInfo.rawPrice)));
            listCheckoutInfo.add(new CheckoutInfo("服务项目", checkoutInfo.serviceType));
            listCheckoutInfo.add(new CheckoutInfo("服务价格", String.valueOf(checkoutInfo.servicePrice)));
            listCheckoutInfo.add(new CheckoutInfo("结帐价格", String.valueOf(checkoutInfo.totalPrice)));

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View layout = getLayoutInflater().inflate(R.layout.checkout_frame, null);
            ListView listViewCheckoutList = (ListView) layout.findViewById(R.id.listViewCheckoutList);
            listViewCheckoutList.setAdapter(checkoutAdapter);
            builder.setView(layout);
            builder.setTitle("结帐信息：");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("结帐", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    final JSONObject jsonObjectSubmit = Interface.checkoutSubmit();
                    if (null == jsonObjectSubmit) {
                        L.e(TAG, "checkout submit fail: jsonObjectSubmit is null");
                        return;
                    }
                    dialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (Interface.isError(jsonObjectSubmit)) {
                                    Toast.makeText(WorkMainActivity.this, Interface.getMessage(jsonObjectSubmit), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Toast.makeText(WorkMainActivity.this, "结帐成功!", Toast.LENGTH_LONG).show();
                                finish();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            builder.create().show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private List<CheckoutInfo> listCheckoutInfo = new ArrayList<>();

    private class CheckoutInfo {
        String stringBillInfo;
        String stringCheckoutExplain;

        CheckoutInfo(@NonNull String stringBillInfo, @NonNull String stringCheckoutExplain) {
            this.stringBillInfo = stringBillInfo;
            this.stringCheckoutExplain = stringCheckoutExplain;
        }
    }

    private BaseAdapter checkoutAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return listCheckoutInfo.size();
        }

        @Override
        public Object getItem(int position) {
            return listCheckoutInfo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = getLayoutInflater().inflate(R.layout.checkout_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.textViewBillInfo = (TextView) convertView.findViewById(R.id.textViewBillInfo);
                viewHolder.textViewCheckoutExplain = (TextView) convertView.findViewById(R.id.textViewCheckoutExplain);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final String stringBillInfo = listCheckoutInfo.get(position).stringBillInfo;
            final String stringCheckoutExplain = listCheckoutInfo.get(position).stringCheckoutExplain;
            viewHolder.textViewBillInfo.setText(stringBillInfo);
            viewHolder.textViewCheckoutExplain.setText(stringCheckoutExplain);

            return convertView;
        }

        class ViewHolder {
            TextView textViewBillInfo;
            TextView textViewCheckoutExplain;
        }
    };

    @Override
    protected void onPause() {
        ManageApplication.getInstance().removeCurrentHandler(handler);
        ManageApplication.getInstance().removeCurrentActivity(this);
        deviceState.stopLoop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ManageApplication.getInstance().setCurrentActivity(this);
        ManageApplication.getInstance().setCurrentActivityHandler(handler);
        deviceState.startLoop();
    }

    @Override
    protected void onDestroy() {
        L.d(TAG, "onDestroy()");
        super.onDestroy();
    }
}
