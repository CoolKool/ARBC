package uestc.arbc.background;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * use to define cloud interface
 * Created by CK on 2017/3/20.
 */

public class Interface {

    private static JSONObject upload(JSONObject jsonObject) {
        return ManageApplication.getInstance().getCloudManage().upload(jsonObject);
    }

    //获取主界面信息
    public static JSONObject getMainInfo() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("token", 0);
            jsonObject.put("require", "PAD_Main_Info");
            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("bedID", ManageApplication.getInstance().bedID);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //设备注册
    public static JSONObject deviceSign(@NonNull String account, @NonNull String password) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "PAD_DeviceSign");
            data.put("account", account);
            data.put("code", password);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //工作人员登录
    public static JSONObject workerLogin(@NonNull String account, @NonNull String password) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "PAD_Start_Login");
            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("bedID", ManageApplication.getInstance().bedID);
            data.put("account", account);
            data.put("code", password);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //进入工作主界面请求
    public static JSONObject workBed() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "STORE_WORKBED");
            data.put("operateType", "OPEN");
            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("bedID", ManageApplication.getInstance().bedID);
            data.put("workerID", ManageApplication.getInstance().workerID);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //主界面启动请求
    public static JSONObject mainStart() {
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

    //获取启动时的设置数据
    public static JSONObject getStartSetTypes() {
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


    //艾灸床启动
    public static JSONObject bedStart(@NonNull JSONObject jsonData) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "PAD_Start_Set");
            jsonObject.put("data", jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //获取艾灸机设备的传感器数据
    public static JSONObject getDeviceState() {
        JSONObject jsonObject = new JSONObject();
        try {
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

    public static class Customer {
        public long id;
        public String name;
        public String sex;
        public int age;
        public long phone;
    }

    //获取客户信息的指令
    public static List<Customer> getCustomerInfo(final Activity activity, String info) {
        JSONObject jsonObject = new JSONObject();
        final JSONObject jsonResponse;
        JSONObject data = new JSONObject();
        try {

            jsonObject.put("token", "0");
            jsonObject.put("require", "SEARCH");
            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("operateType", "USER");
            data.put("data", info);
            jsonObject.put("data", data);

            jsonResponse = upload(jsonObject);
            if (null == jsonResponse) {
                return null;
            }
            if (isError(jsonResponse)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, getMessage(jsonResponse), Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            if ((data = getData(jsonResponse)) == null) {
                return null;
            }

            List<Customer> customers = new ArrayList<>();

            int num = data.getInt("num");
            JSONArray jsonArrayList = data.getJSONArray("dataList");
            JSONObject jsonObjectCustomer;
            int i;
            for (i = 0; i < num; i++) {
                jsonObjectCustomer = jsonArrayList.getJSONObject(i);
                Customer customer = new Customer();
                customer.id = jsonObjectCustomer.getLong("userID");
                customer.name = jsonObjectCustomer.getString("name");
                customer.sex = jsonObjectCustomer.getString("sex");
                customer.age = jsonObjectCustomer.getInt("age");
                customer.phone = jsonObjectCustomer.getLong("phone");
                customers.add(customer);
            }

            return customers;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }


    }

    public static JSONObject setCustomerInfo(long userID, @NonNull String customerName, @NonNull String customerSex, int customerAge, long phone, @Nullable String illDescription, int rawNum) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            jsonObject.put("token", 0);
            jsonObject.put("require", "WORKBED_USER");

            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("bedID", ManageApplication.getInstance().bedID);
            data.put("userID", userID);
            data.put("userName", customerName);
            data.put("userSex", customerSex);
            data.put("userAge", customerAge);
            data.put("userPhone", phone);
            data.put("description", null == illDescription ? "" : illDescription);
            data.put("addNum", rawNum);

            jsonObject.put("data", data);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    //控制指令
    private static JSONObject bedControl(String operateType, int targetState) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {

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

    //控制指令：主电机上
    public static JSONObject bedControlMainMotorUp() {
        return bedControl("MAINMOTOR", 1);
    }

    //控制指令：主电机下
    public static JSONObject bedControlMainMotorDown() {
        return bedControl("MAINMOTOR", 2);
    }

    //控制指令：主电机停
    public static JSONObject bedControlMainMotorStop() {
        return bedControl("MAINMOTOR", 0);
    }

    //控制指令：主点燃打开
    public static JSONObject bedControlIgniteMainOn() {
        return bedControl("FIRE_MAIN", 1);
    }

    //控制指令：备用点燃打开
    public static JSONObject bedControlIgniteBackupOn() {
        return bedControl("FIRE_TMP", 1);
    }

    //控制指令：点燃关闭
    public static JSONObject bedControlIgniteOff() {
        return bedControl("FIRE_STOP", 1);
    }

    //控制指令：前加热打开
    public static JSONObject bedControlHeatFrontOn() {
        return bedControl("HOT_PREV", 1);
    }

    //控制指令：后加热打开
    public static JSONObject bedControlHeatBackOn() {
        return bedControl("HOT_NEXT", 1);
    }

    //控制指令：加热关闭
    public static JSONObject bedControlHeatOff() {
        return bedControl("HOT_STOP", 1);
    }

    //控制指令：风扇开
    public static JSONObject bedControlFanOn() {
        return bedControl("WIND", 1);
    }

    //控制指令：风扇关
    public static JSONObject bedControlFanOff() {
        return bedControl("WIND", 2);
    }

    //控制指令：风扇停
    public static JSONObject bedControlFanStop() {
        return bedControl("WIND", 0);
    }

    //获取设备信息的指令
    public static JSONObject getBedInfo() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {

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

    //提交反馈的指令
    public static JSONObject feedbackSubmit(String content) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {

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

    //获取结帐信息
    public static JSONObject getCheckoutInfo() {
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

    //确认结帐
    public static JSONObject checkoutSubmit() {
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

    //暂停设备
    public static JSONObject devicePause() {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();
        try {
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

    //提交手环监控数据
    public static JSONObject sendHealthData(float bodyTemperature, int heartRate, int bloodPressure, int stepCount) {
        JSONObject jsonObject = new JSONObject();
        JSONObject data = new JSONObject();

        try {
            jsonObject.put("token", "0");
            jsonObject.put("require", "HEALTH_Monitor");
            data.put("storeID", ManageApplication.getInstance().storeID);
            data.put("bedID", ManageApplication.getInstance().bedID);
            data.put("bodyTemperature", bodyTemperature);
            data.put("heartRate", heartRate);
            data.put("bloodPressure", bloodPressure);
            data.put("walkStep", stepCount);

            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return upload(jsonObject);
    }

    public static int getErrorCode(@NonNull JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt("errorCode");
    }

    public static boolean isError(@NonNull JSONObject jsonObject) {
        boolean isError;
        try {
            isError = getErrorCode(jsonObject) != 0;
        } catch (JSONException e) {
            e.printStackTrace();
            isError = true;
        }
        return isError;
    }

    public static String getMessage(@NonNull JSONObject jsonObject) {
        String message = "";
        try {
            message = jsonObject.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return message;
    }

    public static JSONObject getData(@NonNull JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject("data");
    }

    public static int getStoreID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("storeID");
    }

    public static String getStoreName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("storeName");
    }

    public static int getBedID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("bedID");
    }

    public static String getBedName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("bedName");
    }

    public static boolean isBedConnected(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("boardConnect") == 1;
    }

    public static int getWorkerID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("workerID");
    }

    public static String getWorkerName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("workerName");
    }

    public static JSONArray getBedList(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getJSONArray("bedList");
    }

    public static int getState(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("state");
    }

    public static int getDefaultServiceID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("defaultConsumeID");
    }

    public static int getDefaultRawID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("defaultHerbID");
    }

    public static JSONArray getServiceType(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getJSONArray("consumeType");
    }

    public static JSONArray getRawType(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getJSONArray("herbType");
    }

    public static String getServiceTypeName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("name");
    }

    public static String getRawTypeName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("name");
    }

    public static int getServiceTypeID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("dataID");
    }

    public static int getRawTypeID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getInt("dataID");
    }

    public static String getCustomerName(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("userName");
    }

    public static String getCustomerSex(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("userSex");
    }

    public static String getCustomerAge(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getString("userAge");
    }

    public static long getCustomerID(@NonNull JSONObject jsonData) throws JSONException {
        return jsonData.getLong("userID");
    }

    public static JSONObject getBedStartSetting(@NonNull JSONObject jsonIn) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray hotSet = new JSONArray();
        JSONArray fireSet = new JSONArray();
        JSONObject jsonTemp;


        jsonObject.put("storeID", jsonIn.getInt("storeID"));
        jsonObject.put("workerID", jsonIn.getInt("workerID"));
        jsonObject.put("bedID", jsonIn.getInt("bedID"));
        jsonObject.put("num", jsonIn.getInt("num"));
        jsonObject.put("userID", jsonIn.getInt("customerID"));
        jsonObject.put("herbID", jsonIn.getInt("herbID"));
        jsonObject.put("consumeID", jsonIn.getInt("serviceID"));

        jsonTemp = new JSONObject();
        jsonTemp.put("switchID", jsonIn.getJSONArray("hotSet").getJSONObject(0).getInt("switchID"));
        jsonTemp.put("state", jsonIn.getJSONArray("hotSet").getJSONObject(0).getInt("state"));
        hotSet.put(jsonTemp);

        jsonTemp = new JSONObject();
        jsonTemp.put("switchID", jsonIn.getJSONArray("hotSet").getJSONObject(1).getInt("switchID"));
        jsonTemp.put("state", jsonIn.getJSONArray("hotSet").getJSONObject(1).getInt("state"));
        hotSet.put(jsonTemp);

        jsonTemp = new JSONObject();
        jsonTemp.put("switchID", jsonIn.getJSONArray("fireSet").getJSONObject(0).getInt("switchID"));
        jsonTemp.put("state", jsonIn.getJSONArray("fireSet").getJSONObject(0).getInt("state"));
        fireSet.put(jsonTemp);

        jsonObject.put("hotSet", hotSet);
        jsonObject.put("fireSet", fireSet);

        return jsonObject;

    }

    public static class BedInfo {
        public int bedID, boardID, padID, storeID;
        public String bedType, bedSpecification, companyAddr, companyIntro, companyWeixin, storeTel, storeAddr, storeName;
        public long companyQQ, companyTel, bedProduceTime, workNum, workTotalTime;
        public double boardVer, padVer;

        public BedInfo(@NonNull JSONObject jsonData) throws JSONException {

            this.bedID = jsonData.getInt("bedID");
            this.boardID = jsonData.getInt("boardID");
            this.boardVer = jsonData.getDouble("boardVer");
            this.companyQQ = jsonData.getLong("companyQQ");
            this.companyTel = jsonData.getLong("companyTel");
            this.padID = jsonData.getInt("padID");
            this.padVer = jsonData.getDouble("padVer");
            this.bedProduceTime = jsonData.getLong("produceTime");
            this.storeID = jsonData.getInt("storeID");
            this.workNum = jsonData.getLong("workNum");
            this.workTotalTime = jsonData.getLong("workTotalTime");
            this.bedType = jsonData.getString("bedType");
            this.companyAddr = jsonData.getString("companyAddr");
            this.companyIntro = jsonData.getString("companyIntro");
            this.companyWeixin = jsonData.getString("companyWeixin");
            this.storeTel = jsonData.getString("serverTel");
            this.bedSpecification = jsonData.getString("specification");
            this.storeAddr = jsonData.getString("storeAddr");
            this.storeName = jsonData.getString("storeName");
        }
    }


    public static class MonitorInfo {
        public boolean isWork;
        public int degreeBack, degreeFore, heartRate, posMainMotor, stateIgniteFL, stateIgniteFR, stateIgniteBL, stateIgniteBR, stateHeatFL, stateHeatFR, stateHeatBL, stateHeatBR, stateWind, stateMainMotor, customerAge;
        public long startTime, currentTime;
        public float bodyTemperature;
        public String customerName, customerSex;

        public MonitorInfo(@NonNull JSONObject jsonData) throws JSONException {

            this.isWork = !(0 == jsonData.getInt("isWork"));

            this.degreeBack = jsonData.getInt("degreeBack");

            this.degreeFore = jsonData.getInt("degreeFore");

            this.posMainMotor = jsonData.getInt("posMainMotor");

            this.stateIgniteBL = jsonData.getInt("stateDianBackLeft");
            this.stateIgniteBR = jsonData.getInt("stateDianBackRight");
            this.stateIgniteFL = jsonData.getInt("stateDianForeLeft");
            this.stateIgniteFR = jsonData.getInt("stateDianForeRight");

            this.stateHeatBL = jsonData.getInt("stateHotBackLeft");
            this.stateHeatBR = jsonData.getInt("stateHotBackRight");
            this.stateHeatFL = jsonData.getInt("stateHotForeLeft");
            this.stateHeatFR = jsonData.getInt("stateHotForeRight");

            this.currentTime = jsonData.getLong("currentTime");
            this.startTime = jsonData.getLong("startTime");

            this.stateWind = jsonData.getInt("stateWind");
            this.stateMainMotor = jsonData.getInt("stateMainMotor");

            this.customerName = jsonData.getString("userName");
            this.customerSex = jsonData.getString("userSex");
            this.customerAge = jsonData.getInt("userAge");

            this.bodyTemperature = (float) jsonData.getDouble("degreeBody");
            this.heartRate = jsonData.getInt("numHeart");
        }
    }

    public static class CheckoutInfo {

        public String CustomerInfo, bedName, rawType, serviceType;
        public long startTime, workTime;
        public double rawPrice, servicePrice, totalPrice;

        public CheckoutInfo(@NonNull JSONObject jsonData) throws JSONException {

            this.CustomerInfo = jsonData.getString("userInfo");
            this.bedName = jsonData.getString("bedName");
            this.startTime = jsonData.getLong("startTime");
            this.workTime = jsonData.getLong("workTime");
            this.rawType = jsonData.getString("productName");
            this.rawPrice = jsonData.getDouble("productMoney");
            this.serviceType = jsonData.getString("serveItem");
            this.servicePrice = jsonData.getDouble("serveMoney");
            this.totalPrice = jsonData.getDouble("totalMoney");

        }
    }

}
