package uestc.arbc.background;

import android.os.Handler;


/**
 * MyHandler
 * Created by CK on 2016/11/17.
 */

public class MyHandler extends Handler {
    private String handlerName = "NoName";

    public MyHandler(String handlerName) {
        if (null != handlerName) {
            this.handlerName = handlerName;
        }
    }

   public String getHandlerName() {
        return handlerName;
    }
}
