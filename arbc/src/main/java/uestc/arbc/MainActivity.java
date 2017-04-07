package uestc.arbc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
import android.util.DisplayMetrics;
import android.view.Display;
*/
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import uestc.arbc.background.BLEManage;
import uestc.arbc.background.DataSQL;
import uestc.arbc.background.Interface;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;


public class MainActivity extends Activity {

    private final static String TAG = "MainActivity";

    private TextView textViewTime;
    private Button buttonStart;
    ImageButton imageButtonDeviceInfo;
    ImageButton imageButtonManage;
    private TextView textViewCloudConnect;
    private TextView textViewLocalConnect;
    private TextView textViewSelectBed;

    AlertDialog alertDialogSelectBed = null;
    private List<JSONObject> bedList = new ArrayList<>();

    private long tmpTime = 0L;//记录上一次按下退出键的时间，实现按两次退出键才退出程序的功能
    private DataSQL dataSQL;

    BLEManage bleManage;

    private boolean isServerConnected = false;
    private boolean isDeviceConnected = false;
    private MyHandler handler = new MyHandler(TAG) {


        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ManageApplication.MESSAGE_TIME:
                    textViewTime.setText(msg.obj.toString());
                    break;
                case ManageApplication.MESSAGE_SERVER_CONNECTED:
                    textViewCloudConnect.setText(getString(R.string.cloud_connect_successful));
                    isServerConnected = true;
                    if (isDeviceConnected) {
                        buttonStart.setEnabled(true);
                    }

                    //如果DeviceId不存在则需要登录
                    if (null != dataSQL) {
                        if (!dataSQL.isTableExists(ManageApplication.TABLE_NAME_DEVICE_INFO)) {
                            Intent intent = new Intent();
                            L.i(TAG, "deviceInfo is not exist");
                            intent.setClass(MainActivity.this, LoginActivity.class);
                            intent.putExtra("RequestCode", ManageApplication.REQUEST_CODE_DEVICE_SIGN);
                            startActivityForResult(intent, ManageApplication.REQUEST_CODE_DEVICE_SIGN);
                        } else {
                            getMainInfo();
                        }
                    }
                    break;
                case ManageApplication.MESSAGE_SERVER_DISCONNECTED:
                    textViewCloudConnect.setText(getString(R.string.cloud_connect_failed));
                    isServerConnected = false;
                    buttonStart.setEnabled(false);
                    break;
                case ManageApplication.MESSAGE_DEVICE_CONNECTED:
                    textViewLocalConnect.setText(getString(R.string.local_connect_successful));
                    isDeviceConnected = true;
                    if (isServerConnected) {
                        buttonStart.setEnabled(true);
                    }
                    break;
                case ManageApplication.MESSAGE_DEVICE_DISCONNECTED:
                    textViewLocalConnect.setText(getString(R.string.local_connect_failed));
                    isDeviceConnected = false;
                    buttonStart.setEnabled(false);
                    break;
                default:
                    break;
            }
        }
    };

    private void getMainInfo() {
        //Log.i(TAG, "getMainInfo() started");

        JSONObject jsonObjectMainInfo = Interface.getMainInfo();


        if (null == jsonObjectMainInfo) {
            L.e(TAG, "getMainInfo failed: server returned null");
            return;
        }
        try {
            if (Interface.isError(jsonObjectMainInfo)) {
                Toast.makeText(MainActivity.this, Interface.getMessage(jsonObjectMainInfo), Toast.LENGTH_LONG).show();
            } else {
                JSONObject jsonData = Interface.getData(jsonObjectMainInfo);
                ManageApplication.getInstance().storeID = Interface.getStoreID(jsonData);
                ManageApplication.getInstance().storeName = Interface.getStoreName(jsonData);

                if (Interface.isBedConnected(jsonData)) {
                    textViewLocalConnect.setText(getString(R.string.local_connect_successful));
                    isDeviceConnected = true;
                    if (isServerConnected) {
                        buttonStart.setEnabled(true);
                    }
                } else {
                    textViewLocalConnect.setText(getString(R.string.local_connect_failed));
                    isDeviceConnected = false;
                    buttonStart.setEnabled(false);
                }
                int localBedID = Interface.getBedID(ManageApplication.getInstance().getDataSQL().getJson(ManageApplication.TABLE_NAME_DEVICE_INFO));
                if (0 == localBedID) {

                    JSONArray jsonArrayBedList = Interface.getBedList(jsonData);
                    bedList.clear();
                    for (int i = 0; i < jsonArrayBedList.length(); i++) {
                        JSONObject jsonObjectTmp = jsonArrayBedList.getJSONObject(i);
                        if (!bedList.contains(jsonObjectTmp)) {
                            bedList.add(jsonObjectTmp);
                        }
                    }
                    bedAdapter.notifyDataSetChanged();
                    if (0 == ManageApplication.getInstance().bedID) {
                        selectBed();
                    }
                } else {
                    String localBedName = Interface.getBedName(ManageApplication.getInstance().getDataSQL().getJson(ManageApplication.TABLE_NAME_DEVICE_INFO));
                    ManageApplication.getInstance().bedID = localBedID;
                    ManageApplication.getInstance().bedName = localBedName;
                    textViewSelectBed.setText(localBedName);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void selectBed() {
        if (!isServerConnected) {
            return;
        }
        try {
            int localBedID = Interface.getBedID(ManageApplication.getInstance().getDataSQL().getJson(ManageApplication.TABLE_NAME_DEVICE_INFO));
            if (0 != localBedID) {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        if (null == alertDialogSelectBed) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View layout = getLayoutInflater().inflate(R.layout.bedlist_frame, null);
            ListView listViewBedList = (ListView) layout.findViewById(R.id.listViewBedList);
            listViewBedList.setAdapter(bedAdapter);
            builder.setView(layout);
            builder.setCancelable(false);
            alertDialogSelectBed = builder.create();
            alertDialogSelectBed.show();
        }
    }

    private BaseAdapter bedAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return bedList.size();
        }

        @Override
        public Object getItem(int position) {
            return bedList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null == convertView) {
                convertView = getLayoutInflater().inflate(R.layout.bedlist_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.bedID = (TextView) convertView.findViewById(R.id.textViewBedID);
                viewHolder.bedName = (TextView) convertView.findViewById(R.id.textViewBedName);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            try {
                final int bedID = Interface.getBedID(bedList.get(position));
                final String bedName = Interface.getBedName(bedList.get(position));
                viewHolder.bedID.setText(String.valueOf(bedID));
                viewHolder.bedName.setText(bedName);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ManageApplication.getInstance().bedID = bedID;
                        ManageApplication.getInstance().bedName = bedName;
                        textViewSelectBed.setText(bedName);
                        if (null != alertDialogSelectBed) {
                            alertDialogSelectBed.dismiss();
                            alertDialogSelectBed = null;
                            buttonStart.setEnabled(false);
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return convertView;
        }

        class ViewHolder {
            TextView bedID;
            TextView bedName;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.i(TAG, "onCreate");
        setContentView(R.layout.main);

        init();//初始化

    }

    private void init() {
        //非常重要，启动网络管理，数据库等模块
        ManageApplication.getInstance().init();

        buttonStart = (Button) findViewById(R.id.buttonStart);

        imageButtonDeviceInfo = (ImageButton) findViewById(R.id.imageButtonDeviceInfo);

        imageButtonManage = (ImageButton) findViewById(R.id.imageButtonManage);

        textViewTime = ((TextView) findViewById(R.id.textViewTime));

        textViewCloudConnect = ((TextView) findViewById(R.id.textViewCloudConnect));

        textViewLocalConnect = ((TextView) findViewById(R.id.textViewLocalConnect));

        textViewSelectBed = (TextView) findViewById(R.id.textViewSelectBed);
        textViewSelectBed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectBed();
            }
        });

        dataSQL = ((ManageApplication) getApplication()).getDataSQL();
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
        }

        //为“启动/start”按钮设置按下行为
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start();
            }
        });

        //为“信息”按钮设置按下行为
        imageButtonDeviceInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoFeedback();
            }
        });

        imageButtonManage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });

        bleManage = new BLEManage();
        bleManage.init(getApplicationContext());
        bleManage.showFloatBall(true);
    }


    private void test() {

    }

    private void gotoFeedback() {
        if (isServerConnected || true) {
            Intent intent = new Intent();
            intent.setClass(this, FeedbackActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        ManageApplication.getInstance().removeCurrentHandler(handler);
        ManageApplication.getInstance().removeCurrentActivity(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ManageApplication.getInstance().setCurrentActivity(this);
        ManageApplication.getInstance().setCurrentActivityHandler(handler);
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

    @Override
    protected void onDestroy() {
        L.i(TAG, "onDestroy()");
        bleManage.close();
        ManageApplication.getInstance().close();
        super.onDestroy();
    }

    //"启动/start"被按下时
    public void start() {
        Intent intent = new Intent();
        JSONObject jsonObject = Interface.mainStart();
        if (null == jsonObject) {
            Toast.makeText(this, "通信失败", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            if (Interface.isError(jsonObject)) {
                Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                return;
            } else {
                JSONObject jsonData = Interface.getData(jsonObject);
                if (null == jsonData) {
                    Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Interface.getState(jsonData) == 0) {
                    intent.setClass(this, LoginActivity.class);
                    intent.putExtra("RequestCode", ManageApplication.REQUEST_CODE_WORKER_LOGIN);
                } else if (Interface.getState(jsonData) == 1) {
                    intent.setClass(this, WorkMainActivity.class);
                } else {
                    Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            L.e(TAG, "start failed:json error");
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
                //nothing need to do
                break;
            default:
                break;
        }
    }

}
