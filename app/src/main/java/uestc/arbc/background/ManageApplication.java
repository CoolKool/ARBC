package uestc.arbc.background;

import android.app.Application;
import android.os.Message;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 用于保存全局数据，APP可以在任何地方通过此类获得全局数据
 * Created by CK on 2016/11/9.
 */

public class ManageApplication extends Application {
    private final static String TAG = "ManageApplication";

    //定义各种消息的值
    public final static int MESSAGE_TIME = 1;//更新时间UI
    public final static int MESSAGE_NETWORK_BAD = 2;//断网

    public final static int MESSAGE_SERVER_CONNECTED = 4;//云端连接
    public final static int MESSAGE_SERVER_DISCONNECTED = 5;//云端断连
    public final static int MESSAGE_MACHINE_CONNECTED = 6;//艾灸机连接
    public final static int MESSAGE_MACHINE_DISCONNECTED = 7;//艾灸机断连
    public final static int MESSAGE_MACHINE_STATE = 8;//艾炙机传感器数据

    //定义请求码和结果码，用于activity间startActivityForResult()
    public final static int REQUEST_CODE_DEVICE_SIGN = 1;
    public final static int REQUEST_CODE_USER_LOGIN = 2;
    public final static int RESULT_CODE_SUCCEED = 1;
    public final static int RESULT_CODE_FAILED = 2;

    private static ManageApplication instance = null;//保存此类本身的实例

    private DataSQL dataSQL = null;//数据库管理类
    private TimeThread timeThread = null;//获取时间的线程
    private CloudManage cloudManage = null;//管理网络连接的类

    private volatile MyHandler currentActivityHandler;



    //返回此类的实例，静态方法
    public static ManageApplication getInstance() {
        return instance;
    }

    //程序启动时调用的方法
    @Override
    public void onCreate() {
        super.onCreate();
        //app开始运行时将会首先实例化该类，此时将其保存下来，以后可以在任何地方通过静态方法获得该实例
        //从而得到其中的数据
        instance = this;

        dataSQL = new DataSQL();
        if (!dataSQL.isStartSucceed()) {
            dataSQL = null;
        }

        cloudManage = new CloudManage();
        cloudManage.init();

        startTimeThread();

    }


    public DataSQL getDataSQL() {
        return dataSQL;
    }

    public CloudManage getCloudManage() {
        return cloudManage;
    }

    public void close () {
        closeTimeThread();
        cloudManage.close();
        dataSQL.close();
    }

    //activity或线程启动时将其handler发送到此保存，用以和activity通信
    public void setCurrentActivityHandler(MyHandler handler) {
        currentActivityHandler = handler;
        Log.i(TAG,"current handler has set to: " + handler.getHandlerName());
    }

    //activity或线程退出时移出其handler
    public void removeCurrentHandler() {
        currentActivityHandler = null;
        Log.i(TAG,"current handler has set to null");
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
    public synchronized void sendMessage(Message msgTime) {
       if (null != currentActivityHandler) {
           currentActivityHandler.sendMessage(msgTime);
       }
    }

    public static String string2MD5(String string) {
        String reStr;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(string.getBytes());
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : bytes){
                int bt = b&0xff;
                if (bt < 16){
                    stringBuilder.append(0);
                }
                stringBuilder.append(Integer.toHexString(bt));
            }
            reStr = stringBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            reStr = "MD5 error";
            Log.i(TAG,"make MD5 error");
        }
        return reStr;
    }
}
