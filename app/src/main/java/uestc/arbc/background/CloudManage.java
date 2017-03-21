package uestc.arbc.background;


import android.os.Message;
import android.os.SystemClock;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
/*
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import java.net.InetAddress;
*/

/**
 * manage cloud connection
 * Created by CK on 2016/11/11.
 */

public class CloudManage {

    private final static String TAG = "CloudManage";

    private String SERVER_IP_ADDRESS = null;
    private final static int SERVER_PORT = 6680;
    private final static int BROADCAST_PORT = 61688;

    private final static int LOCAL_SERVER_PORT = 6681;
    private final static int TIME_OUT = 1000;

    //整个CloudManage继续运行的标志
    private volatile boolean cloudManageKeepRunning = true;

    //上传函数upload()运行完成的标志
    private volatile boolean isUploadDone = false;
    //是否上传完成接收到的数据
    private volatile JSONObject jsonReturn = null;

    private LocalServer localServer = null;
    //服务端是否继续运行的标志
    private volatile boolean localServerKeepRunning = true;
    //服务端是否正在运行的标志
    private volatile boolean isLocalServerRunning = false;

    private CloudBroadcastReceiver cloudBroadcastReceiver = null;

    private class CloudBroadcastReceiver extends Thread {
        private String TAG = "CloudBroadcastReceiver";
        private volatile boolean isRunning = true;
        private volatile boolean isServerConnected = false;
        DatagramSocket udpSocket;

