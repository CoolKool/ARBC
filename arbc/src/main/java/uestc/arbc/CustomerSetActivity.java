package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import uestc.arbc.background.Interface;
import uestc.arbc.background.Interface.Customer;
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

    EditText editTextCustomerSearch;
    Button buttonCustomerSearch;
    EditText editTextIll;
    EditText editTextRawNum;

    long customerID = 0;
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
        editTextCustomerSearch = (EditText) findViewById(R.id.editTextCustomerSearch);
        buttonCustomerSearch = (Button) findViewById(R.id.buttonCustomerSearch);
        editTextIll = (EditText) findViewById(R.id.editTextIll);
        editTextRawNum = (EditText) findViewById(R.id.editTextRawNum);

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
                finish();
            }
        });

        buttonCustomerSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerSearch();
            }
        });

    }

    private void customerSearch() {
        if (editTextCustomerSearch.getText().toString().isEmpty()) {
            return;
        }
        final List<Customer> customers = Interface.getCustomerInfo(this, editTextCustomerSearch.getText().toString());
        if (null == customers) {
            return;
        }

        ListView listView = (ListView) LayoutInflater.from(CustomerSetActivity.this).inflate(R.layout.customers_frame, null);
        final PopupWindow popupWindow = new PopupWindow(listView, editTextCustomerSearch.getWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        BaseAdapter baseAdapter = new BaseAdapter() {
            @Override
            public int getCount() {
                return customers.size();
            }

            @Override
            public Object getItem(int position) {
                return customers.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                ViewHolder viewHolder;
                if (null == convertView) {
                    convertView = LayoutInflater.from(CustomerSetActivity.this).inflate(R.layout.customers_item, parent, false);
                    viewHolder = new ViewHolder();
                    viewHolder.name = (TextView) convertView.findViewById(R.id.textViewName);
                    viewHolder.sex = (TextView) convertView.findViewById(R.id.textViewSex);
                    viewHolder.age = (TextView) convertView.findViewById(R.id.textViewAge);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }
                final Customer customer = customers.get(position);
                final String name = customer.name;
                String sex = customer.sex;
                final int age = customer.age;
                viewHolder.name.setText(name);
                viewHolder.sex.setText(sex);
                viewHolder.age.setText(age + "岁");
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                customerID = customer.id;
                                editTextCustomerName.setText(name);
                                editTextCustomerName.setEnabled(false);
                                spinnerCustomerSex.setSelection(customer.sex.equals("男") ? 0 : 1, true);
                                spinnerCustomerSex.setEnabled(false);
                                editTextCustomerAge.setText(String.valueOf(age));
                                editTextCustomerAge.setEnabled(false);
                                editTextCustomerPhone.setText(String.valueOf(customer.phone));
                                editTextCustomerPhone.setEnabled(false);

                                popupWindow.dismiss();
                            }
                        });

                    }
                });
                return convertView;
            }

            class ViewHolder {
                TextView name;
                TextView sex;
                TextView age;
            }
        };
        listView.setAdapter(baseAdapter);
        popupWindow.showAsDropDown(editTextCustomerSearch);


    }

    private void submit() {
        if (editTextCustomerName.getText().toString().isEmpty() || editTextCustomerAge.getText().toString().isEmpty()) {
            Toast.makeText(this,"输入为空！",Toast.LENGTH_SHORT).show();
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
        String illDescription;
        int rawNum;

        @Override
        protected void onPreExecute() {

            customerName = editTextCustomerName.getText().toString();
            customerAge = Integer.parseInt(editTextCustomerAge.getText().toString());
            customerPhone = Long.parseLong(editTextCustomerPhone.getText().toString());
            illDescription = editTextIll.getText().toString();
            if (editTextRawNum.getText().toString().isEmpty()) {
                rawNum = 0;
            } else {
                rawNum = Integer.parseInt(editTextRawNum.getText().toString());
            }

            dialog.setTitle("启动提示");
            dialog.setMessage("正在提交...");
            dialog.setCancelable(false);
            dialog.show();
        }


        @Override
        protected Integer doInBackground(JSONObject... jsonObjects) {
            jsonObjectResponse = Interface.setCustomerInfo(customerID, customerName, customerSex, customerAge, customerPhone, illDescription, rawNum);
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
                        String msg = Interface.getMessage(jsonObjectResponse);
                        Toast.makeText(CustomerSetActivity.this, msg, Toast.LENGTH_LONG).show();
                    break;
                case 0:
                    Toast.makeText(CustomerSetActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
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
