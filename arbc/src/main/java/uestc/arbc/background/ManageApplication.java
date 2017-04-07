package uestc.arbc.background;

import android.app.Activity;
import android.app.Application;
import android.os.Message;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 用于保存全局数据，APP可以在任何地方通过此类获得全局数据
 * Created by CK on 2016/11/9.
 */

public class ManageApplication extends Application {
    private final static String TAG = "ManageApplication";

    public volatile int storeID;
    public volatile int bedID;
    public volatile String storeName;
    public volatile String bedName;
    public volatile int workerID;
    public volatile String workerName;

    public final static String TABLE_NAME_DEVICE_INFO = "deviceInfo";//保存设备信息的表名
    public final static String TABLE_NAME_WORKER_ACCOUNT = "workerAccount";//保存工作人员账号的表名

    //定义各种消息的值
    public final static int MESSAGE_TIME = 1;//更新时间UI
    public final static int MESSAGE_WORK_ERROR = 2;//工作出错

    public final static int MESSAGE_SERVER_CONNECTED = 4;//云端连接
    public final static int MESSAGE_SERVER_DISCONNECTED = 5;//云端断连
    public final static int MESSAGE_DEVICE_CONNECTED = 6;//艾灸机连接
    public final static int MESSAGE_DEVICE_DISCONNECTED = 7;//艾灸机断连
    public final static int MESSAGE_DEVICE_STATE = 8;//艾炙机传感器数据

    //定义请求码和结果码，用于activity间startActivityForResult()
    public final static int REQUEST_CODE_DEVICE_SIGN = 1;
    public final static int REQUEST_CODE_WORKER_LOGIN = 2;
    public final static int REQUEST_CODE_CUSTOMER_SET = 3;

    public final static int RESULT_CODE_SUCCEED = 1;
    public final static int RESULT_CODE_FAILED = 2;

    private static ManageApplication instance = null;//保存此类本身的实例

    private DataSQL dataSQL = null;//数据库管理类
    private TimeThread timeThread = null;//获取时间的线程
    private CloudManage cloudManage = null;//管理网络连接的类

    private volatile MyHandler currentActivityHandler;
    private volatile Activity currentActivity;



    //返回此类的实例，静态方法
    public static ManageApplication getInstance() {
        return instance;
    }

    //程序启动时调用的方法
    @Override
    public void onCreate() {
        super.onCreate();
        L.i(TAG, "onCreate()");
        //app开始运行时将会首先实例化该类，此时将其保存下来，以后可以在任何地方通过静态方法获得该实例
        //从而得到其中的数据
        instance = this;
    }

    public void init() {


        dataSQL = new DataSQL();
        if (!dataSQL.isStartSucceed()) {
            dataSQL = null;
        }

        cloudManage = new CloudManage();
        cloudManage.init();
        startTimeThread();
        initValues();
    }

    public DataSQL getDataSQL() {
        return dataSQL;
    }

    public CloudManage getCloudManage() {
        return cloudManage;
    }

    public void close () {
        L.i(TAG, "close()");
        initValues();
        closeTimeThread();
        cloudManage.close();
        dataSQL.close();
    }

    private void initValues() {
        if (dataSQL.isTableExists(ManageApplication.TABLE_NAME_DEVICE_INFO)) {
            try {
                JSONObject jsonData = dataSQL.getJson(ManageApplication.TABLE_NAME_DEVICE_INFO);
                storeID = Interface.getStoreID(jsonData);
                bedID = Interface.getBedID(jsonData);
                storeName = Interface.getStoreName(jsonData);
                bedName = Interface.getBedName(jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
                storeID = 0;
                bedID = 0;

            }
        } else {
            storeID = 0;
            bedID = 0;
            storeName = "";
            bedName = "";
        }


        workerID = 0;
        workerName = "";
    }

    //activity或线程启动时将其handler发送到此保存，用以和activity通信
    public void setCurrentActivityHandler(MyHandler handler) {
        currentActivityHandler = handler;
        L.d(TAG, "current handler has set to: " + handler.getHandlerName());
    }

    //activity或线程退出时移出其handler
    public void removeCurrentHandler(MyHandler handler) {
        if (currentActivityHandler.equals(handler)) {
            currentActivityHandler = null;
            L.d(TAG, "current handler has set to null");
        }
    }


    public void setCurrentActivity(Activity activity) {
        currentActivity = activity;
        L.d(TAG, "current activity has set to: " + activity.getLocalClassName());
    }

    public void removeCurrentActivity(Activity activity) {
        if (currentActivity.equals(activity)) {
            currentActivity = null;
            L.d(TAG, "current activity has set to null");
        }
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    //开启更新时间线程
    private void startTimeThread() {
        if (null != timeThread) {
            return;
        }

        timeThread = new TimeThread();
        timeThread.start();
    }



    //关闭更新时间线程
    private void closeTimeThread() {
        if (null != timeThread) {
            timeThread.close();
            timeThread = null;
        }
    }

    //向当前activity发送消息
    public synchronized void sendMessage(Message msg) {
       if (null != currentActivityHandler) {
           currentActivityHandler.sendMessage(msg);
       }
    }

    @Override
    public void onTerminate() {
        L.i(TAG, "onTerminate()");
        super.onTerminate();
    }
}
