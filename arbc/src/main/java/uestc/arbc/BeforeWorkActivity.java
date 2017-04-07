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

import uestc.arbc.background.Interface;
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

    private EditText editTextCustomerPhone;

    private int rawNum;
    private int serviceTypeID;
    private int rawTypeID;
    private long customerID = 0;
    private String customerName;
    private TextView textViewCustomerInfo;

    Button buttonOpen;
    Spinner spinnerRawNum;
    Spinner spinnerRawType;
    Spinner spinnerServiceType;
    ImageButton imageButtonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beforework);

        init();//初始化
    }

    private void init() {

        customerName = getString(R.string.guest);

        //为艾草数选择框填充数据
        spinnerRawNum = (Spinner) findViewById(R.id.spinnerRawNum);
        String arr[] = new String[6];
        for (int i = 0; i < 6; i++) {
            arr[i] = "" + (i + 1);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.my_spinner, arr);
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

        //为艾草类型和服务费类型选择框填充数据
        spinnerRawType = (Spinner) findViewById(R.id.spinnerRawType);
        spinnerServiceType = (Spinner) findViewById(R.id.spinnerServiceType);
        try {
            JSONObject jsonObject = Interface.getStartSetTypes();
            if (null == jsonObject) {
                Toast.makeText(this, R.string.trans_error, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            if (Interface.isError(jsonObject)) {
                Toast.makeText(this, Interface.getMessage(jsonObject), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }


            JSONObject jsonData = Interface.getData(jsonObject);

            final int defaultServiceID = Interface.getDefaultServiceID(jsonData);
            final int defaultRawID = Interface.getDefaultRawID(jsonData);
            JSONArray jsonArrayServiceType = Interface.getServiceType(jsonData);
            JSONArray jsonArrayRawType = Interface.getRawType(jsonData);
            final JSONObject[] jsonObjectsServiceType = new JSONObject[jsonArrayServiceType.length()];
            final JSONObject[] jsonObjectsRawType = new JSONObject[jsonArrayRawType.length()];

            for (int i = 0; i < jsonArrayServiceType.length(); i++) {
                jsonObjectsServiceType[i] = jsonArrayServiceType.getJSONObject(i);
            }
            for (int i = 0; i < jsonArrayRawType.length(); i++) {
                jsonObjectsRawType[i] = jsonArrayRawType.getJSONObject(i);
            }

            String stringsServiceType[] = new String[jsonArrayServiceType.length()];
            for (int i = 0; i < jsonArrayServiceType.length(); i++) {
                stringsServiceType[i] = Interface.getServiceTypeName(jsonObjectsServiceType[i]);
            }
            String stringsRawType[] = new String[jsonArrayRawType.length()];
            for (int i = 0; i < jsonArrayRawType.length(); i++) {
                stringsRawType[i] = Interface.getRawTypeName(jsonObjectsRawType[i]);
            }

            ArrayAdapter<String> serviceAdapter = new ArrayAdapter<>(this, R.layout.my_spinner, stringsServiceType);
            ArrayAdapter<String> rawAdapter = new ArrayAdapter<>(this, R.layout.my_spinner, stringsRawType);

            serviceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rawAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerServiceType.setAdapter(serviceAdapter);
            spinnerRawType.setAdapter(rawAdapter);


            spinnerServiceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        serviceTypeID = Interface.getServiceTypeID(jsonObjectsServiceType[i]);
                        L.d(TAG, "the serviceTypeID is:" + serviceTypeID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    serviceTypeID = defaultServiceID;
                    L.d(TAG, "the serviceTypeID is:" + serviceTypeID);
                }
            });
            spinnerRawType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        rawTypeID = Interface.getRawTypeID(jsonObjectsRawType[i]);
                        L.d(TAG, "the rawTypeID is:" + rawTypeID);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    rawTypeID = defaultRawID;
                    L.d(TAG, "the rawTypeID is:" + rawTypeID);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            finish();
            return;
        }

        //为“开启”按钮设置按下行为
        buttonOpen = (Button) findViewById(R.id.buttonOpen);
        buttonOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                open();
            }
        });


        //为“关闭”按钮设置按下行为
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);
        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        imageButtonHeatFront = (ImageButton) findViewById(R.id.imageButtonHeatFront);
        imageButtonHeatBack = (ImageButton) findViewById(R.id.imageButtonHeatBack);

        imageButtonIgniteMain = (ImageButton) findViewById(R.id.imageButtonIgniteMain);
        imageButtonIgniteBackup = (ImageButton) findViewById(R.id.imageButtonIgniteBackup);

        setHeatState(imageButtonHeatFront, false);
        setHeatState(imageButtonHeatBack, false);
        setIgniteState(imageButtonIgniteMain, true);
        setIgniteState(imageButtonIgniteBackup, false);

        imageButtonHeatFront.setOnClickListener(heatListener);
        imageButtonHeatBack.setOnClickListener(heatListener);

        imageButtonIgniteMain.setOnClickListener(igniteListener);
        imageButtonIgniteBackup.setOnClickListener(igniteListener);

        //为用户输入框添加监听
        textViewCustomerInfo = (TextView) findViewById(R.id.textViewCustomerInfo);
        textViewCustomerInfo.setText(getString(R.string.guest));
        editTextCustomerPhone = (EditText) findViewById(R.id.editTextCustomerPhone);
        editTextCustomerPhone.addTextChangedListener(new TextWatcher() {
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
                        JSONObject jsonObject = Interface.getCustomerInfo(Long.parseLong(s.toString()));
                        if (null == jsonObject) {
                            Toast.makeText(BeforeWorkActivity.this, getString(R.string.trans_error), Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        if (!Interface.isError(jsonObject)) {
                            JSONObject jsonData = Interface.getData(jsonObject);
                            customerName = Interface.getCustomerName(jsonData);
                            customerID = Interface.getCustomerID(jsonData);
                            textViewCustomerInfo.setText(customerName + "," + Interface.getCustomerSex(jsonData) + "," + Interface.getCustomerAge(jsonData) + getString(R.string.quantifier_age));
                            Toast.makeText(BeforeWorkActivity.this, getString(R.string.welcome) + customerName, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BeforeWorkActivity.this, Interface.getMessage(jsonObject), Toast.LENGTH_SHORT).show();
                            customerID = 0;
                            textViewCustomerInfo.setText(getString(R.string.guest));
                            Intent intent = new Intent();
                            intent.setClass(BeforeWorkActivity.this, CustomerSetActivity.class);
                            intent.putExtra("phone", Long.parseLong(s.toString()));
                            startActivityForResult(intent, ManageApplication.REQUEST_CODE_CUSTOMER_SET);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        finish();
                    }
                } else {
                    customerID = 0;
                    textViewCustomerInfo.setText(getString(R.string.guest));
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ManageApplication.REQUEST_CODE_CUSTOMER_SET == requestCode) {
            if (ManageApplication.RESULT_CODE_FAILED == resultCode) {
                customerID = 0;
                editTextCustomerPhone.setText("");
            } else {
                long phone;
                if ((phone = data.getLongExtra("phone", -1)) == -1) {
                    customerID = 0;
                    editTextCustomerPhone.setText("");
                } else {
                    //触发监听
                    editTextCustomerPhone.setText("");
                    editTextCustomerPhone.setText(String.valueOf(phone));
                }
            }
        }
    }

    private void setHeatState(ImageButton imageButton, Boolean state) {
        if (state) {
            imageButton.setTag(1);
            imageButton.setImageResource(R.drawable.pic_button_heat_on);
        } else {
            imageButton.setTag(0);
            imageButton.setImageResource(R.drawable.pic_button_heat_off);
        }
    }

    private void setIgniteState(ImageButton imageButton, Boolean state) {
        if (state) {
            imageButton.setTag(1);
            imageButton.setImageResource(R.drawable.pic_button_ignite_on);
        } else {
            imageButton.setTag(0);
            imageButton.setImageResource(R.drawable.pic_button_ignite_off);
        }
    }

    private void changeHeatState(ImageButton imageButton) {
        int state = (int) imageButton.getTag();
        if (0 == state) {
            setHeatState(imageButton, true);
        } else {
            setHeatState(imageButton, false);
        }
    }

    private void changeIgniteState(ImageButton imageButton) {
        int state = (int) imageButton.getTag();
        if (0 == state) {
            setIgniteState(imageButton, true);
        } else {
            setIgniteState(imageButton, false);
        }
    }

    View.OnClickListener heatListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeHeatState((ImageButton) view);
        }
    };

    View.OnClickListener igniteListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            changeIgniteState((ImageButton) view);
            if (view.getId() == R.id.imageButtonIgniteMain) {
                setIgniteState(imageButtonIgniteBackup, false);
            } else {
                setIgniteState(imageButtonIgniteMain, false);
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
            jsonObject.put("customerID", customerID);
            jsonObject.put("herbID", rawTypeID);
            jsonObject.put("serviceID", serviceTypeID);

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
            jsonObject = Interface.getBedStartSetting(jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        L.d("TAG", "mainStart setting json data is:" + jsonObject.toString());
        return jsonObject;
    }

    public void open() {
        new StartAsyncTask().execute();
    }

    private class StartAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        JSONObject jsonData;
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
        protected Integer doInBackground(Integer... integers) {
            jsonData = getStartSetting();
            if (null == jsonData) {
                L.e(TAG, "getStartSetting() return null");
                return -3;
            }
            jsonObjectResponse = Interface.bedStart(jsonData);
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
                    Toast.makeText(BeforeWorkActivity.this, getString(R.string.trans_error), Toast.LENGTH_LONG).show();
                    break;
                case -1:
                    try {
                        String msg = jsonObjectResponse.getString("message");
                        Toast.makeText(BeforeWorkActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(BeforeWorkActivity.this, getString(R.string.trans_error), Toast.LENGTH_LONG).show();
                    }
                    break;
                case 0:
                    Toast.makeText(BeforeWorkActivity.this, getString(R.string.start_succeed), Toast.LENGTH_SHORT).show();
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