        private void connectionStateWatcher() {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int connectDelay = 0;
                    int disconnectDelay = 0;

                    L.i(TAG, "connectionStateWatcher is running");
                    while (cloudManageKeepRunning && isRunning) {

                        if (isServerConnected) {
                            connectDelay++;
                            disconnectDelay = 0;
                            isServerConnected = false;
                            if (connectDelay > 3) {
                                connectDelay = 0;
                                Message connected = new Message();
                                connected.what = ManageApplication.MESSAGE_SERVER_CONNECTED;
                                ManageApplication.getInstance().sendMessage(connected);
                            }
                        } else {
                            connectDelay = 0;
                            disconnectDelay++;
                            if (disconnectDelay > 10) {
                                Message disconnected = new Message();
                                disconnected.what = ManageApplication.MESSAGE_SERVER_DISCONNECTED;
                                ManageApplication.getInstance().sendMessage(disconnected);
                            }
                        }

                        SystemClock.sleep(1000);
                    }
                    L.i(TAG, "connection state watcher closed");
                }
            };
            new Thread(runnable).start();
        }

        @Override
        public void run() {
            DatagramPacket udpPacket;
            byte[] data = new byte[100];

            try {
                udpSocket = new DatagramSocket(BROADCAST_PORT);
                //udpSocket.setSoTimeout(TIME_OUT);
                udpPacket = new DatagramPacket(data, data.length);
            } catch (SocketException e) {
                e.printStackTrace();
                L.e(TAG, "udp init failed");
                return;
            }
            connectionStateWatcher();
            L.i(TAG, "udp listener is running");
            while (cloudManageKeepRunning) {
                try {
                    udpSocket.receive(udpPacket);
                    try {
                        int packetLength = udpPacket.getLength();
                        if (0 == udpPacket.getData()[packetLength - 1]) {
                            packetLength--;
                        }
                        L.v(TAG, "received a broadcast,ip is:" + udpPacket.getAddress().toString() + " data is:" + new String(udpPacket.getData(), 0, packetLength));
                        String string = new String(udpPacket.getData(), 0, packetLength, "UTF-8");
                        String[] strings = string.split(" ");
                        // 广播处理
                        if (strings.length == 4 && strings[0].equals("AiRuiYun")) {
                            //获取服务器ip地址
                            SERVER_IP_ADDRESS = udpPacket.getAddress().toString().substring(1);
                            //int storeID = Integer.parseInt(strings[1]);
                            //Log.i(TAG, "cloud ip is:" + SERVER_IP_ADDRESS);
                            //Log.i(TAG, "cloud broadcast data is:" + string);
                            //表示云端连接正常
                            isServerConnected = true;
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    L.i(TAG, "IOException while receiving udpPacket");
                    e.printStackTrace();
                    break;
                }
            }

            udpSocket.close();
            isRunning = false;
            L.i(TAG, "udp listener closed");
        }
    }


    /*
        //获取广播地址
        private static InetAddress getBroadcastAddress() throws UnknownHostException {
            WifiManager wifi = (WifiManager) ManageApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            if(dhcp==null) {
                return InetAddress.getByName("255.255.255.255");
            }
            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k ++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
          return InetAddress.getByAddress(quads);
        }
    /*
        //广播本机
        private void broadcast () {
            DatagramSocket socket;
            InetAddress addr;
            try {
                socket = new DatagramSocket();
                socket.setBroadcast(true);
            } catch (SocketException e) {
                e.printStackTrace();
                Log.i(TAG,"broadcast():create socket failed of SocketException");
                return;
            }

            try {
                addr = getBroadcastAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                Log.i(TAG,"broadcast():getBroadcastAddress() failed of UnknownHostException");
                return;
            }
            byte[] buffer = "Hello Server".getBytes();
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
            packet.setAddress(addr);
            packet.setPort(BROADCAST_PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
                L.e(TAG,"broadcast():send() failed of IOException");
            }
        }
    */
    //向服务器上传数据 参数为JSONObject，成功返回true，失败返回false
    public synchronized JSONObject upload(final JSONObject jsonObject) {

        if (null == SERVER_IP_ADDRESS) {
            return null;
        }

        if (null == jsonObject) {
            return null;
        }
        JSONObject emptyJSONObject = new JSONObject();
        if (jsonObject.equals(emptyJSONObject)) {
            return null;
        }


        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Socket socket = new Socket();
                try {
                    L.d(TAG, "upload(): Json to upload is:" + jsonObject.toString());
                    //连接服务器 并设置连接超时//
                    L.v(TAG, "upload(): Connecting to Server");
                    socket.connect(new InetSocketAddress(SERVER_IP_ADDRESS, SERVER_PORT), TIME_OUT);
                    L.v(TAG, "upload(): Server Connected");
                    ////

                    //获取输入输出流//
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    ////

                    //发出信息//
                    L.v(TAG, "upload(): message sending");
                    byte[] jsonBytes = jsonObject.toString().getBytes("UTF-8");//使用UTF-8编码
                    dataOutputStream.write(jsonBytes);
                    dataOutputStream.flush();
                    L.v(TAG, "upload(): message sent");
                    socket.shutdownOutput();
                    ////

                    //接收和储存回复信息//
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[2048];
                    int len;
                    L.v(TAG, "upload(): receiving message");
                    while ((len = dataInputStream.read(bytes)) > 0) {
                        byteArrayOutputStream.write(bytes, 0, len);
                    }
                    ////

                    //将接收的数据转化为String//
                    String strReceived = byteArrayOutputStream.toString("UTF-8");
                    L.d(TAG, "upload() 收到服务器消息:" + strReceived);
                    try {
                        jsonReturn = new JSONObject(strReceived);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    L.v(TAG, "upload(): finished succeed");
                } catch (ConnectException e) {
                    e.printStackTrace();
                    L.e(TAG, "upload(): timeout，ConnectException");
                } catch (IOException e) {
                    e.printStackTrace();
                    L.e(TAG, "upload(): failed，IOException");
                } finally {
                    isUploadDone = true;

                    try {
                        //关闭连接//
                        socket.close();
                        ////
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        new Thread(runnable).start();

        while (!isUploadDone) {
            SystemClock.sleep(50);
        }

        //保存上传是否成功的结果
        JSONObject result = jsonReturn;

        //此处无错，将上传标志初始化用//
        isUploadDone = false;
        jsonReturn = null;
        ////

        //返回上传是否成功的结果
        return result;
        ////
    }

    //关闭cloudManage
    void close() {
        cloudManageKeepRunning = false;
        if (isLocalServerRunning && null != localServer) {
            try {
                localServer.localServerSocket.close();
                localServer = null;
            } catch (IOException e) {
                L.i(TAG, "IOException while close server socket");
            }
        }
        if (null != cloudBroadcastReceiver) {
            cloudBroadcastReceiver.udpSocket.close();
            cloudBroadcastReceiver = null;
        }
    }


    //服务端线程，保持运行，随cloudManage结束而结束
    private class LocalServer extends Thread {
        ServerSocket localServerSocket = null;
        @Override
        public void run() {


            while (cloudManageKeepRunning) {
                try {
                    //赋值，向外界表明服务端已经在运行
                    isLocalServerRunning = true;

                    //服务端的监听端口
                    localServerSocket = new ServerSocket(LOCAL_SERVER_PORT);

                    //broadcast();//广播自己

                    //若serverKeepRun为true就一直运行，外界可以通过改变该值通知服务端线程结束运行
                    while (cloudManageKeepRunning && localServerKeepRunning) {
                        //等待连接
                        L.d(TAG, "LocalServer: waiting for connect");
                        Socket clientSocket = localServerSocket.accept();
                        L.d(TAG, "LocalServer: client connected - InetAddress:" + clientSocket.getInetAddress().toString());

                        //获得输入输出流
                        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                        ////

                        // 接收和储存信息//
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bytesReceive = new byte[2048];//储存接收的字节流
                        int len;//记录每次读取的字节流长度

                        L.v(TAG, "LocalServer: reading message");
                        while ((len = dataInputStream.read(bytesReceive)) != -1) {
                            byteArrayOutputStream.write(bytesReceive, 0, len);
                        }
                        L.v(TAG, "LocalServer: message received");
                        ////


                        //发送回复信息//
                        try {
                            JSONObject jsonSend = new JSONObject();

                            jsonSend.put("response", "message received");//TODO 回复给客户端的消息

                            byte[] bytesResponse = jsonSend.toString().getBytes("UTF-8");//储存将要发送的字节流
                            dataOutputStream.write(bytesResponse);
                            clientSocket.shutdownOutput();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            L.e(TAG, "LocalServer: failed to response : JSONException");
                        }
                        ////


                        //将接收的数据转化为String//
                        String strReceived = byteArrayOutputStream.toString("UTF-8");
                        byteArrayOutputStream.close();
                        ////

                        //将String转化为JSONObject并根据数据采取行动//
                        try {
                            JSONObject jsonReceived = new JSONObject(strReceived);
                            //TODO 根据获取的数据采取相应操作
                            L.d(TAG, "LocalServer: 接收到客户端消息:" + jsonReceived.toString());

                            String require = jsonReceived.getString("require");

                            if (require.equals("SERVER_MachineIsConnected")) {
                                Message msg = new Message();
                                msg.what = ManageApplication.MESSAGE_DEVICE_CONNECTED;
                                ManageApplication.getInstance().sendMessage(msg);
                            }

                            if (require.equals("SERVER_MachineIsDisconnected")) {
                                Message msg = new Message();
                                msg.what = ManageApplication.MESSAGE_DEVICE_DISCONNECTED;
                                ManageApplication.getInstance().sendMessage(msg);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            L.e(TAG, "LocalServer: failed while converting String to JSONObject : JSONException");
                        }
                        ////

                        //关闭该连接//
                        L.d(TAG, "LocalServer: closing clientSocket");
                        clientSocket.close();
                        L.d(TAG, "LocalServer: clientSocket closed");
                        ////
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    L.i(TAG, "LocalServer: LocalServer will close itself because of IOException");
                } finally {
                    //关闭服务端
                    localServerKeepRunning = false;
                    //表明服务端已经结束运行
                    isLocalServerRunning = false;
                    L.i(TAG, "LocalServer: LocalServer is closed");
                }
                SystemClock.sleep(500);//若非正常退出，则延迟500ms后重启服务端
            }
        }
    }

    //启动服务端，如果服务端已经在运行则不采取操作
    private void startLocalServer() {
        if (isLocalServerRunning || !cloudManageKeepRunning || null != localServer) {
            return;
        }
        localServerKeepRunning = true;
        localServer = new LocalServer();
        localServer.start();
    }


    void init() {
        cloudManageKeepRunning = true;
        //// TODO: if server need
        //startLocalServer();
        startCloudBroadcastReceiver();
        L.i(TAG, "initialed");
    }

    private void startCloudBroadcastReceiver() {
        if (cloudManageKeepRunning && null == cloudBroadcastReceiver) {
            cloudBroadcastReceiver = new CloudBroadcastReceiver();
            cloudBroadcastReceiver.start();
        }
    }

    /*
        //获取主界面信息
        public JSONObject getMainInfo() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject deviceInfo = ManageApplication.getInstance().getDataSQL().getJson(ManageApplication.TABLE_NAME_DEVICE_INFO);

            try {
                jsonObject.put("token", 0);
                jsonObject.put("require", "PAD_Main_Info");
                data.put("storeID", deviceInfo.getInt("storeID"));
                data.put("bedID", deviceInfo.getInt("bedID"));
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        //启动请求
        public JSONObject mainStart() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();

            try {
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Main_Start");
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        //获取艾草类型
        public JSONObject getRawType() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();

            try {
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Start_GetType");
                data.put("storeID", ManageApplication.getInstance().storeID);
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }


        //获取艾灸机设备的传感器数据
        public JSONObject getDeviceState() {
            JSONObject jsonObject = new JSONObject();
            try {
                // 获取设备传感器数据的指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Monitor");

                JSONObject jsonData = new JSONObject();
                jsonData.put("storeID", ManageApplication.getInstance().storeID);
                jsonData.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }


        public JSONObject getCustomerInfo(long phone) {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                //获取客户的的指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_User_Info");
                data.put("phone", phone);
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject bedControl(String operateType, int targetState) {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                //控制指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Control");
                data.put("operateType", operateType);
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);
                data.put("state", targetState);
                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject getBedInfo() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                //获取设备信息的指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Bed_Info");
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject feedbackSubmit(String content) {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                //提交反馈的指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Proposal");
                data.put("content", content);
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject getCheckoutInfo() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Check_Info");
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject checkoutSubmit() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Check_Submit");
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }

        public JSONObject devicePause() {
            JSONObject jsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            try {
                //暂停的指令
                jsonObject.put("token", "0");
                jsonObject.put("require", "PAD_Work_Pause");
                data.put("storeID", ManageApplication.getInstance().storeID);
                data.put("bedID", ManageApplication.getInstance().bedID);

                jsonObject.put("data", data);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return upload(jsonObject);
        }
    */
    public DeviceState makeDeviceState(MyHandler handler) {
        return new DeviceState(handler);
    }

    public class DeviceState {
        private volatile boolean running = false;
        private long DEVICE_STATE_DELAY = 1000;
        private MyHandler handler = null;
        private volatile int failedTime = 0;

        DeviceState(MyHandler handler) {
            this.handler = handler;
        }

        public void getOnce() {
            try {
                JSONObject jsonObjectDeviceState = Interface.getDeviceState();
                if (null != jsonObjectDeviceState) {
                    failedTime = 0;
                    if (jsonObjectDeviceState.getInt("errorCode") == 0) {
                        JSONObject jsonData = jsonObjectDeviceState.getJSONObject("data");
                        if (jsonData.getInt("stateNet") == 1) {
                            Message message = new Message();
                            message.what = ManageApplication.MESSAGE_DEVICE_STATE;
                            message.obj = jsonObjectDeviceState;
                            handler.sendMessage(message);
                        } else {
                            Message message = new Message();
                            message.what = ManageApplication.MESSAGE_DEVICE_DISCONNECTED;
                            handler.sendMessage(message);
                        }
                    } else if (jsonObjectDeviceState.getInt("errorCode") == 1) {
                        Message message = new Message();
                        message.what = ManageApplication.MESSAGE_WORK_ERROR;
                        handler.sendMessage(message);
                    } else {
                        L.e(TAG, "get deviceState error:" + jsonObjectDeviceState.toString());
                    }
                } else {
                    failedTime++;
                    if (failedTime > 3) {
                        Message message = new Message();
                        message.what = ManageApplication.MESSAGE_SERVER_DISCONNECTED;
                        handler.sendMessage(message);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private class GetDeviceStateThread extends Thread {
            @Override
            public void run() {
                while (running) {
                    getOnce();
                    SystemClock.sleep(DEVICE_STATE_DELAY);
                }
            }
        }

        public boolean startLoop() {
            if (running || null == handler) {
                return false;
            }
            running = true;
            new GetDeviceStateThread().start();
            return true;
        }

        public boolean startLoop(long delay) {
            setDelay(delay);
            return startLoop();
        }

        public void stopLoop() {
            running = false;
        }

        public boolean setDelay(long delay) {
            if (delay < 0) {
                return false;
            } else {
                DEVICE_STATE_DELAY = delay;
                return true;
            }
        }

        public boolean setHandler(MyHandler handler) {
            if (null == handler) {
                return false;
            }

            this.handler = handler;
            return true;
        }

        public long getDelay() {
            return DEVICE_STATE_DELAY;
        }
    }
}
