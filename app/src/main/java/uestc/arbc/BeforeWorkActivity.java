package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.CloudManage;
import uestc.arbc.background.L;
import uestc.arbc.background.ManageApplication;

/**
 * ready to work
 * Created by CK on 2016/11/6.
 */

public class BeforeWorkActivity extends Activity {

    private final static String TAG = "BeforeWorkActivity";

    private ImageButton imageButtonHeatFront;
    private ImageButton imageButtonHeatBack;

    private ImageButton imageButtonIgniteMain;
    private ImageButton imageButtonIgniteBackup;

    private EditText editTextCustomer;

    private int rawNum;
    private int consumeTypeID;
    private int herbTypeID;
    private long customerID = 0;
    private String customerName = "散客";
    private TextView textViewCustomerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beforework);

        init();//初始化
    }

    private void init() {

        //为艾草数选择框填充数据
        Spinner spinnerRawNum = (Spinner) findViewById(R.id.spinnerRawNum);
        if (null != spinnerRawNum) {
            String arr[] = new String[20];
            for (int i = 0; i < 20; i++) {
                arr[i] = i + 1 + "盒";
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arr);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRawNum.setAdapter(adapter);
            spinnerRawNum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    rawNum = i + 1;
                    L.d(TAG, "the raw num is:" + rawNum);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    rawNum = 1;
                    L.d(TAG, "the raw num is:" + rawNum);
                }
            });
        }

        //为艾草类型和服务费类型选择框填充数据
        Spinner spinnerRawType = (Spinner) findViewById(R.id.spinnerRawType);
        Spinner spinnerConsumeType = (Spinner) findViewById(R.id.spinnerServiceType);
        if (null != spinnerRawType) {
            try {
                JSONObject jsonObject = ManageApplication.getInstance().getCloudManage().getRawType();
                if (null == jsonObject) {
                    Toast.makeText(this, "与云端通信异常！", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                if (jsonObject.getInt("errorCode") != 0) {
                    Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    finish();
                }


                JSONObject jsonData = jsonObject.getJSONObject("data");
                final int defaultConsumeID = jsonData.getInt("defaultConsumeID");
                final int defaultHerbID = jsonData.getInt("defaultHerbID");
                JSONArray jsonArrayConsumeType = jsonData.getJSONArray("consumeType");
                JSONArray jsonArrayHerbType = jsonData.getJSONArray("herbType");
                final JSONObject[] jsonObjectsConsumeType = new JSONObject[jsonArrayConsumeType.length()];
                final JSONObject[] jsonObjectsHerbType = new JSONObject[jsonArrayHerbType.length()];

                for (int i = 0; i < jsonArrayConsumeType.length(); i++) {
                    jsonObjectsConsumeType[i] = jsonArrayConsumeType.getJSONObject(i);
                }
                for (int i = 0; i < jsonArrayHerbType.length(); i++) {
                    jsonObjectsHerbType[i] = jsonArrayHerbType.getJSONObject(i);
                }

                String stringsConsumeType[] = new String[jsonArrayConsumeType.length()];
                for (int i = 0; i < jsonArrayConsumeType.length(); i++) {
                    stringsConsumeType[i] = jsonObjectsConsumeType[i].getString("name");
                }
                String stringsHerbType[] = new String[jsonArrayHerbType.length()];
                for (int i = 0; i < jsonArrayHerbType.length(); i++) {
                    stringsHerbType[i] = jsonObjectsHerbType[i].getString("name");
                }

                ArrayAdapter<String> consumeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringsConsumeType);
                ArrayAdapter<String> herbAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringsHerbType);

                consumeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                herbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerConsumeType.setAdapter(consumeAdapter);
                spinnerRawType.setAdapter(herbAdapter);


                spinnerConsumeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            consumeTypeID = jsonObjectsConsumeType[i].getInt("dataID");
                            L.d(TAG, "the consumeTypeID is:" + consumeTypeID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        consumeTypeID = defaultConsumeID;
                        L.d(TAG, "the consumeTypeID is:" + consumeTypeID);
                    }
                });
                spinnerRawType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            herbTypeID = jsonObjectsHerbType[i].getInt("dataID");
                            L.d(TAG, "the herbTypeID is:" + herbTypeID);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        herbTypeID = defaultHerbID;
                        L.d(TAG, "the herbTypeID is:" + herbTypeID);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //为“开启”按钮设置按下行为
        Button buttonOpen = (Button) findViewById(R.id.buttonOpen);
        if (null != buttonOpen) {
            buttonOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    open();
                }
            });
        }

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

        imageButtonHeatFront = (ImageButton) findViewById(R.id.imageButtonHeatFront);
        imageButtonHeatBack = (ImageButton) findViewById(R.id.imageButtonHeatBack);

        imageButtonIgniteMain = (ImageButton) findViewById(R.id.imageButtonIgniteMain);
        imageButtonIgniteBackup = (ImageButton) findViewById(R.id.imageButtonIgniteBackup);

        setState(imageButtonHeatFront, true);
        setState(imageButtonHeatBack, true);
        setState(imageButtonIgniteMain, true);
        setState(imageButtonIgniteBackup, false);

        imageButtonHeatFront.setOnClickListener(heatListener);
        imageButtonHeatBack.setOnClickListener(heatListener);

        imageButtonIgniteMain.setOnClickListener(igniteListener);
        imageButtonIgniteBackup.setOnClickListener(igniteListener);

        //为用户输入框添加监听
        textViewCustomerInfo = (TextView) findViewById(R.id.textViewCustomerInfo);
        textViewCustomerInfo.setText("");
        editTextCustomer = (EditText) findViewById(R.id.editTextCustomer);
        editTextCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 11) {
                    try {
                        JSONObject jsonObject = ManageApplication.getInstance().getCloudManage().getCustomerInfo(Long.parseLong(s.toString()));
                        if (null == jsonObject) {
                            Toast.makeText(BeforeWorkActivity.this, "与云端通信异常！", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (jsonObject.getInt("errorCode") == 0) {
                            JSONObject jsonData = jsonObject.getJSONObject("data");
                            Toast.makeText(BeforeWorkActivity.this, "欢迎 " + jsonData.getString("userName"), Toast.LENGTH_SHORT).show();
                            textViewCustomerInfo.setText(jsonData.getString("userName") + "," + jsonData.getString("userSex") + "," + jsonData.getInt("userAge") + "岁");
                            customerID = jsonData.getLong("userID");
                            customerName = jsonData.getString("userName");
                        } else if (jsonObject.getInt("errorCode") == -1){
                            Toast.makeText(BeforeWorkActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                            customerID = 0;
                            textViewCustomerInfo.setText("");
                            Intent intent = new Intent();
                            intent.setClass(BeforeWorkActivity.this,CustomerSetActivity.class);
                            intent.putExtra("phone", Long.parseLong(s.toString()));
                            startActivityForResult(intent, ManageApplication.REQUEST_CODE_CUSTOMER_SET);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    customerID = 0;
                    textViewCustomerInfo.setText("");
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ManageApplication.REQUEST_CODE_CUSTOMER_SET == requestCode) {
            if (ManageApplication.RESULT_CODE_FAILED == resultCode) {
                customerID = 0;
                editTextCustomer.setText("");
            } else {
                String s = editTextCustomer.getText().toString();
                //触发监听
                editTextCustomer.setText("");
                editTextCustomer.setText(s);
            }
        }
    }

    private void setState(ImageButton imageButton, Boolean state) {
        if (state) {
            imageButton.setTag(1);
            imageButton.setImageResource(R.drawable.pic_button_leftup_pressed);
        } else {
            imageButton.setTag(0);
            imageButton.setImageResource(R.drawable.pic_button_leftup_released);
        }
    }

    private void changeState(ImageButton imageButton) {
        int state = (int) imageButton.getTag();
        if (0 == state) {
            setState(imageButton, true);
        } else {
            setState(imageButton, false);
        }
    }

    View.OnClickListener heatListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState((ImageButton) view);
        }
    };

    View.OnClickListener igniteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeState((ImageButton) view);
            if (view.getId() == R.id.imageButtonIgniteMain) {
                setState(imageButtonIgniteBackup, false);
            } else {
                setState(imageButtonIgniteMain, false);
            }
        }
    };


    private JSONObject getStartSetting() {
        JSONObject jsonObject = new JSONObject();
        JSONArray hotSet = new JSONArray();
        JSONArray fireSet = new JSONArray();
        JSONObject jsonTemp;

        try {
            jsonObject.put("storeID", ManageApplication.getInstance().storeID);
            jsonObject.put("workerID", ManageApplication.getInstance().workerID);
            jsonObject.put("bedID", ManageApplication.getInstance().bedID);
            jsonObject.put("num", rawNum);
            jsonObject.put("userID", customerID);
            jsonObject.put("herbID", herbTypeID);
            jsonObject.put("consumeID", consumeTypeID);

            jsonTemp = new JSONObject();
            jsonTemp.put("switchID", 12);
            jsonTemp.put("state", (int) imageButtonHeatFront.getTag());
            hotSet.put(jsonTemp);

            jsonTemp = new JSONObject();
            jsonTemp.put("switchID", 34);
            jsonTemp.put("state", (int) imageButtonHeatBack.getTag());
            hotSet.put(jsonTemp);

            if (((int) imageButtonIgniteMain.getTag()) == 1) {
                jsonTemp = new JSONObject();
                jsonTemp.put("switchID", 13);
                jsonTemp.put("state", 1);
                fireSet.put(jsonTemp);
            }

            if (((int) imageButtonIgniteBackup.getTag()) == 1) {
                jsonTemp = new JSONObject();
                jsonTemp.put("switchID", 24);
                jsonTemp.put("state", 1);
                fireSet.put(jsonTemp);
            }
            jsonObject.put("hotSet", hotSet);
            jsonObject.put("fireSet", fireSet);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        L.i("TAG", "mainStart setting json data is:" + jsonObject.toString());
        return jsonObject;
    }

    public void open() {
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonData;
        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "PAD_Start_Set");
            jsonData = getStartSetting();
            if (null == jsonData) {
                L.e(TAG, "启动数据获取失败");
                return;
            }
            jsonObject.put("data", jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
            L.e(TAG, "启动数据时JSONException");
            return;
        }

        new StartAsyncTask().execute(jsonObject);
    }

    private class StartAsyncTask extends AsyncTask<JSONObject, Integer, Integer> {
        JSONObject jsonObjectResponse;
        ProgressDialog dialog = new ProgressDialog(BeforeWorkActivity.this);

        @Override
        protected void onPreExecute() {

            dialog.setTitle("启动提示");
            dialog.setMessage("正在启动...");
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
                    Toast.makeText(BeforeWorkActivity.this, "启动失败，与服务器通信异常", Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    try {
                        String msg = jsonObjectResponse.getString("message");
                        Toast.makeText(BeforeWorkActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(BeforeWorkActivity.this, "启动失败，获取的服务器数据异常", Toast.LENGTH_LONG).show();
                    }
                    break;
                case 0:
                    Toast.makeText(BeforeWorkActivity.this, "启动成功", Toast.LENGTH_SHORT).show();
                    ManageApplication.getInstance().customerName = customerName;
                    Intent intent = new Intent();
                    intent.setClass(BeforeWorkActivity.this, WorkMainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                default:
                    break;
            }
        }
    }
}
