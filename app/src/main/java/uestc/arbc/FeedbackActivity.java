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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import uestc.arbc.background.Interface;
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

    private void init() {
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
                msg.obj = Interface.getBedInfo();
                handler.sendMessage(msg);
            }
        }).start();

    }

    private void setBedInfo(JSONObject jsonObject) {
        if (null == jsonObject) {
            Toast.makeText(this, getString(R.string.trans_error), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject jsonData = Interface.getData(jsonObject);
            Interface.BedInfo bedInfo = new Interface.BedInfo(jsonData);

            textViewInfoLeft.append(getString(R.string.bed_info_id) + bedInfo.bedID + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_type) + bedInfo.bedType + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_specification) + bedInfo.bedSpecification + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_produce_time) + bedInfo.bedProduceTime + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_board_id) + bedInfo.boardID + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_board_version) + bedInfo.boardVer + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_pad_id) + bedInfo.padID + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_pad_version) + bedInfo.padVer + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_store_id) + bedInfo.storeID + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_store_name) + bedInfo.storeName + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_store_address) + bedInfo.storeAddr + "\n");
            textViewInfoLeft.append(getString(R.string.bed_info_store_tel) + bedInfo.storeTel);

            SpannableString spannableStringWorkTime = new SpannableString("" + bedInfo.workNum);
            spannableStringWorkTime.setSpan(new ForegroundColorSpan(0xffe8ba0e), 0, spannableStringWorkTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoLeft.append("\n共运行");
            textViewInfoLeft.append(spannableStringWorkTime);

            SpannableString spannableStringWorkTotalTime = new SpannableString("" + bedInfo.workTotalTime);
            spannableStringWorkTotalTime.setSpan(new ForegroundColorSpan(0xffe8ba0e), 0, spannableStringWorkTotalTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoLeft.append("次，已工作");
            textViewInfoLeft.append(spannableStringWorkTotalTime);

            SpannableString spannableStringCompany = new SpannableString("四川艾瑞本草科技有限公司");
            spannableStringCompany.setSpan(new AbsoluteSizeSpan(40, true), 0, spannableStringCompany.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewInfoRight.append(spannableStringCompany);
            textViewInfoRight.append(bedInfo.companyIntro);

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 公司地址：" + bedInfo.companyAddr, 0, R.drawable.pic_view_address));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 服务/加盟热线电话：" + bedInfo.companyTel, 0, R.drawable.pic_view_phone));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 服务QQ：" + bedInfo.companyQQ, 0, R.drawable.pic_view_qq));

            textViewInfoRight.append("\n");
            textViewInfoRight.append(getImageSpannedString(" 微信公众号：" + bedInfo.companyWeixin, 0, R.drawable.pic_view_wechat));

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

    private class ProposalAsyncTask extends AsyncTask<String, Integer, Integer> {
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
            jsonObjectResponse = Interface.feedbackSubmit(stringProposal);
            if (null == jsonObjectResponse) {
                return -2;//-2表示上传出错，没有得到服务器回应
            } else {
                try {
                    return Interface.getErrorCode(jsonObjectResponse);
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
                        String msg = Interface.getMessage(jsonObjectResponse);
                        Toast.makeText(FeedbackActivity.this, msg, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(FeedbackActivity.this, "提交失败，获取的服务器数据异常 T_T", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 0:
                    try {
                        Toast.makeText(FeedbackActivity.this, Interface.getMessage(jsonObjectResponse), Toast.LENGTH_LONG).show();
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
