package uestc.arbc.background;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * manage database used in this APP
 * Created by CK on 2016/11/10.
 */

public class DataSQL {
    private final static String TAG = "DataSQL";

    private final static int JSON_SIZE = 2048;//定义数据库表中用于储存JSONObject的varchar大小
    private SQLiteDatabase db = null;

    //构造函数，打开数据库，如果local或cloud表不存在则创建
    DataSQL() {
        try {

            db = SQLiteDatabase.openOrCreateDatabase(ManageApplication.getInstance().getFilesDir().getPath()+"/arbc.db", null);
            L.i(TAG, "Database path:" + ManageApplication.getInstance().getFilesDir().getPath() + "/arbc.db");
            /*if (null != db) {

                if (!isTableExists("Local")) {
                    createJsonTable("Local");
                }
                if (!isTableExists("Cloud")) {
                    createJsonTable("Cloud");
                }

            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
        L.i(TAG, "initialed");
    }

    //检测数据库打开是否成功，return true/false
    boolean isStartSucceed() {
        return (null != db);
    }

    //检测表是否存在，return true/false，注意当数据库打开失败时也返回false
    public synchronized boolean isTableExists(final String table) {
        if (null == db||null == table||table.equals("")) {
            return false;
        }
        String s = "select count(*) as c from sqlite_master where type ='table' and name ='" + table + "';";
        Cursor cursor = db.rawQuery(s, null);
        boolean result = false;
        if (cursor.moveToNext()) {
            result = (cursor.getInt(0) > 0);
        }
        cursor.close();
        return result;
    }

    //创建表 示例 createTable("cloud","(id integer,data varchar)") 出错时返回false
    private synchronized boolean createTable(String table, String tableFormat) {
        if (null == db || isTableExists(table)) {
            return false;
        }
        //创建表SQL语句
        String s = "create table " + table + tableFormat;

        try {
            //执行SQL语句
            db.execSQL(s);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //创建本APP所用的cloud或local表 示例 createTable("cloud")，出错返回false
    public synchronized boolean createJsonTable(String table) {
        return createTable(table,"(json varchar(" + JSON_SIZE + "))");
    }

    public synchronized boolean deleteTable(String table) {
        if (!isTableExists(table)) {
            return false;
        }

        //SQL语句
        String s = "drop table " + table;
        try {
            //执行SQL语句
            db.execSQL(s);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    //检测表是否为空 空返回true
    public synchronized boolean isTableEmpty (String table) {
        if (!isTableExists(table)) {
            return true;
        }

        try {
            Cursor cursor = db.query(table, null, null, null, null, null, null);
            if (cursor.getCount() == 0) {
                return true;
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }

        return false;
    }

    //在表的末尾插入数据 示例 insert("cloud","('string add single quote mark')")
    private synchronized boolean insert(String table, String values) {
        if (!isTableExists(table)) {
            return false;
        }

        String s = "insert into " + table + " values" + values;

        try {
            db.execSQL(s);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //往表末尾插入数据，第二个参数为String型
    private synchronized boolean pushString(String table, String json) {
        return insert(table, "('" + json + "')");
    }


    //往cloud 或local 表末尾插入数据，第二个参数为JSONObject型
    public synchronized boolean pushJson(String table, JSONObject json) {
        JSONObject emptyJSONObject = new JSONObject();
        if (!isTableExists(table)||json.equals(emptyJSONObject)) {
            return false;
        }

        String jsonString = json.toString();
        return pushString(table,jsonString);
    }

    //从表头获取数据，返回值为String型
    private synchronized String getString(String table) {
        if (!isTableExists(table)) {
            return null;
        }
        Cursor cursor;
        String jsonString;
        try {
            cursor = db.query(table, null, null, null, null, null, null);
            /*Log.d(TAG, "table " + table + " is:");
            while (cursor.moveToNext()) {
                L.d(TAG, cursor.getString(0));
            }*/
            cursor.moveToFirst();
            jsonString = cursor.getString(0);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        cursor.close();
        return jsonString;
    }

    //从表头读取数据，返回值为JSONObject型
    public synchronized JSONObject getJson (String table) {
        String jsonString = getString(table);
        if (null == jsonString) {
            return null;
        }

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

    }

    //关闭数据库
    void close() {
        try {
            db.close();
            db = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
