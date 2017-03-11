package uestc.arbc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
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
        /*
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
                */
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
        try {
            JSONObject jsonData = jsonObject.getJSONObject("data");
            int bedID, boardID, padID, storeID, workNum;
            String bedType, companyAddr, companyIntro, companyWeixin, serverTel, specification, storeAddr, storeName;
            long companyQQ, companyTel, produceTime, workTotalTime;
            double boardVer, padVer;

            bedID = jsonData.getInt("bedID");
            boardID = jsonData.getInt("boardID");
            boardVer = jsonData.getDouble("boardVer");
            companyQQ = jsonData.getLong("companyQQ");
            companyTel = jsonData.getLong("companyTel");
            padID = jsonData.getInt("padID");
            padVer = jsonData.getDouble("padVer");
            produceTime = jsonData.getLong("produceTime");
            storeID = jsonData.getInt("storeID");
            workNum = jsonData.getInt("workNum");
            workTotalTime = jsonData.getLong("workTotalTime");
            bedType = jsonData.getString("bedType");
            companyAddr = jsonData.getString("companyAddr");
            companyIntro = jsonData.getString("companyIntro");
            companyWeixin = jsonData.getString("companyWeixin");
            serverTel = jsonData.getString("serverTel");
            specification = jsonData.getString("specification");
            storeAddr = jsonData.getString("storeAddr");
            storeName = jsonData.getString("storeName");

            textViewInfoLeft.append("艾灸床编号：" + bedID);
            textViewInfoLeft.append("\n艾灸床型号：" + bedType);
            textViewInfoLeft.append("\n艾灸床规格：" + specification);
            textViewInfoLeft.append("\n艾灸床生产日期：" + produceTime);
            textViewInfoLeft.append("\n电路板编号：" + boardID);
            textViewInfoLeft.append("\n电路板版本：" + boardVer);
            textViewInfoLeft.append("\n智能平板编号：" + padID);
            textViewInfoLeft.append("\n智能平板版本：" + padVer);
            textViewInfoLeft.append("\n商家编号：" + storeID);
            textViewInfoLeft.append("\n商家名称：" + storeName);
            textViewInfoLeft.append("\n商家地址：" + storeAddr);
            textViewInfoLeft.append("\n商家服务电话：" + serverTel);

            SpannableString spannableStringWorkTime = new SpannableString("" + workNum);
            spannableStringWorkTime.setSpan(new ForegroundColorSpan(0xffe8ba0e), 0, spannableStringWorkTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoLeft.append("\n共运行");
            textViewInfoLeft.append(spannableStringWorkTime);

            //// TODO: 2017/1/6  
            SpannableString spannableStringWorkTotalTime = new SpannableString("" + workTotalTime);
            spannableStringWorkTotalTime.setSpan(new ForegroundColorSpan(0xffe8ba0e), 0, spannableStringWorkTotalTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoLeft.append("次，已工作");
            textViewInfoLeft.append(spannableStringWorkTotalTime);

            SpannableString spannableStringCompany = new SpannableString("四川艾瑞本草科技有限公司");
            spannableStringCompany.setSpan(new AbsoluteSizeSpan(40, true), 0, spannableStringCompany.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoRight.append(spannableStringCompany);
            textViewInfoRight.append(companyIntro);

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 公司地址：" + companyAddr, 0, R.drawable.pic_view_address));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 服务/加盟热线电话：" + companyTel, 0, R.drawable.pic_view_phone));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 服务QQ：" + companyQQ, 0, R.drawable.pic_view_qq));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 微信公众号：" + companyWeixin, 0, R.drawable.pic_view_wechat));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private SpannableString getImageSpannedString(String string, int insertPosition, int resourceID) {
        SpannableString spannableString = new SpannableString(string);
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= 23) {
            drawable = getDrawable(resourceID);
        } else {
            drawable = getResources().getDrawable(resourceID);
        }
        if (null != drawable) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            spannableString.setSpan(new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM), insertPosition, insertPosition + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            Log.i(TAG, "drawable not null");
        }

        return spannableString;
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
