package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
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
import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;

/**CustomerSet
 * Created by CK on 2017/1/2.
 */

public class CustomerSetActivity extends Activity {

    private final static String TAG = "CustomerSetActivity";
    MyHandler handler = new MyHandler(TAG);

    TextView textViewCustomerPhone;
    EditText editTextName;
    Spinner spinnerSex;
    EditText editTextAge;
    Button buttonSubmit;
    ImageButton imageButtonCancel;

    private int phone = 0;
    private String userSex = "";

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.customerset);
        init();
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

    private void init() {
        textViewCustomerPhone = (TextView)findViewById(R.id.textViewCustomerPhone);
        editTextName = (EditText) findViewById(R.id.editTextName);
        spinnerSex = (Spinner) findViewById(R.id.spinnerSex);
        editTextAge = (EditText) findViewById(R.id.editTextAge);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);

        Intent intent = getIntent();
        phone = intent.getIntExtra("phone",-1);
        if (-1 == phone) {
            Toast.makeText(this,"获取手机号失败！",Toast.LENGTH_SHORT).show();
            setResult(ManageApplication.RESULT_CODE_FAILED, null);
            finish();
            return;
        }
        textViewCustomerPhone.setText("手机号：" + phone + "的客户信息设置");

        final String[] strings = new String[]{"男","女","其他"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,strings);
        spinnerSex.setAdapter(arrayAdapter);
        spinnerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userSex = strings[position];
                Log.i(TAG,"User Sex is:" + userSex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                userSex = strings[0];
                Log.i(TAG,"User Sex is:" + userSex);
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

    class CustomerSetAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
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
