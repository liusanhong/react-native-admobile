package com.reactnativeadmobile;

/**
 * describe:
 */
public class AdCallbackUtils {
    private static AdCallback mCallBack;

    public static void setCallBack(AdCallback callBack) {
        mCallBack = callBack;
    }

    public static void doRewordErrorCallback(String backStr){
//        String info = "这里CallBackUtils即将发送的数据。";
        if(mCallBack != null){
            mCallBack.rewordErrorCallback(backStr);
        }
    }

    public static void doRewordSuccessCallback(){
        if(mCallBack != null){
            mCallBack.rewordSuccessCallback();
        }
    }


    public static void doSplashErrorCallback(){
//        String info = "这里CallBackUtils即将发送的数据。";
        if(mCallBack!=null){
            mCallBack.splashErrorCallback();
        }
    }

    public static void doSplashSuccessCallback(){
        if(mCallBack != null){
            mCallBack.splashSuccessCallback();
        }
    }

}
