package uestc.arbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;

/**
 * activity working
 * Created by CK on 2016/11/6.
 */

public class WorkMainActivity extends Activity implements View.OnClickListener {
    private final static String TAG = "WorkMainActivity";

    private boolean getDeviceState = true;
    private final static long DEVICE_STATE_DELAY = 1000;//每1秒获取一次艾灸机信息

    TextView textViewStoreID;
    TextView textViewStoreName;
    TextView textViewBedID;

    ImageButton imageButtonMainBoxCtrlUP;
    ImageButton imageButtonMainBoxCtrlDown;

    ImageButton imageButtonHeatFront;
    ImageButton imageButtonHeatBack;

    ImageButton imageButtonIgniteMain;
    ImageButton imageButtonIgniteBackup;

    ImageButton imageButtonFanOn;
    ImageButton imageButtonFanOff;
    ImageButton imageButtonFanStop;

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

    MyHandler handler = new MyHandler(TAG) {

        AlertDialog dialogCloud = null;
        AlertDialog dialogLocal = null;

        //TODO 处理消息
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
                    getDeviceState = true;
                    new GetDeviceStateThread().start();
                    break;
                case ManageApplication.MESSAGE_SERVER_DISCONNECTED:
                    if (null == dialogCloud) {
                        dialogCloud = new AlertDialog.Builder(WorkMainActivity.this).setTitle("系统提示")//设置对话框标题

                                .setMessage("云端连接异常")//设置显示的内容

                                .setCancelable(false).create();
                        dialogCloud.show();
                    }
                    getDeviceState = false;
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

        Log.i(TAG, "init start");

        //显示时间的控件
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        Log.i(TAG, "time init done");

        //关闭按钮
        findViewById(R.id.imageButtonCancel).setOnClickListener(this);

        //暂停按钮
        findViewById(R.id.buttonPause).setOnClickListener(this);

        //接下来一堆控制按钮
        imageButtonMainBoxCtrlUP = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlUp);
        imageButtonMainBoxCtrlUP.setTag(false);
        imageButtonMainBoxCtrlUP.setOnClickListener(this);

