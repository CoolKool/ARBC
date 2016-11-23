package uestc.arbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
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

    TextView textViewTime;


    private boolean getDeviceState = true;
    private final static long DEVICE_STATE_DELAY = 5000;//每5秒获取一次艾灸机信息

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
                case ManageApplication.MESSAGE_NETWORK_BAD:
                    Toast.makeText(WorkMainActivity.this,getString(R.string.message_network_not_available),Toast.LENGTH_SHORT).show();
                    break;
                case ManageApplication.MESSAGE_SERVER_CONNECTED:
                    if (null != dialogCloud) {
                        dialogCloud.dismiss();
                    }
                    break;
                case ManageApplication.MESSAGE_SERVER_DISCONNECTED:
                    dialogCloud = new AlertDialog.Builder(WorkMainActivity.this).setTitle("系统提示")//设置对话框标题

                            .setMessage("云端连接异常")//设置显示的内容

                            .setCancelable(false).create();
                    dialogCloud.show();
                    break;
                case ManageApplication.MESSAGE_MACHINE_CONNECTED:
                    if (null != dialogLocal) {
                        dialogLocal.dismiss();
                    }
                    break;
                case ManageApplication.MESSAGE_MACHINE_DISCONNECTED:
                    dialogLocal = new AlertDialog.Builder(WorkMainActivity.this).setTitle("系统提示")//设置对话框标题

                            .setMessage("云端连接异常")//设置显示的内容

                            .setCancelable(false).create();
                    dialogLocal.show();
                    break;
                case ManageApplication.MESSAGE_MACHINE_STATE:
                    JSONObject json = null;
                    try {
                        json =  new JSONObject(msg.obj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i(TAG,"MESSAGE_MACHINE_STATE json error");
                    }
                    if (null != json) {
                        updateDeviceState(json);
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
        findViewById(R.id.imageButtonCancel).setOnClickListener(this);;

        //暂停按钮
        findViewById(R.id.buttonPause).setOnClickListener(this);

        //接下来一堆控制按钮
        findViewById(R.id.imageButtonMainBoxCtrlUp).setOnClickListener(this);

        findViewById(R.id.imageButtonMainBoxCtrlDown).setOnClickListener(this);

        findViewById(R.id.imageButtonBackBoxCtrlUp).setOnClickListener(this);

        findViewById(R.id.imageButtonBackBoxCtrlDown).setOnClickListener(this);

        findViewById(R.id.imageButtonHeatFL).setOnClickListener(this);

        findViewById(R.id.imageButtonHeatFR).setOnClickListener(this);

        findViewById(R.id.imageButtonHeatBL).setOnClickListener(this);

        findViewById(R.id.imageButtonHeatBR).setOnClickListener(this);

        Log.i(TAG,"button init done");

        //传递handler给ManageApplication
        ManageApplication.getInstance().setCurrentActivityHandler(handler);
        //开始周期性获取艾灸机数据
        //TODO new GetDeviceStateThread().start();

    }

    class GetDeviceStateThread extends Thread {
        @Override
        public void run() {
            JSONObject jsonObjectDeviceState;
            while (getDeviceState) {
                jsonObjectDeviceState = ((ManageApplication) getApplication()).getCloudManage().getMachineState();
                if (null != jsonObjectDeviceState) {
                    Message message = new Message();
                    message.what = ManageApplication.MESSAGE_MACHINE_STATE;
                    message.obj = jsonObjectDeviceState.toString();
                    handler.sendMessage(message);
                }
                SystemClock.sleep(DEVICE_STATE_DELAY);
            }
        }
    }

    private void updateDeviceState(JSONObject jsonObject) {
        //TODO 根据服务器返回的json更新UI界面

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonPause:
                ((ManageApplication) getApplication()).getCloudManage().devicePause();
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
            case R.id.imageButtonBackBoxCtrlUp:
                ((ManageApplication) getApplication()).getCloudManage().backBoxCtrlUp();
                break;
            case R.id.imageButtonBackBoxCtrlDown:
                ((ManageApplication) getApplication()).getCloudManage().backBoxCtrlDown();
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
