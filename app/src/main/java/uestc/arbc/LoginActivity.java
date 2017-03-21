package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.DataSQL;
import uestc.arbc.background.Interface;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;

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
            L.e(TAG, "loginMode wrong");
            finish();
        }
        if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
            textViewTitle.setText("设备首次使用注册");
            editTextAccount.setInputType(InputType.TYPE_CLASS_TEXT);
        } else if (ManageApplication.REQUEST_CODE_WORKER_LOGIN == loginMode) {
            textViewTitle.setText("智能艾灸床登录");
            editTextAccount.setInputType(InputType.TYPE_CLASS_TEXT);

            JSONObject jsonData = dataSQL.getJson(ManageApplication.TABLE_NAME_WORKER_ACCOUNT);
            if (null != jsonData) {
                try {
                    String account = jsonData.getString(ManageApplication.TABLE_NAME_WORKER_ACCOUNT);
                    editTextAccount.setText(account);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            L.e(TAG, "loginMode wrong");
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

        new LoginAsyncTask().execute();

    }

    private class LoginAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
        JSONObject jsonObjectResponse;
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute() {

            if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
                dialog.setTitle("注册提示");
                dialog.setMessage("正在注册...");
            } else if (ManageApplication.REQUEST_CODE_WORKER_LOGIN == loginMode) {
                dialog.setTitle("登录提示");
                dialog.setMessage("正在登录...");
            }
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            if (ManageApplication.REQUEST_CODE_DEVICE_SIGN == loginMode) {
                jsonObjectResponse = Interface.deviceSign(stringAccount, stringPassword);
            } else if (ManageApplication.REQUEST_CODE_WORKER_LOGIN == loginMode) {
                jsonObjectResponse = Interface.workerLogin(stringAccount, stringPassword);
            } else {
                return -3;
            }

            if (null == jsonObjectResponse) {
                return -2;//-2表示上传出错，没有得到服务器回应
            } else {
                int errorCode;
                try {
                    errorCode = Interface.getErrorCode(jsonObjectResponse);
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
                case -3:
                    L.e(TAG, "loginMode error");
                    finish();
                    break;
                case -2:
                    Toast.makeText(LoginActivity.this, "登录失败，与服务器通信异常", Toast.LENGTH_LONG).show();
                    setResult(ManageApplication.RESULT_CODE_FAILED, null);
                    break;
                case -1:
                    try {
                        String msg = Interface.getMessage(jsonObjectResponse);
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
                            jsonData.put("password", stringPassword);
                            L.d(TAG, "deviceInfo is:" + jsonData.toString());
                            dataSQL.pushJson(ManageApplication.TABLE_NAME_DEVICE_INFO, jsonData);
                            setResult(ManageApplication.RESULT_CODE_SUCCEED, null);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        finish();
                    } else if (ManageApplication.REQUEST_CODE_WORKER_LOGIN == loginMode) {
                        //工作人员登录成功
                        dataSQL.deleteTable(ManageApplication.TABLE_NAME_WORKER_ACCOUNT);
                        dataSQL.createJsonTable(ManageApplication.TABLE_NAME_WORKER_ACCOUNT);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(ManageApplication.TABLE_NAME_WORKER_ACCOUNT, stringAccount);
                            dataSQL.pushJson(ManageApplication.TABLE_NAME_WORKER_ACCOUNT, jsonObject);
                            JSONObject jsonData = Interface.getData(jsonObjectResponse);
                            ManageApplication.getInstance().workerID = Interface.getWorkerID(jsonData);
                            ManageApplication.getInstance().workerName = Interface.getWorkerName(jsonData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            finish();
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

}
