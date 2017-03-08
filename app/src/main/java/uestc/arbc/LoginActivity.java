package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.DataSQL;
import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.CloudManage;

/**
 * LoginActivity
 * Created by CK on 2016/11/6.
 */

public class LoginActivity extends Activity {
    private final static String TAG = "LoginActivity";

    private int loginMode;
    DataSQL dataSQL;

    String stringAccount,stringPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        init();
    }


    private void init() {
        dataSQL = ManageApplication.getInstance().getDataSQL();
        TextView textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        EditText editTextAccount = (EditText) findViewById(R.id.editTextAccount);
        loginMode = getIntent().getIntExtra("RequestCode", -1);
        if (-1 == loginMode) {
            Log.i(TAG, "loginMode wrong,未知的登录请求");
            finish();
        }
        if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
            textViewTitle.setText("设备首次使用注册");
            editTextAccount.setInputType(InputType.TYPE_CLASS_TEXT);
            //editTextAccount.setHint("请输入艾灸床号");
        } else if (ManageApplication.REQUEST_CODE_USER_LOGIN == loginMode){
            textViewTitle.setText("智能艾灸床登录");
            editTextAccount.setInputType(InputType.TYPE_CLASS_TEXT);
            //editTextAccount.setHint("请输入帐号");


            JSONObject jsonObject = dataSQL.getJson(ManageApplication.TABLE_NAME_USER_ACCOUNT);
            if (null != jsonObject) {
                try {
                    String account = jsonObject.getString("account");
                    editTextAccount.setText(account);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.i(TAG, "loginMode wrong,未知的登录请求");
            finish();
        }

        findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
        //为“关闭”按钮设置按下行为
        ImageButton imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);
        if (null != imageButtonCancel) {
            imageButtonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    private void login() {

        EditText editTextAccount = (EditText) findViewById(R.id.editTextAccount);
        EditText editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        stringAccount = editTextAccount.getText().toString();
        stringPassword = editTextPassword.getText().toString();

        if (stringAccount.isEmpty() || stringPassword.isEmpty()) {
            Toast.makeText(this, "输入不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject data = new JSONObject();
        JSONObject jsonObject = new JSONObject();
        if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
            Log.v(TAG, "device sign");

            try {
                data.put("account", stringAccount);
                /*if (stringAccount.contains("store")) {
                    data.put("code", ManageApplication.string2MD5(stringPassword));
                } else {
                    data.put("code", stringPassword);
                }*/
                data.put("code", stringPassword);
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_DeviceSign");
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v(TAG, "device sign,create JSONObject failed");
            }
        } else if (ManageApplication.REQUEST_CODE_USER_LOGIN == loginMode) {
            Log.v(TAG, "user login");

            try {
                data.put("storeID",ManageApplication.getInstance().storeID);
                data.put("bedID",ManageApplication.getInstance().bedID);
                data.put("account",stringAccount);
                data.put("code", stringPassword);
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Start_Login");
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.v(TAG, "user login,create JSONObject failed");
            }
        }

        new LoginAsyncTask().execute(jsonObject);

    }

    private class LoginAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
        JSONObject jsonObjectResponse;
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {

            dialog.setTitle("登录提示");
            dialog.setMessage("正在登录...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            JSONObject jsonObject = jsonObjects[0];
            CloudManage cloudManage = ((ManageApplication) getApplication()).getCloudManage();
            jsonObjectResponse = cloudManage.upload(jsonObject);
            if (null == jsonObjectResponse) {
                return -2;//-2表示上传出错，没有得到服务器回应
            } else {
                int errorCode;
                try {
                    errorCode = jsonObjectResponse.getInt("errorCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return -2;
                }
                return errorCode;
            }
        }

        @Override
        protected void onPostExecute(Integer errorCode) {
            dialog.dismiss();
            switch (errorCode) {
                case -2:
                    Toast.makeText(LoginActivity.this, "登录失败，与服务器通信异常", Toast.LENGTH_LONG).show();
                    setResult(ManageApplication.RESULT_CODE_FAILED, null);
                    break;
                case -1:
                    try {
                        String msg = jsonObjectResponse.getString("message");
                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "登录失败，获取的服务器数据异常", Toast.LENGTH_LONG).show();
                    }
                    setResult(ManageApplication.RESULT_CODE_FAILED, null);
                    break;
                case 0:
                    Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_LONG).show();


                    if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
                        //设备注册成功
                        //此处保存设备信息
                        dataSQL.createJsonTable(ManageApplication.TABLE_NAME_DEVICE_INFO);

                        try {
                            JSONObject jsonData = jsonObjectResponse.getJSONObject("data");
                            /*if (stringAccount.contains("store")) {
                                jsonData.put("bedID", 0);
                            }*/
                            jsonData.put("password", stringPassword);
                            Log.i(TAG, "deviceInfo is:" + jsonData.toString());
                            dataSQL.pushJson(ManageApplication.TABLE_NAME_DEVICE_INFO, jsonData);
                            setResult(ManageApplication.RESULT_CODE_SUCCEED, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
                    } else if (ManageApplication.REQUEST_CODE_USER_LOGIN == loginMode) {
                        //工作人员登录成功
                        dataSQL.deleteTable(ManageApplication.TABLE_NAME_USER_ACCOUNT);
                        dataSQL.createJsonTable(ManageApplication.TABLE_NAME_USER_ACCOUNT);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("account",stringAccount);
                            dataSQL.pushJson(ManageApplication.TABLE_NAME_USER_ACCOUNT,jsonObject);
                            JSONObject jsonData = jsonObjectResponse.getJSONObject("data");
                            ManageApplication.getInstance().workerID = jsonData.getInt("workerID");
                            ManageApplication.getInstance().workerName = jsonData.getString("workerName");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this,BeforeWorkActivity.class);
                        startActivity(intent);
                        finish();
                    }

                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                new Handler().postDelayed(new Runnable() {
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
    }
}
