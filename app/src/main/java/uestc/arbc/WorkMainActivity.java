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
    ImageButton imageButtonBackBoxFU;
    ImageButton imageButtonBackBoxFD;
    ImageButton imageButtonBackBoxBU;
    ImageButton imageButtonBackBoxBD;
    ImageButton imageButtonHeatFL;
    ImageButton imageButtonHeatFR;
    ImageButton imageButtonHeatBL;
    ImageButton imageButtonHeatBR;

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
    TextView textViewMainBoxPosition;
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
                    //implements in ManageApplication.MESSAGE_DEVICE_STATE:
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
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                },1000);
            }
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        init();

    }

    private void init() {

        Log.i(TAG,"init start");

        //显示时间的控件
        textViewTime = (TextView) findViewById(R.id.textViewTime);
        Log.i(TAG,"time init done");

        //关闭按钮
        findViewById(R.id.imageButtonCancel).setOnClickListener(this);

        //暂停按钮
        findViewById(R.id.buttonPause).setOnClickListener(this);

        //接下来一堆控制按钮
        imageButtonMainBoxCtrlUP = (ImageButton) findViewById(R.id.imageButtonMainBoxCtrlUp);
        imageButtonMainBoxCtrlUP.setOnClickListener(this);
        imageButtonMainBoxCtrlUP.setOnTouchListener(mainBoxOnTouchListener);

        imageButtonMainBoxCtrlDown = (ImageButton)findViewById(R.id.imageButtonMainBoxCtrlDown);
        imageButtonMainBoxCtrlDown.setOnClickListener(this);
        imageButtonMainBoxCtrlDown.setOnTouchListener(mainBoxOnTouchListener);

        imageButtonBackBoxFU = (ImageButton)findViewById(R.id.imageButtonBackBoxFU);
        imageButtonBackBoxFU.setOnClickListener(this);
        imageButtonBackBoxFU.setOnTouchListener(backBoxOnTouchListener);

        imageButtonBackBoxFD = (ImageButton)findViewById(R.id.imageButtonBackBoxFD);
        imageButtonBackBoxFD.setOnClickListener(this);
        imageButtonBackBoxFD.setOnTouchListener(backBoxOnTouchListener);

        imageButtonBackBoxBU = (ImageButton)findViewById(R.id.imageButtonBackBoxBU);
        imageButtonBackBoxBU.setOnClickListener(this);
        imageButtonBackBoxBU.setOnTouchListener(backBoxOnTouchListener);

        imageButtonBackBoxBD = (ImageButton)findViewById(R.id.imageButtonBackBoxBD);
        imageButtonBackBoxBD.setOnClickListener(this);
        imageButtonBackBoxBD.setOnTouchListener(backBoxOnTouchListener);

        imageButtonHeatFL = (ImageButton)findViewById(R.id.imageButtonHeatFL);
        imageButtonHeatFL.setOnClickListener(this);

        imageButtonHeatFR = (ImageButton)findViewById(R.id.imageButtonHeatFR);
        imageButtonHeatFR.setOnClickListener(this);

        imageButtonHeatBL = (ImageButton)findViewById(R.id.imageButtonHeatBL);
        imageButtonHeatBL.setOnClickListener(this);

        imageButtonHeatBR = (ImageButton)findViewById(R.id.imageButtonHeatBR);
        imageButtonHeatBR.setOnClickListener(this);

        Log.i(TAG,"button init done");

        //信息显示面板
        textViewStoreID = (TextView)findViewById(R.id.textViewStoreID);
        textViewStoreName = (TextView)findViewById(R.id.textViewStoreName);
        try {
            textViewStoreName.setText(ManageApplication.getInstance().getDataSQL().getJson(ManageApplication.TABLE_NAME_DEVICE_INFO).getString("storeName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        textViewBedID = (TextView)findViewById(R.id.textViewBedID);
        textViewWorkTimeMin = (TextView)findViewById(R.id.textViewWorkTimeMin);
        textViewWorkTimeSec = (TextView)findViewById(R.id.textViewWorkTimeSec);
        textViewTemperatureFL = (TextView)findViewById(R.id.textViewTemperatureFL);
        textViewTemperatureFR = (TextView)findViewById(R.id.textViewTemperatureFR);
        textViewTemperatureBL = (TextView)findViewById(R.id.textViewTemperatureBL);
        textViewTemperatureBR = (TextView)findViewById(R.id.textViewTemperatureBR);
        textViewHumidityFront = (TextView)findViewById(R.id.textViewHumidityFront);
        textViewHumidityBack = (TextView)findViewById(R.id.textViewHumidityBack);
        imageViewHeatBoardWorkStateFL = (ImageView)findViewById(R.id.imageViewHeatBoardWorkStateFL);
        imageViewHeatBoardWorkStateFR = (ImageView)findViewById(R.id.imageViewHeatBoardWorkStateFR);
        imageViewHeatBoardWorkStateBL = (ImageView)findViewById(R.id.imageViewHeatBoardWorkStateBL);
        imageViewHeatBoardWorkStateBR = (ImageView)findViewById(R.id.imageViewHeatBoardWorkStateBR);
        imageViewIgniteBoardWorkStateFL = (ImageView)findViewById(R.id.imageViewIgniteBoardWorkStateFL);
        imageViewIgniteBoardWorkStateFR = (ImageView)findViewById(R.id.imageViewIgniteBoardWorkStateFR);
        imageViewIgniteBoardWorkStateBL = (ImageView)findViewById(R.id.imageViewIgniteBoardWorkStateBL);
        imageViewIgniteBoardWorkStateBR = (ImageView)findViewById(R.id.imageViewIgniteBoardWorkStateBR);
        textViewMainBoxPosition = (TextView)findViewById(R.id.textViewMainBoxPosition);
        textViewHeadBoxState = (TextView)findViewById(R.id.textViewHeadBoxState);
        textViewTailBoxState = (TextView)findViewById(R.id.textViewTailBoxState);

        //传递handler给ManageApplication
        ManageApplication.getInstance().setCurrentActivityHandler(handler);
        //开始周期性获取艾灸机数据
        new GetDeviceStateThread().start();

    }

    class GetDeviceStateThread extends Thread {
        @Override
        public void run() {
            try {
                JSONObject jsonObjectDeviceState;
                while (getDeviceState) {
                    jsonObjectDeviceState = ((ManageApplication) getApplication()).getCloudManage().getDeviceState();
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
                            Toast.makeText(WorkMainActivity.this,jsonObjectDeviceState.getString("message"),Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SystemClock.sleep(DEVICE_STATE_DELAY);
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
            if ( 0 == tmp) {
               imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
            }

            tmp = jsonData.getInt("stateDianBackRight");
            if ( 0 == tmp) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
            }

            tmp = jsonData.getInt("stateDianForeLeft");
            if ( 0 == tmp) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
            }

            tmp = jsonData.getInt("stateDianForeRight");
            if ( 0 == tmp) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
            } else if (1 == tmp) {
                imageViewIgniteBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
            }

            tmp = jsonData.getInt("stateHotBackLeft");
            if ( 0 == tmp) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatBL.setImageResource(R.drawable.pic_button_leftup_released);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateBL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatBL.setImageResource(R.drawable.pic_button_leftup_pressed);
            }

            tmp = jsonData.getInt("stateHotBackRight");
            if ( 0 == tmp) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatBR.setImageResource(R.drawable.pic_button_leftup_released);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateBR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatBR.setImageResource(R.drawable.pic_button_leftup_pressed);
            }

            tmp = jsonData.getInt("stateHotForeLeft");
            if ( 0 == tmp) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatFL.setImageResource(R.drawable.pic_button_leftup_released);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateFL.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatFL.setImageResource(R.drawable.pic_button_leftup_pressed);
            }

            tmp = jsonData.getInt("stateHotForeRight");
            if ( 0 == tmp) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lightoff);
                imageButtonHeatFR.setImageResource(R.drawable.pic_button_leftup_released);
            } else if (1 == tmp) {
                imageViewHeatBoardWorkStateFR.setImageResource(R.drawable.pic_view_lighton);
                imageButtonHeatFR.setImageResource(R.drawable.pic_button_leftup_pressed);
            }

            tmp = jsonData.getInt("currentTime") - jsonData.getInt("startTime");
            textViewWorkTimeMin.setText(String.valueOf(tmp/60));
            textViewWorkTimeSec.setText(String.valueOf(tmp%60));

            tmp = jsonData.getInt("posMainMotor");
            if (1 == tmp) {
                textViewMainBoxPosition.setText("中间");
            } else if (2 == tmp) {
                textViewMainBoxPosition.setText("顶部");
            } else if (3 == tmp) {
                textViewMainBoxPosition.setText("下部");
            }

            tmp = jsonData.getInt("posForeMotor");
            if (1 == tmp) {
                textViewHeadBoxState.setText("中间");
            } else if (2 == tmp) {
                textViewHeadBoxState.setText("上翘");
            } else if (3 == tmp) {
                textViewHeadBoxState.setText("下翘");
            }

            tmp = jsonData.getInt("posBackMotor");
            if (1 == tmp) {
                textViewTailBoxState.setText("中间");
            } else if (2 == tmp) {
                textViewTailBoxState.setText("上翘");
            } else if (3 == tmp) {
                textViewTailBoxState.setText("下翘");
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
                ((ManageApplication) getApplication()).getCloudManage().setDeviceWorkState(1);
                break;
            case R.id.imageButtonCancel:
                finish();
                break;
            case R.id.imageButtonMainBoxCtrlUp:
                ((ManageApplication) getApplication()).getCloudManage().mainBoxCtrlUp();
                break;
            case R.id.imageButtonMainBoxCtrlDown:
                ((ManageApplication) getApplication()).getCloudManage().mainBoxCtrlDown();
                break;
            case R.id.imageButtonBackBoxFU:
                ((ManageApplication) getApplication()).getCloudManage().backBoxCtrlFU();
                break;
            case R.id.imageButtonBackBoxFD:
                ((ManageApplication) getApplication()).getCloudManage().backBoxCtrlFD();
                break;
            case R.id.imageButtonBackBoxBU:

                break;
            case R.id.imageButtonBackBoxBD:

                break;
            case R.id.imageButtonHeatFL:
                ((ManageApplication) getApplication()).getCloudManage().heatBoardCtrl("FL");
                break;
            case R.id.imageButtonHeatFR:
                ((ManageApplication) getApplication()).getCloudManage().heatBoardCtrl("FR");
                break;
            case R.id.imageButtonHeatBL:
                ((ManageApplication) getApplication()).getCloudManage().heatBoardCtrl("BL");
                break;
            case R.id.imageButtonHeatBR:
                ((ManageApplication) getApplication()).getCloudManage().heatBoardCtrl("BR");
                break;
            default:
                break;
        }
    }

    View.OnTouchListener mainBoxOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageButton)v).setImageResource(R.drawable.pic_button_mainbox_up_pressed);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                ((ImageButton)v).setImageResource(R.drawable.pic_button_mainbox_up_released);
            }

            return false;
        }
    };

    View.OnTouchListener backBoxOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ((ImageButton)v).setImageResource(R.drawable.pic_button_backbox_up_pressed);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                ((ImageButton)v).setImageResource(R.drawable.pic_button_backbox_up_released);
            }

            return false;
        }
    };

    @Override
    public void finish() {
        //TODO 退出时干嘛？
        ManageApplication.getInstance().removeCurrentHandler();
        super.finish();
    }

    @Override
    protected void onPause() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
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
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION| View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                    }
                },1000);
            }
        });
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onDestroy() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
        getDeviceState = false;
        super.onDestroy();
    }
}
