package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.ManageApplication;
import uestc.arbc.background.MyHandler;

/**
 * feedback
 * Created by CK on 2016/11/6.
 */

public class FeedbackActivity extends Activity {
    private final static String TAG = "FeedbackActivity";

    TextView textViewInfoLeft;
    TextView textViewInfoRight;
    EditText editTextFeedback;

    ImageButton imageButtonCancel;
    Button buttonSubmit;

    private final static int MESSAGE_DEVICE_INFO = 1;
    MyHandler handler = new MyHandler(TAG) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_DEVICE_INFO:
                    setBedInfo((JSONObject) msg.obj);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback);

        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                handler.postDelayed(new Runnable() {
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

    private void init() {
        //// TODO: 2017/1/5
        textViewInfoLeft = (TextView) findViewById(R.id.textViewInfoLeft);
        textViewInfoRight = (TextView) findViewById(R.id.textViewInfoRight);
        editTextFeedback = (EditText) findViewById(R.id.editTextFeedback);
        imageButtonCancel = (ImageButton) findViewById(R.id.imageButtonCancel);
        buttonSubmit = (Button) findViewById(R.id.buttonSubmit);

        imageButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = MESSAGE_DEVICE_INFO;
                msg.obj = ManageApplication.getInstance().getCloudManage().getBedInfo();
                handler.sendMessage(msg);
            }
        }).start();

    }

    private void setBedInfo(JSONObject jsonObject) {
        if (null == jsonObject) {
            Toast.makeText(this, "获取信息失败 T_T", Toast.LENGTH_SHORT).show();
            return;
        }
        //// TODO: 2017/1/5
        try {
            JSONObject jsonData = jsonObject.getJSONObject("data");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void submit() {
        String content = editTextFeedback.getText().toString();
        if (content.isEmpty()) {
            Toast.makeText(this, "还什么都没填呢 -_-||", Toast.LENGTH_SHORT).show();
            return;
        }
        new ProposalAsyncTask().execute(content);
    }

    class ProposalAsyncTask extends AsyncTask<String, Integer, Integer> {
        ProgressDialog dialog = new ProgressDialog(FeedbackActivity.this);
        JSONObject jsonObjectResponse;

        @Override
        protected void onPreExecute() {

            dialog.setTitle("提示");
            dialog.setMessage("正在提交...");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            String stringProposal = strings[0];
            jsonObjectResponse = ManageApplication.getInstance().getCloudManage().feedbackSubmit(stringProposal);
            if (null == jsonObjectResponse) {
                return -2;//-2表示上传出错，没有得到服务器回应
            } else {
                try {
                    return jsonObjectResponse.getInt("errorCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return -2;
                }
            }
        }

        @Override
        protected void onPostExecute(Integer errorCode) {
            dialog.dismiss();
            switch (errorCode) {
                case -2:
                    Toast.makeText(FeedbackActivity.this, "提交失败，与服务器通信异常 T_T", Toast.LENGTH_SHORT).show();
                    break;
                case -1:
                    try {
                        String msg = jsonObjectResponse.getString("message");
                        Toast.makeText(FeedbackActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(FeedbackActivity.this, "提交失败，获取的服务器数据异常 T_T", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0:
                    try {
                        Toast.makeText(FeedbackActivity.this, jsonObjectResponse.getString("message"), Toast.LENGTH_LONG).show();
                        editTextFeedback.setText("");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }
        }
    }
}
