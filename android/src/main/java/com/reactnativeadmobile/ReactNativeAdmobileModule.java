// ReactNativeAdmobileModule.java

package com.reactnativeadmobile;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import cn.admobiletop.adsuyi.ADSuyiSdk;
import cn.admobiletop.adsuyi.config.ADSuyiInitConfig;
import cn.admobiletop.adsuyi.listener.ADSuyiInitListener;

public class ReactNativeAdmobileModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private String TAG = "AdmobileModule";

    public ReactNativeAdmobileModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "ReactNativeAdmobile";
    }

//    @ReactMethod
//    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
//        // TODO: Implement some actually useful functionality
//        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
//    }

    @ReactMethod
    public void initAd(String appID,Promise promise) {
        if(this.reactContext!= null){
            ReactApplicationContext context = this.reactContext;
            this.reactContext.runOnUiQueueThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("initAd::",appID);
                    // 初始化ADSuyi广告SDK
                    ADSuyiSdk.getInstance().init(context, new ADSuyiInitConfig.Builder()
                            // 设置APPID，必须的
                            .appId(appID)
                            // 是否开启Debug，开启会有详细的日志信息打印，如果用上ADSuyiToastUtil工具还会弹出toast提示。
                            // 注意上线后请置为false
                            .debug(BuildConfig.DEBUG)
                            // 是否同意隐私政策
                            .agreePrivacyStrategy(true)
                            // 是否同意使用oaid
                            .isCanUseOaid(true)
                            // 是否可读取wifi状态
                            .isCanUseWifiState(true)
                            // 是否可获取定位数据
                            .isCanUseLocation(true)
                            // 是否可获取设备信息
                            .isCanUsePhoneState(true)
                            // 是否过滤第三方平台的问题广告（例如: 已知某个广告平台在某些机型的Banner广告可能存在问题，如果开启过滤，则在该机型将不再去获取该平台的Banner广告）
                            .filterThirdQuestion(true)
                            // 注意：如果使用oaid1.0.26版本，需要在assets中放置密钥，并将密钥传入ADSuyi（suyi内部初始化oaid需要使用）
                            // 密钥需要到移动安全联盟申请（非oaid1.0.26版本无需使用该接口）
//                .setOaidCertPath("cn.admobiletop.adsuyidemo.cert.pem")
                            .build(), new ADSuyiInitListener() {
                        @Override
                        public void onSuccess() {
                            Log.e("initAd:","onSuccess");
                            promise.resolve("success");
                        }

                        @Override
                        public void onFailed(String s) {
                            Log.e("initAd: onFailed:",s);
                            promise.reject("failed",s);
                        }
                    });
                }
            });
        }
    }


    /**
     * 开屏广告
     */
    @ReactMethod
    public void splashAd() {
        if(this.reactContext!= null){
            Intent intent = new Intent(this.reactContext,SplashAdActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            this.reactContext.startActivity(intent);
        }
    }

    /**
     * 激励广告
     */
    @ReactMethod
    public void rewardVodAd() {
        if(this.reactContext!= null){
            Intent intent = new Intent(this.reactContext,RewardVodActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            this.reactContext.startActivity(intent);
        }
    }



}