        imageButtonMainBoxCtrlDown = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlDown);
        imageButtonMainBoxCtrlDown.setTag(false);
        imageButtonMainBoxCtrlDown.setOnClickListener(this);

        imageButtonIgniteMain = (ImageButton) findViewById(R.id.imageButtonIgniteMain);
        imageButtonIgniteMain.setTag(1);
        imageButtonIgniteMain.setOnClickListener(this);

        imageButtonIgniteBackup = (ImageButton) findViewById(R.id.imageButtonIgniteBackup);
        imageButtonIgniteBackup.setTag(1);
        imageButtonIgniteBackup.setOnClickListener(this);

        imageButtonHeatFront = (ImageButton) findViewById(R.id.imageButtonHeatFront);
        imageButtonHeatFront.setTag(1);
        imageButtonHeatFront.setOnClickListener(this);

        imageButtonHeatBack = (ImageButton) findViewById(R.id.imageButtonHeatBack);
        imageButtonHeatBack.setTag(1);
        imageButtonHeatBack.setOnClickListener(this);

        imageButtonFanOn = (ImageButton) findViewById(R.id.imageButtonFanOn);
        imageButtonFanOn.setOnClickListener(this);

        imageButtonFanOff = (ImageButton) findViewById(R.id.imageButtonFanOff);
        imageButtonFanOff.setOnClickListener(this);

        imageButtonFanStop = (ImageButton) findViewById(R.id.imageButtonFanStop);
        imageButtonFanStop.setOnClickListener(this);

        Log.i(TAG, "button init done");

        //信息显示面板
        textViewStoreID = (TextView) findViewById(R.id.textViewStoreID);
        textViewStoreName = (TextView) findViewById(R.id.textViewStoreName);
        textViewStoreName.setText(ManageApplication.getInstance().storeName);
        textViewBedID = (TextView) findViewById(R.id.textViewBedID);

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

        //开始周期性获取艾灸机数据
        new GetDeviceStateThread().start();

    }

    private class GetDeviceStateThread extends Thread {
        @Override
        public void run() {
            while (getDeviceState) {
                getOnes();
                SystemClock.sleep(DEVICE_STATE_DELAY);
            }
        }

        private void getOnes() {
            try {
                JSONObject jsonObjectDeviceState = ((ManageApplication) getApplication()).getCloudManage().getDeviceState();
                if (null != jsonObjectDeviceState) {
                    if (jsonObjectDeviceState.getInt("errorCode") == 0) {
                        JSONObject jsonData = jsonObjectDeviceState.getJSONObject("data");
                        if (jsonData.getInt("stateNetBoard") == 1) {
                            Message message = new Message();
                            message.what = ManageApplication.MESSAGE_DEVICE_STATE;
                            message.obj = jsonObjectDeviceState;
                            handler.sendMessage(message);
                        } else if (jsonData.getInt("stateNetBoard") == 0) {
                            Message message = new Message();
                            message.what = ManageApplication.MESSAGE_DEVICE_DISCONNECTED;
                            handler.sendMessage(message);
                        }
                    } else {
                        Toast.makeText(WorkMainActivity.this, jsonObjectDeviceState.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateDeviceState(JSONObject jsonObject) {
        //TODO 根据服务器返回的json更新UI界面

        try {
            JSONObject jsonData = jsonObject.getJSONObject("data");

            int tmp;

            tmp = jsonData.getInt("degreeBack");
            textViewBedTemperatureBack.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("degreeFore");
            textViewBedTemperatureFront.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("posMainMotor");
            textViewMainBoxPosition.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("stateDianBackLeft");
            boolean ignite3;
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                ignite3 = false;

            } else {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                ignite3 = true;
            }

            tmp = jsonData.getInt("stateDianBackRight");
            boolean ignite4;
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                ignite4 = false;
            } else {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                ignite4 = true;
            }

            tmp = jsonData.getInt("stateDianForeLeft");
            boolean ignite1;
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                ignite1 = false;
            } else {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                ignite1 = true;
            }

            tmp = jsonData.getInt("stateDianForeRight");
            boolean ignite2;
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                ignite2 = false;
            } else {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                ignite2 = true;
            }

            if (ignite1 || ignite3) {
                imageButtonIgniteMain.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteMain.setTag(0);
            } else {
                imageButtonIgniteMain.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteMain.setTag(1);
            }

            if (ignite2 || ignite4) {
                imageButtonIgniteBackup.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteBackup.setTag(0);
            } else {
                imageButtonIgniteBackup.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteBackup.setTag(1);
            }



            tmp = jsonData.getInt("stateHotBackLeft");
            boolean heat3;
            if (0 == tmp) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                heat3 = false;
            } else {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                heat3 = true;
            }

            tmp = jsonData.getInt("stateHotBackRight");
            boolean heat4;
            if (0 == tmp) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                heat4 = false;
            } else {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                heat4 = true;
            }

            tmp = jsonData.getInt("stateHotForeLeft");
            boolean heat1;
            if (0 == tmp) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                heat1 = false;
            } else {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                heat1 = true;
            }

            tmp = jsonData.getInt("stateHotForeRight");
            boolean heat2;
            if (0 == tmp) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                heat2 = false;
            } else {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                heat2 = true;
            }

            if (heat1 || heat2) {
                imageButtonHeatFront.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatFront.setTag(0);
            } else {
                imageButtonHeatFront.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatFront.setTag(1);
            }
            if (heat3 || heat4) {
                imageButtonHeatBack.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatBack.setTag(0);
            } else {
                imageButtonHeatBack.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatBack.setTag(1);
            }


            tmp = jsonData.getInt("currentTime") - jsonData.getInt("startTime");
            textViewWorkTimeMin.setText(String.valueOf(tmp / 60));
            textViewWorkTimeSec.setText(String.valueOf(tmp % 60));

            tmp = jsonData.getInt("stateWind");
            if (0 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_pressed);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_released);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_released);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lighton);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_released);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_pressed);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_released);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lighton);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lightoff);
            } else if (2 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_released);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_released);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_pressed);

                imageViewFanWorkStateStop.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOn.setImageResource(R.drawable.pic_view_lightoff);
                imageViewFanWorkStateOff.setImageResource(R.drawable.pic_view_lighton);
            }

            tmp = jsonData.getInt("stateMainMotor");
            if (0 == tmp) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_mainbox_up_released);
                imageButtonMainBoxCtrlUP.setTag(false);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_mainbox_up_released);
                imageButtonMainBoxCtrlDown.setTag(false);
            } else if (1 == tmp) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_mainbox_up_pressed);
                imageButtonMainBoxCtrlUP.setTag(true);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_mainbox_up_released);
                imageButtonMainBoxCtrlDown.setTag(false);
            } else if (2 == tmp) {
                imageButtonMainBoxCtrlUP.setImageResource(R.drawable.pic_button_mainbox_up_released);
                imageButtonMainBoxCtrlUP.setTag(false);
                imageButtonMainBoxCtrlDown.setImageResource(R.drawable.pic_button_mainbox_up_pressed);
                imageButtonMainBoxCtrlDown.setTag(true);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //// TODO: 2017/1/2  
            case R.id.buttonPause:
                JSONObject jsonObject = ManageApplication.getInstance().getCloudManage().devicePause();
                if (null != jsonObject) {
                    try {
                        if (jsonObject.getInt("errorCode") != 0) {
                            Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.imageButtonCancel:
                finish();
                break;
            case R.id.imageButtonMainBoxCtrlUp:
                if ((boolean) view.getTag()) {
                    ManageApplication.getInstance().getCloudManage().bedControl("MAINMOTOR", 0);
                } else {
                    ManageApplication.getInstance().getCloudManage().bedControl("MAINMOTOR", 1);
                }
                break;
            case R.id.imageButtonMainBoxCtrlDown:
                if ((boolean) view.getTag()) {
                    ManageApplication.getInstance().getCloudManage().bedControl("MAINMOTOR", 0);
                } else {
                    ManageApplication.getInstance().getCloudManage().bedControl("MAINMOTOR", 2);
                }
                break;
            case R.id.imageButtonIgniteMain:
                ManageApplication.getInstance().getCloudManage().bedControl("FIRE_Main", (int) view.getTag());
                break;
            case R.id.imageButtonIgniteBackup:
                ManageApplication.getInstance().getCloudManage().bedControl("FIRE_TMP", (int) view.getTag());
                break;
            case R.id.imageButtonHeatFront:
                ManageApplication.getInstance().getCloudManage().bedControl("HOT_PREV", (int) view.getTag());
                break;
            case R.id.imageButtonHeatBack:
                ManageApplication.getInstance().getCloudManage().bedControl("HOT_NEXT", (int) view.getTag());
                break;

            case R.id.imageButtonFanOn:
                ManageApplication.getInstance().getCloudManage().bedControl("WIND", 1);
                break;
            case R.id.imageButtonFanOff:
                ManageApplication.getInstance().getCloudManage().bedControl("WIND", 2);
                break;
            case R.id.imageButtonFanStop:
                ManageApplication.getInstance().getCloudManage().bedControl("WIND", 0);
                break;
            default:
                break;
        }
        new GetDeviceStateThread().getOnes();
    }

    @Override
    public void finish() {
        //TODO 退出时干嘛？
        ManageApplication.getInstance().removeCurrentHandler();
        super.finish();
    }

    @Override
    protected void onPause() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
        getDeviceState = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ManageApplication.getInstance().setCurrentActivityHandler(handler);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                }, 1000);
            }
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getDeviceState = true;
        new GetDeviceStateThread().start();
    }

    @Override
    protected void onDestroy() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
        getDeviceState = false;
        super.onDestroy();
    }
}
