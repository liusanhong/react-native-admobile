package com.reactnativeadmobile;

/**
 * describe:
 */
public class AdCallbackUtils {
    private static AdCallback mCallBack;

    public static void setCallBack(AdCallback callBack) {
        mCallBack = callBack;
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
