package com.reactnativeadmobile;

/**
 * describe:
 */
public class AdCallbackUtils {
    private static AdCallback mCallBack;

    public static void setCallBack(AdCallback callBack) {
        mCallBack = callBack;
    }

    public static void doRewordErrorCallback(){
//        String info = "这里CallBackUtils即将发送的数据。";
        mCallBack.rewordErrorCallback();
    }
    public static void doRewordSuccessCallback(){
//        String info = "这里CallBackUtils即将发送的数据。";
        mCallBack.rewordSuccessCallback();
    }

}
