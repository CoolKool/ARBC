package uestc.arbc.background;



import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
/*
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
    private final static int BROADCAST_PORT = 6682;

    private final static int LOCAL_SERVER_PORT = 6681;
    private final static int TIME_OUT = 1000;

    //deviceID 为0表示还没有注册
    private int deviceID = 0;

    //整个CloudManage继续运行的标志
    private volatile boolean cloudManageKeepRunning = true;

    //上传函数upload()运行完成的标志
    private volatile boolean isUploadDone = false;
    //是否上传完成接收到的数据
    private volatile JSONObject jsonReturn = null;

    //服务端是否继续运行的标志
    private volatile boolean localServerKeepRunning = true;
    //服务端是否正在运行的标志
    private volatile boolean isLocalServerRunning = false;

    private class CloudBroadcastReceiver extends Thread {
        private String TAG = "CloudBroadcastReceiver";
        private volatile boolean isRunning = true;
        private volatile boolean isServerConnected = false;

        private void connectionStateWatcher() {

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    int time = 0;
                    Message disconnected = new Message();
                    disconnected.what = ManageApplication.MESSAGE_SERVER_DISCONNECTED;
                    Message connected = new Message();
                    connected.what = ManageApplication.MESSAGE_SERVER_CONNECTED;

                    while (isRunning) {

                        if (isServerConnected) {
                            time = 0;
                            isServerConnected = false;
                            ManageApplication.getInstance().sendMessage(connected);
                        } else {
                            time += 5000;
                        }
                        if (time > 15000) {
                            ManageApplication.getInstance().sendMessage(disconnected);
                        }
                        SystemClock.sleep(5000);
                    }
                }
            };
            new Thread(runnable).start();
        }

        @Override
        public void run() {
            DatagramSocket udpSocket;
            DatagramPacket udpPacket;
            byte[] data = new byte[256];

            try {
                udpSocket = new DatagramSocket(BROADCAST_PORT);
                udpPacket = new DatagramPacket(data,data.length);
            } catch (SocketException e) {
                e.printStackTrace();
                Log.i(TAG,"udp init failed");
                return;
            }
            connectionStateWatcher();
            while (cloudManageKeepRunning) {
                try {
                    udpSocket.receive(udpPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //获取服务器ip地址
                SERVER_IP_ADDRESS = udpPacket.getAddress().toString();
                Log.i(TAG, "cloud ip is:" + SERVER_IP_ADDRESS);

                //表示云端连接正常
                isServerConnected = true;

            }
            isRunning = false;
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
                Log.i(TAG,"broadcast():send() failed of IOException");
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
                    //连接服务器 并设置连接超时//
                    Log.i(TAG,"upload(): Connecting to Server");
                    socket.connect(new InetSocketAddress(SERVER_IP_ADDRESS, SERVER_PORT), TIME_OUT);
                    Log.i(TAG,"upload(): Server Connected");
                    ////

                    //获取输入输出流//
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    ////

                    //发出信息//
                    Log.i(TAG,"upload(): message sending");
                    byte[] jsonBytes = jsonObject.toString().getBytes("UTF-8");//使用UTF-8编码
                    dataOutputStream.write(jsonBytes);
                    dataOutputStream.flush();
                    Log.i(TAG,"upload(): message sent");
                    socket.shutdownOutput();
                    ////

                    //接收和储存回复信息//
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[2048];
                    int len;
                    Log.i(TAG,"upload(): receiving message");
                    while((len = dataInputStream.read(bytes)) != -1 ) {
                        byteArrayOutputStream.write(bytes,0,len);
                    }
                    ////

                    //将接收的数据转化为String//
                    String strReceived = byteArrayOutputStream.toString("UTF-8");
                    Log.i(TAG,"收到服务器消息:" + strReceived);
                    try {
                        jsonReturn = new JSONObject(strReceived);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.i(TAG,"upload(): finished succeed");
                } catch (ConnectException e) {
                    e.printStackTrace();
                    Log.i(TAG,"upload(): timeout，ConnectException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"upload(): failed，IOException");
                }
                finally {
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

        while(!isUploadDone) {
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
    void close () {
        cloudManageKeepRunning = false;
    }



    //服务端线程，保持运行，随cloudManage结束而结束
    private class LocalServer extends Thread {
        @Override
        public void run() {


            while (cloudManageKeepRunning) {
                try {
                    //赋值，向外界表明服务端已经在运行
                    isLocalServerRunning = true;

                    //服务端的监听端口
                    ServerSocket localServerSocket = new ServerSocket(LOCAL_SERVER_PORT);

                    //broadcast();//广播自己

                    //若serverKeepRun为true就一直运行，外界可以通过改变该值通知服务端线程结束运行
                    while (cloudManageKeepRunning && localServerKeepRunning) {
                        //等待连接
                        Log.i(TAG,"LocalServer: waiting for connect");
                        Socket clientSocket = localServerSocket.accept();
                        Log.i(TAG,"LocalServer: client connected - InetAddress:" + clientSocket.getInetAddress().toString());

                        //获得输入输出流
                        DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                        ////

                        // 接收和储存信息//
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        byte[] bytesReceive = new byte[2048];//储存接收的字节流
                        int len;//记录每次读取的字节流长度

                        Log.i(TAG,"LocalServer: reading message");
                        while((len = dataInputStream.read(bytesReceive)) != -1) {
                            byteArrayOutputStream.write(bytesReceive,0,len);
                        }
                        Log.i(TAG,"LocalServer: message received");
                        ////


                        //发送回复信息//
                        try {
                            JSONObject jsonSend = new JSONObject();

                            jsonSend.put("response","message received");//TODO 回复给客户端的消息

                            byte[] bytesResponse = jsonSend.toString().getBytes("UTF-8");//储存将要发送的字节流
                            dataOutputStream.write(bytesResponse);
                            clientSocket.shutdownOutput();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG,"LocalServer: failed to response : JSONException");
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
                            Log.i(TAG,"LocalServer: 接收到客户端消息:" + jsonReceived.toString());

                            String require = jsonReceived.getString("require");

                            if (require.equals("SERVER_MachineIsConnected")) {
                                Message msg = new Message();
                                msg.what = ManageApplication.MESSAGE_MACHINE_CONNECTED;
                                ManageApplication.getInstance().sendMessage(msg);
                            }

                            if (require.equals("SERVER_MachineIsDisconnected")) {
                                Message msg = new Message();
                                msg.what = ManageApplication.MESSAGE_MACHINE_DISCONNECTED;
                                ManageApplication.getInstance().sendMessage(msg);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i(TAG,"LocalServer: failed while converting String to JSONObject : JSONException");
                        }
                        ////

                        //关闭该连接//
                        Log.i(TAG,"LocalServer: closing clientSocket");
                        clientSocket.close();
                        Log.i(TAG,"LocalServer: clientSocket closed");
                        ////
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"LocalServer: LocalServer will close itself because of IOException");
                } finally {
                    //关闭服务端
                    localServerKeepRunning = false;
                    //表明服务端已经结束运行
                    isLocalServerRunning = false;
                    Log.i(TAG,"LocalServer: LocalServer is closed");
                }
                SystemClock.sleep(500);//若非正常退出，则延迟500ms后重启服务端
            }
        }
    }

    //启动服务端，如果服务端已经在运行则不采取操作
    private void startLocalServer() {
        if (isLocalServerRunning) {
            return;
        }
        localServerKeepRunning = true;
        new LocalServer().start();
    }


    void init() {
        startLocalServer();
        new CloudBroadcastReceiver().start();
    }

    public void setDeviceID (int deviceID) {
        this.deviceID = deviceID;
    }

    public int getDeviceID() {
        return deviceID;
    }

    //艾灸机是否在线
    public boolean isMachineConnected() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_IsMachineConnected");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject jsonResponse = upload(jsonObject);
        if (null == jsonResponse) {
            return false;
        }
        int result;
        try {
            result = jsonResponse.getInt("errorCode");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return 0 == result;
    }

    //获取艾灸机设备的传感器数据
    public JSONObject getMachineState() {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 获取设备传感器数据的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_GetMachineState");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  upload(jsonObject);
    }

    public void mainBoxCtrlUp () {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 主箱向上的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_MachineControl");
            JSONObject data = new JSONObject();
            data.put("cmd","MainBoxUp");
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public void mainBoxCtrlDown () {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 主箱向下的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_MachineControl");
            JSONObject data = new JSONObject();
            data.put("cmd","MainBoxDown");
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public void backBoxCtrlUp () {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 背部箱向上的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_MachineControl");
            JSONObject data = new JSONObject();
            data.put("cmd","BackBoxUp");
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public void backBoxCtrlDown () {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 背部箱向下的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_MachineControl");
            JSONObject data = new JSONObject();
            data.put("cmd","BackBoxDown");
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public void heatBoardCtrl (String whichOne) {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 切换某个加热板开启状态的指令
            jsonObject.put("deviceInfo","hh");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public void devicePause () {
        JSONObject jsonObject = new JSONObject();

        try {
            //TODO 切换运行/暂停的指令
            jsonObject.put("deviceInfo","hh");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        upload(jsonObject);
    }

    public JSONObject getCustomers (int id) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            //TODO 获取客户的的指令
            jsonObject.put("token",deviceID);
            jsonObject.put("require","PAD_MatchCustomer");
            data.put("id",id);
            jsonObject.put("data",data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }
}
