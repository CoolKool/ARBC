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

    ImageButton imageButtonMainBoxCtrlUP;
    ImageButton imageButtonMainBoxCtrlDown;

    ImageButton imageButtonHeatFL;
    ImageButton imageButtonHeatFR;
    ImageButton imageButtonHeatBL;
    ImageButton imageButtonHeatBR;
    ImageButton imageButtonIgniteFL;
    ImageButton imageButtonIgniteFR;
    ImageButton imageButtonIgniteBL;
    ImageButton imageButtonIgniteBR;
    ImageButton imageButtonFanOn;
    ImageButton imageButtonFanOff;
    ImageButton imageButtonFanStop;

    TextView textViewStoreID;
    TextView textViewStoreName;
    TextView textViewBedID;
    TextView textViewWorkTimeMin;
    TextView textViewWorkTimeSec;
    TextView textViewTemperatureFL;
    TextView textViewTemperatureFR;
    TextView textViewTemperatureBL;
    TextView textViewTemperatureBR;
    TextView textViewHumidityFront;
    TextView textViewHumidityBack;
    ImageView imageViewHeatBoardWorkStateFL;
    ImageView imageViewHeatBoardWorkStateFR;
    ImageView imageViewHeatBoardWorkStateBL;
    ImageView imageViewHeatBoardWorkStateBR;
    ImageView imageViewIgniteBoardWorkStateFL;
    ImageView imageViewIgniteBoardWorkStateFR;
    ImageView imageViewIgniteBoardWorkStateBL;
    ImageView imageViewIgniteBoardWorkStateBR;
    TextView textViewHeadBoxState;
    TextView textViewTailBoxState;
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

        imageButtonIgniteFL = (ImageButton) findViewById(R.id.imageButtonIgniteFL);
        imageButtonIgniteFL.setTag(1);
        imageButtonIgniteFL.setOnClickListener(this);

        imageButtonIgniteFR = (ImageButton) findViewById(R.id.imageButtonIgniteFR);
        imageButtonIgniteFR.setTag(1);
        imageButtonIgniteFR.setOnClickListener(this);

        imageButtonIgniteBL = (ImageButton) findViewById(R.id.imageButtonIgniteBL);
        imageButtonIgniteBL.setTag(1);
        imageButtonIgniteBL.setOnClickListener(this);

        imageButtonIgniteBR = (ImageButton) findViewById(R.id.imageButtonIgniteBR);
        imageButtonIgniteBR.setTag(1);
        imageButtonIgniteBR.setOnClickListener(this);

        imageButtonHeatFL = (ImageButton) findViewById(R.id.imageButtonHeatFL);
        imageButtonHeatFL.setTag(1);
        imageButtonHeatFL.setOnClickListener(this);

        imageButtonHeatFR = (ImageButton) findViewById(R.id.imageButtonHeatFR);
        imageButtonHeatFR.setTag(1);
        imageButtonHeatFR.setOnClickListener(this);

        imageButtonHeatBL = (ImageButton) findViewById(R.id.imageButtonHeatBL);
        imageButtonHeatBL.setTag(1);
        imageButtonHeatBL.setOnClickListener(this);

        imageButtonHeatBR = (ImageButton) findViewById(R.id.imageButtonHeatBR);
        imageButtonHeatBR.setTag(1);
        imageButtonHeatBR.setOnClickListener(this);

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
        textViewWorkTimeMin = (TextView) findViewById(R.id.textViewWorkTimeMin);
        textViewWorkTimeSec = (TextView) findViewById(R.id.textViewWorkTimeSec);
        textViewTemperatureFL = (TextView) findViewById(R.id.textViewTemperatureFL);
        textViewTemperatureFR = (TextView) findViewById(R.id.textViewTemperatureFR);
        textViewTemperatureBL = (TextView) findViewById(R.id.textViewTemperatureBL);
        textViewTemperatureBR = (TextView) findViewById(R.id.textViewTemperatureBR);
        textViewHumidityFront = (TextView) findViewById(R.id.textViewHumidityFront);
        textViewHumidityBack = (TextView) findViewById(R.id.textViewHumidityBack);
        imageViewHeatBoardWorkStateFL = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateFL);
        imageViewHeatBoardWorkStateFR = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateFR);
        imageViewHeatBoardWorkStateBL = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateBL);
        imageViewHeatBoardWorkStateBR = (ImageView) findViewById(R.id.imageViewHeatBoardWorkStateBR);
        imageViewIgniteBoardWorkStateFL = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateFL);
        imageViewIgniteBoardWorkStateFR = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateFR);
        imageViewIgniteBoardWorkStateBL = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateBL);
        imageViewIgniteBoardWorkStateBR = (ImageView) findViewById(R.id.imageViewIgniteBoardWorkStateBR);
        textViewHeadBoxState = (TextView) findViewById(R.id.textViewHeadBoxState);
        textViewTailBoxState = (TextView) findViewById(R.id.textViewTailBoxState);

        //开始周期性获取艾灸机数据
        new GetDeviceStateThread().start();

    }

    class GetDeviceStateThread extends Thread {
        @Override
        public void run() {
            while (getDeviceState) {
                getOnes();
                SystemClock.sleep(DEVICE_STATE_DELAY);
            }
        }

        public void getOnes() {
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

            tmp = jsonData.getInt("degreeBackLeft");
            textViewTemperatureBL.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("degreeBackRight");
            textViewTemperatureBR.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("degreeForeLeft");
            textViewTemperatureFL.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("degreeForeRight");
            textViewTemperatureFR.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("humidityBack");
            textViewHumidityBack.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("humidityFore");
            textViewHumidityFront.setText(String.valueOf(tmp));

            tmp = jsonData.getInt("stateDianBackLeft");
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonIgniteBL.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteBL.setTag(1);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonIgniteBL.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteBL.setTag(0);
            }

            tmp = jsonData.getInt("stateDianBackRight");
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonIgniteBR.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteBR.setTag(1);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonIgniteBR.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteBR.setTag(0);
            }

            tmp = jsonData.getInt("stateDianForeLeft");
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonIgniteFL.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteFL.setTag(1);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonIgniteFL.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteFL.setTag(0);
            }

            tmp = jsonData.getInt("stateDianForeRight");
            if (0 == tmp) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonIgniteFR.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonIgniteFR.setTag(1);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonIgniteFR.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonIgniteFR.setTag(0);
            }

            tmp = jsonData.getInt("stateHotBackLeft");
            if (0 == tmp) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatBL.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatBL.setTag(1);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatBL.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatBL.setTag(0);
            }

            tmp = jsonData.getInt("stateHotBackRight");
            if (0 == tmp) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatBR.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatBR.setTag(1);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatBR.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatBR.setTag(0);
            }

            tmp = jsonData.getInt("stateHotForeLeft");
            if (0 == tmp) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatFL.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatFL.setTag(1);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatFL.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatFL.setTag(0);
            }

            tmp = jsonData.getInt("stateHotForeRight");
            if (0 == tmp) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatFR.setImageResource(R.drawable.pic_button_leftup_released);
                imageButtonHeatFR.setTag(1);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatFR.setImageResource(R.drawable.pic_button_leftup_pressed);
                imageButtonHeatFR.setTag(0);
            }

            tmp = jsonData.getInt("currentTime") - jsonData.getInt("startTime");
            textViewWorkTimeMin.setText(String.valueOf(tmp / 60));
            textViewWorkTimeSec.setText(String.valueOf(tmp % 60));

            tmp = jsonData.getInt("stateWind");
            if (0 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_pressed);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_released);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_released);
            } else if (1 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_released);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_pressed);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_released);
            } else if (2 == tmp) {
                imageButtonFanStop.setImageResource(R.drawable.pic_button_stop_released);
                imageButtonFanOn.setImageResource(R.drawable.pic_button_on_released);
                imageButtonFanOff.setImageResource(R.drawable.pic_button_off_pressed);
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
                    ManageApplication.getInstance().getCloudManage().setMotor("MAINMOTOR", 0);
                } else {
                    ManageApplication.getInstance().getCloudManage().setMotor("MAINMOTOR", 1);
                }
                break;
            case R.id.imageButtonMainBoxCtrlDown:
                if ((boolean) view.getTag()) {
                    ManageApplication.getInstance().getCloudManage().setMotor("MAINMOTOR", 0);
                } else {
                    ManageApplication.getInstance().getCloudManage().setMotor("MAINMOTOR", 2);
                }
                break;
            case R.id.imageButtonIgniteFL:
                ManageApplication.getInstance().getCloudManage().setSwitch("FIRE_FL", (int) view.getTag());
                break;
            case R.id.imageButtonIgniteFR:
                ManageApplication.getInstance().getCloudManage().setSwitch("FIRE_FR", (int) view.getTag());
                break;
            case R.id.imageButtonIgniteBL:
                ManageApplication.getInstance().getCloudManage().setSwitch("FIRE_BL", (int) view.getTag());
                break;
            case R.id.imageButtonIgniteBR:
                ManageApplication.getInstance().getCloudManage().setSwitch("FIRE_BR", (int) view.getTag());
                break;
            case R.id.imageButtonHeatFL:
                ManageApplication.getInstance().getCloudManage().setSwitch("HOT_FL", (int) view.getTag());
                break;
            case R.id.imageButtonHeatFR:
                ManageApplication.getInstance().getCloudManage().setSwitch("HOT_FR", (int) view.getTag());
                break;
            case R.id.imageButtonHeatBL:
                ManageApplication.getInstance().getCloudManage().setSwitch("HOT_BL", (int) view.getTag());
                break;
            case R.id.imageButtonHeatBR:
                ManageApplication.getInstance().getCloudManage().setSwitch("HOT_BR", (int) view.getTag());
                break;
            case R.id.imageButtonFanOn:
                ManageApplication.getInstance().getCloudManage().setSwitch("WIND", 1);
                break;
            case R.id.imageButtonFanOff:
                ManageApplication.getInstance().getCloudManage().setSwitch("WIND", 2);
                break;
            case R.id.imageButtonFanStop:
                ManageApplication.getInstance().getCloudManage().setSwitch("WIND", 0);
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
