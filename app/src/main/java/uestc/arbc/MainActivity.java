package uestc.arbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
import android.util.DisplayMetrics;
import android.view.Display;
*/
import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.DataSQL;
import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;


public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private TextView textViewTime;
    private Button buttonStart;
    private TextView textViewCloudConnect;
    private TextView textViewLocalConnect;


    private long tmpTime = 0L;//记录上一次按下退出键的时间，实现按两次退出键才退出程序的功能
    private MyHandler handler = new MyHandler(TAG) {

        private boolean isServerConnected = false;
        private boolean isMachineConnected = false;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ManageApplication.MESSAGE_TIME:
                    textViewTime.setText(msg.obj.toString());
                    break;
                case ManageApplication.MESSAGE_SERVER_CONNECTED:
                    if (!isServerConnected) {
                        textViewCloudConnect.setText(getString(R.string.cloud_connect_successful));
                        isServerConnected = true;
                        if (isMachineConnected) {
                            buttonStart.setEnabled(true);
                        }
                    }
                    getMainInfo();
                    break;
                case ManageApplication.MESSAGE_SERVER_DISCONNECTED:
                    if (isServerConnected) {
                        textViewCloudConnect.setText(getString(R.string.cloud_connect_failed));
                        isServerConnected = false;
                        buttonStart.setEnabled(false);
                    }
                    break;
                case ManageApplication.MESSAGE_MACHINE_CONNECTED:
                    if (!isMachineConnected) {
                        textViewLocalConnect.setText(getString(R.string.local_connect_successful));
                        isMachineConnected = true;
                        if (isServerConnected) {
                            buttonStart.setEnabled(true);
                        }
                    }
                    break;
                case ManageApplication.MESSAGE_MACHINE_DISCONNECTED:
                    if (isMachineConnected) {
                        textViewLocalConnect.setText(getString(R.string.local_connect_failed));
                        isMachineConnected = false;
                        buttonStart.setEnabled(false);
                        break;
                    }
                default:
                    break;
            }
        }
    };

    private void getMainInfo() {
        //TODO
        JSONObject jsonObjectMainInfo = ManageApplication.getInstance().getCloudManage().getMainInfo();
        JSONObject data;
        if (null == jsonObjectMainInfo) {
            Log.i(TAG,"getMainInfo failed:return null");
            return;
        }
        try {
            if (jsonObjectMainInfo.getInt("errorCode") == -1)  {
                Toast.makeText(this,jsonObjectMainInfo.getString("message"),Toast.LENGTH_LONG).show();
            } else if (jsonObjectMainInfo.getInt("errorCode") == 0) {
                data = jsonObjectMainInfo.optJSONObject("data");
                if (null != data) {
                    Message msg = new Message();
                    if (data.getInt("boardConnect") == 0) {
                        msg.what = ManageApplication.MESSAGE_MACHINE_CONNECTED;
                    } else {
                        msg.what = ManageApplication.MESSAGE_MACHINE_DISCONNECTED;
                    }
                    handler.sendMessage(msg);

                } else {
                    Log.i(TAG,"getMainInfo failed:data null");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
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

        init();//初始化

    }

    private void init() {
        //为“启动/start”按钮设置按下行为
        buttonStart = (Button) findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        textViewTime = ((TextView) findViewById(R.id.textViewTime));

        textViewCloudConnect = ((TextView) findViewById(R.id.textViewCloudConnect));

        textViewLocalConnect = ((TextView) findViewById(R.id.textViewLocalConnect));

        //发送handler
        ((ManageApplication) getApplication()).setCurrentActivityHandler(handler);

        //如果DeviceId不存在则需要登录
        DataSQL dataSQL = ((ManageApplication) getApplication()).getDataSQL();
        if (null == dataSQL) {
            new AlertDialog.Builder(MainActivity.this).setTitle("系统提示")//设置对话框标题

                    .setMessage("数据库初始化失败，应用即将退出！")//设置显示的内容

                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮

                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            finish();
                        }
                    }).show();
            finish();
            return;
        }
        if (!dataSQL.isTableExists("deviceInfo")) {
            Intent intent = new Intent();
            Log.i(TAG,"deviceInfo is not exist");
            intent.setClass(this,LoginActivity.class);
            intent.putExtra("RequestCode",ManageApplication.REQUEST_CODE_DEVICE_SIGN);
            startActivityForResult(intent,ManageApplication.REQUEST_CODE_DEVICE_SIGN);
        } else {
            JSONObject jsonObject = dataSQL.getJson("deviceInfo");
            int deviceID = 0;
            try {
                deviceID = jsonObject.getInt("deviceID");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ManageApplication.getInstance().getCloudManage().setDeviceID(deviceID);
        }
    }

    @Override
    protected void onPause() {
        ManageApplication.getInstance().removeCurrentHandler();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long now = System.currentTimeMillis();
            if (now - tmpTime > 1000) {
                Toast.makeText(getApplicationContext(), this.getString(R.string.toast_quit), Toast.LENGTH_SHORT).show();
                tmpTime = now;
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //主动退出时需要做的事
    @Override
    public void finish() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
        ((ManageApplication) getApplication()).close();
        super.finish();
    }

    @Override
    protected void onDestroy() {
        ((ManageApplication) getApplication()).removeCurrentHandler();
        super.onDestroy();
    }

    //"启动/start"被按下时
    public void start() {
        Intent intent = new Intent();
        JSONObject jsonObject = ManageApplication.getInstance().getCloudManage().mainStart();
        if (null == jsonObject) {
            Toast.makeText(this,"通信失败",Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (jsonObject.getInt("errorCode") == -1) {
                Toast.makeText(this,jsonObject.getString("message"),Toast.LENGTH_SHORT).show();
                return;
            } else if (jsonObject.getInt("errorCode") == 0) {
                JSONObject data = jsonObject.optJSONObject("data");
                if (null == data) {
                    Toast.makeText(this,"数据错误",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (data.getInt("state") == 0) {
                    intent.setClass(this,LoginActivity.class);
                    intent.putExtra("RequestCode",ManageApplication.REQUEST_CODE_USER_LOGIN);
                } else if (data.getInt("state") == 1) {
                    intent.setClass(this,WorkMainActivity.class);
                } else {
                    Toast.makeText(this,"数据错误",Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.i(TAG,"start failed:json error");
        }

        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case ManageApplication.RESULT_CODE_FAILED:
                finish();
                break;
            case ManageApplication.RESULT_CODE_SUCCEED:
                Message msg = new Message();
                if (ManageApplication.getInstance().getCloudManage().isMachineConnected()) {
                    msg.what = ManageApplication.MESSAGE_MACHINE_CONNECTED;
                } else {
                    msg.what = ManageApplication.MESSAGE_MACHINE_DISCONNECTED;
                }
                handler.sendMessage(msg);
                break;
            default:
                break;
        }
    }

}
