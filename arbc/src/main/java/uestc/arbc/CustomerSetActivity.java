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

import uestc.arbc.background.Interface;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;

/**CustomerSet
 * Created by CK on 2017/1/2.
 */

public class CustomerSetActivity extends Activity {

    private final static String TAG = "CustomerSetActivity";

    EditText editTextCustomerPhone;
    EditText editTextCustomerName;
    Spinner spinnerCustomerSex;
    EditText editTextCustomerAge;
    Button buttonSubmit;
    ImageButton imageButtonCancel;

    String customerSex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customerset);
        init();
    }

    private void init() {
        editTextCustomerPhone = (EditText) findViewById(R.id.editTextCustomerPhone);
        editTextCustomerName = (EditText) findViewById(R.id.editTextCustomerName);
        spinnerCustomerSex = (Spinner) findViewById(R.id.spinnerCustomerSex);
        editTextCustomerAge = (EditText) findViewById(R.id.editTextCustomerAge);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);

        Intent intent = getIntent();
        long phone = intent.getLongExtra("phone", -1);
        if (-1 == phone) {
            Toast.makeText(this, "get phone error！", Toast.LENGTH_SHORT).show();
            setResult(ManageApplication.RESULT_CODE_FAILED, null);
            finish();
            return;
        }
        editTextCustomerPhone.setText(String.valueOf(phone));

        final String arr[] = new String[2];
        arr[0] = "男";
        arr[1] = "女";
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustomerSex.setAdapter(adapter);
        spinnerCustomerSex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) view).setTextColor(Color.BLACK);
                ((TextView) view).setTextSize(25);
                customerSex = arr[i];
                L.d(TAG, "user sex is:" + customerSex);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                customerSex = "男";
                L.d(TAG, "user sex is:" + customerSex);
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
        if (editTextCustomerName.getText().toString().isEmpty() || editTextCustomerAge.getText().toString().isEmpty()) {
            Toast.makeText(this,"输入为空！",Toast.LENGTH_SHORT).show();
            return;
        }

        if (editTextCustomerPhone.toString().length() != 11) {
            Toast.makeText(this, "请输入11位手机号", Toast.LENGTH_SHORT).show();
            return;
        }
        new CustomerSetAsyncTask().execute();
    }

    private class CustomerSetAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
        JSONObject jsonObjectResponse;
        ProgressDialog dialog = new ProgressDialog(CustomerSetActivity.this);
        String customerName;
        int customerAge;
        long customerPhone;

        @Override
        protected void onPreExecute() {
            customerName = editTextCustomerName.getText().toString();
            customerAge = Integer.parseInt(editTextCustomerAge.getText().toString());
            customerPhone = Long.parseLong(editTextCustomerPhone.getText().toString());
            dialog.setTitle("启动提示");
            dialog.setMessage("正在提交...");
            dialog.setCancelable(false);
            dialog.show();
        }


        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            jsonObjectResponse = Interface.setCustomerInfo(customerName, customerSex, customerAge, customerPhone);
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
                case -2:
                    Toast.makeText(CustomerSetActivity.this, "提交失败，与服务器通信异常", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    try {
                        String msg = Interface.getMessage(jsonObjectResponse);
                        Toast.makeText(CustomerSetActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(CustomerSetActivity.this, "提交失败，获取的服务器数据异常", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 0:
                    Toast.makeText(CustomerSetActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("phone", customerPhone);
                    CustomerSetActivity.this.setResult(ManageApplication.RESULT_CODE_SUCCEED,intent);
                    CustomerSetActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ManageApplication.getInstance().setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        ManageApplication.getInstance().removeCurrentActivity(this);
        super.onPause();
    }
}
