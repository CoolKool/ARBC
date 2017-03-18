package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.CloudManage;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;

/**CustomerSet
 * Created by CK on 2017/1/2.
 */

public class CustomerSetActivity extends Activity {

    private final static String TAG = "CustomerSetActivity";

    TextView textViewCustomerPhone;
    EditText editTextName;
    Spinner spinnerSex;
    EditText editTextAge;
    Button buttonSubmit;
    ImageButton imageButtonCancel;

    private long phone = 0;
    private String userSex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customerset);
        init();
    }

    private void init() {
        textViewCustomerPhone = (TextView)findViewById(R.id.textViewCustomerPhone);
        editTextName = (EditText) findViewById(R.id.editTextName);
        spinnerSex = (Spinner) findViewById(R.id.spinnerSex);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);

        Intent intent = getIntent();
        phone = intent.getLongExtra("phone",-1);
        if (-1 == phone) {
            Toast.makeText(this,"获取手机号失败！",Toast.LENGTH_SHORT).show();
            setResult(ManageApplication.RESULT_CODE_FAILED, null);
            finish();
            return;
        }
        textViewCustomerPhone.setText("手机号：" + phone + "的客户信息设置");

        final String arr[] = new String[2];
        arr[0] = "男";
        arr[1] = "女";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSex.setAdapter(adapter);
        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.BLACK);
                ((TextView) view).setTextSize(25);
                userSex = arr[i];
                L.d(TAG, "user sex is:" + userSex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                userSex = "男";
                L.d(TAG, "user sex is:" + userSex);
            }
        });

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(ManageApplication.RESULT_CODE_FAILED,null);
                finish();
            }
        });

    }

    private void submit() {
        if (editTextName.getText().toString().isEmpty() || editTextAge.getText().toString().isEmpty()) {
            Toast.makeText(this,"输入为空！",Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", 0);
            jsonObject.put("require","PAD_User_Set");

            JSONObject jsonData = new JSONObject();
            jsonData.put("userPhone",phone);
            jsonData.put("userName",editTextName.getText().toString());
            jsonData.put("userSex",userSex);
            jsonData.put("userAge",Integer.parseInt(editTextAge.getText().toString()));

            jsonObject.put("data",jsonData);

            new CustomerSetAsyncTask().execute(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class CustomerSetAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
        JSONObject jsonObjectResponse;
        ProgressDialog dialog = new ProgressDialog(CustomerSetActivity.this);

        @Override
        protected void onPreExecute() {

            dialog.setTitle("启动提示");
            dialog.setMessage("正在提交...");
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
                    Toast.makeText(CustomerSetActivity.this, "提交失败，与服务器通信异常", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    try {
                        String msg = jsonObjectResponse.getString("message");
                        Toast.makeText(CustomerSetActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CustomerSetActivity.this, "提交失败，获取的服务器数据异常", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 0:
                    Toast.makeText(CustomerSetActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("phone",phone);
                    CustomerSetActivity.this.setResult(ManageApplication.RESULT_CODE_SUCCEED,intent);
                    CustomerSetActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }
}
