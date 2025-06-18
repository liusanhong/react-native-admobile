// ReactNativeAdmobileModule.java

package com.reactnativeadmobile;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;

import cn.admobiletop.adsuyi.ADSuyiSdk;
import cn.admobiletop.adsuyi.ad.ADSuyiInterstitialAd;
import cn.admobiletop.adsuyi.ad.ADSuyiRewardVodAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiInterstitialAdInfo;
import cn.admobiletop.adsuyi.ad.data.ADSuyiRewardVodAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiInterstitialAdListener;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiRewardVodAdListener;
import cn.admobiletop.adsuyi.config.ADSuyiInitConfig;
import cn.admobiletop.adsuyi.listener.ADSuyiInitListener;
import cn.admobiletop.adsuyi.util.ADSuyiAdUtil;

public class ReactNativeAdmobileModule extends ReactContextBaseJavaModule implements AdCallback {

    private final ReactApplicationContext reactContext;
    private String TAG = "AdmobileModule";
    private Callback mSplashSuccess;
    private Callback mSplashError;

    /**
     * 激励视频广告
     */
    private ADSuyiRewardVodAd mRewardVodAd;
    /**
     * 激励视频info对象
     */
    private ADSuyiRewardVodAdInfo mRewardVodAdInfo;

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
    public void initAd(String appID, Promise promise) {
        if (this.reactContext != null) {
            AdCallbackUtils.setCallBack(this);

            ReactApplicationContext context = this.reactContext;
            reactContext.runOnUiQueueThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("initAd::", appID);
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
                            .isCanUseWifiState(false)
                            // 是否可获取定位数据
                            .isCanUseLocation(false)
                            //是否可读取设备列表
                            .isCanReadInstallList(false)
                            // 是否可获取设备信息
                            .isCanUsePhoneState(false)
                            // 是否过滤第三方平台的问题广告（例如: 已知某个广告平台在某些机型的Banner广告可能存在问题，如果开启过滤，则在该机型将不再去获取该平台的Banner广告）
                            .filterThirdQuestion(true)
                            // 注意：如果使用oaid1.0.26版本，需要在assets中放置密钥，并将密钥传入ADSuyi（suyi内部初始化oaid需要使用）
                            // 密钥需要到移动安全联盟申请（非oaid1.0.26版本无需使用该接口）
//                .setOaidCertPath("cn.admobiletop.adsuyidemo.cert.pem")
                            .build(), new ADSuyiInitListener() {
                        @Override
                        public void onSuccess() {
                            Log.e("initAd:", "onSuccess");
                            promise.resolve("success");
                        }

                        @Override
                        public void onFailed(String s) {
                            Log.e("initAd: onFailed:", s);
                            promise.reject("failed", s);
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
    public void splashAd(String adId, Callback successCallback, Callback errorCallback) {
        if (this.reactContext != null) {
            this.mSplashError = errorCallback;
            this.mSplashSuccess = successCallback;

            Intent intent = new Intent(this.reactContext, SplashAdActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("adId", adId);
            this.reactContext.startActivity(intent);
        }
    }

    @ReactMethod
    public void intertitialAd(String adId, Callback successCallback, Callback errorCallback) {
        if (this.reactContext != null) {
            reactContext.runOnUiQueueThread(new Runnable() {
                @Override
                public void run() {
                    ADSuyiInterstitialAd interstitialAd = new ADSuyiInterstitialAd(reactContext.getCurrentActivity());
// 创建额外参数实例
                    ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                            // 设置视频类广告是否静音（部分渠道支持）
                            .setVideoWithMute(false)
                            .build();
                    interstitialAd.setLocalExtraParams(extraParams);

                    // 设置插屏广告监听
                    interstitialAd.setListener(new ADSuyiInterstitialAdListener() {

                        @Override
                        public void onAdReceive(ADSuyiInterstitialAdInfo interstitialAdInfo) {
                            // 插屏广告对象一次成功拉取的广告数据只允许展示一次
//                    InterstitialAdActivity.this.interstitialAdInfo = interstitialAdInfo;
                            Log.d(TAG, "onAdReceive----->");
                            Log.d(TAG, "广告获取成功回调... ");
                        }

                        @Override
                        public void onAdReady(ADSuyiInterstitialAdInfo interstitialAdInfo) {
                            // 建议在该回调之后展示广告
                            Log.d(TAG, "onAdReady----->");
                            Log.d(TAG, "广告准备完毕回调... ");
                            // 插屏的展示，由于插屏的获取是异步的，请在onAdReceive后调用该方法对插屏进行展示
                            ADSuyiAdUtil.showInterstitialAdConvenient(reactContext.getCurrentActivity(), interstitialAdInfo);
                        }

                        @Override
                        public void onAdExpose(ADSuyiInterstitialAdInfo interstitialAdInfo) {
                            Log.d(TAG, "onAdExpose----->");
                            Log.d(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败");
                        }

                        @Override
                        public void onAdClick(ADSuyiInterstitialAdInfo interstitialAdInfo) {
                            Log.d(TAG, "onAdClick----->");
                            Log.d(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
                        }

                        @Override
                        public void onAdClose(ADSuyiInterstitialAdInfo interstitialAdInfo) {
                            Log.d(TAG, "onAdClose----->");
                            Log.d(TAG, "广告点击关闭回调");
                            successCallback.invoke("success");
                        }

                        @Override
                        public void onAdFailed(ADSuyiError adSuyiError) {
                            if (adSuyiError != null) {
                                String failedJson = adSuyiError.toString();
                                Log.d(TAG, "onAdFailed----->" + failedJson);
                            }
                        }
                    });
                    interstitialAd.loadAd(adId);
                }
            });

        }
    }

    /**
     * 激励广告new
     */
    @ReactMethod
    public void loadRewardAd(String adId) {
        Log.e(TAG, "loadRewardAd----->"+adId);

        runOnUiThread(
                () -> {
                    if (reactContext != null) {
                        releaseReward();
                        Log.e(TAG, "loadRewardAd----->");
                        mRewardVodAd = new ADSuyiRewardVodAd(reactContext.getCurrentActivity());
                        // 创建额外参数实例
                        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                                // 设置视频类广告是否静音（部分渠道支持）
                                .setVideoWithMute(true)
                                .build();
                        mRewardVodAd.setLocalExtraParams(extraParams);
//                        mRewardVodAd.setOnlySupportPlatform(Constant.ONLY_SUPPORT_PLATFORM);
//                        mRewardVodAd.setOnlySupportPlatform(Constant.ONLY_SUPPORT_PLATFORM);

                        // 设置插屏广告监听
                        mRewardVodAd.setListener(new ADSuyiRewardVodAdListener() {

                            @Override
                            public void onVideoCache(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                Log.e(TAG, "onVideoCache");
                            }

                            @Override
                            public void onVideoComplete(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                Log.e(TAG, "onVideoComplete");
                            }

                            @Override
                            public void onVideoError(ADSuyiRewardVodAdInfo rewardVodAdInfo, ADSuyiError adSuyiError) {
                                Log.e(TAG, "onVideoError");
                                onSendRewardEvent("onVideoError", rewardVodAdInfo);
                            }

                            @Override
                            public void onReward(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                Log.e(TAG, "广告激励获得成功回调...::::: ");
                                onSendRewardEvent("onReward", rewardVodAdInfo);
                            }

                            @Override
                            public void onAdReceive(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                mRewardVodAdInfo = rewardVodAdInfo;
                                // 插屏广告对象一次成功拉取的广告 数据只允许展示一次
                                Log.e(TAG, "广告获取成功回调...::::: ");
                                showRewardAd();
                                onSendRewardEvent("onAdReceive", rewardVodAdInfo);
                            }

                            @Override
                            public void onAdExpose(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                Log.e(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败");
                                onSendRewardEvent("onRewardExposed", rewardVodAdInfo);
                            }

                            @Override
                            public void onAdClick(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                Log.e(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
                                onSendRewardEvent("onRewardClicked", rewardVodAdInfo);
                            }

                            @Override
                            public void onAdClose(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                                releaseReward();
                                Log.e(TAG, "广告点击关闭回调");
                                onSendRewardEvent("onAdClose", rewardVodAdInfo);
//                                sendEvent("onRewardClosed",  rewardVodAdInfo);
                            }

                            @Override
                            public void onAdFailed(ADSuyiError adSuyiError) {
                                releaseReward();
                                if (adSuyiError != null) {
                                    String failedJson = adSuyiError.toString();
                                    Log.e(TAG, "广告获取失败：" + failedJson);

                                    WritableMap resultMap = Arguments.createMap();
                                    resultMap.putInt("errorCode", adSuyiError.getCode());
                                    resultMap.putString("errorDescription", adSuyiError.getError());

                                    onSendRewardEvent("onAdFailed",  resultMap);
//                                    sendEvent("onRewardFailed",  resultMap);
                                }
                            }
                        });
                        mRewardVodAd.loadAd(adId);
                    }
                }
        );
    }

    @ReactMethod
    public void showRewardAd() {
        if (reactContext == null) {
            WritableMap resultMap = Arguments.createMap();
            resultMap.putInt("errorCode", -1);
            resultMap.putString("errorDescription", "mReactContext对象为空");
//            SendEventManager.getInstance().sendAdEvent("onRewardFailed", null, resultMap);
            return;
        }

        if (mRewardVodAdInfo == null) {
            WritableMap resultMap = Arguments.createMap();
            resultMap.putInt("errorCode", -1);
            resultMap.putString("errorDescription", "激励视频广告对象为空");
//            SendEventManager.getInstance().sendAdEvent("onRewardFailed", null, resultMap);
            return;
        }

        mRewardVodAdInfo.showRewardVod(reactContext.getCurrentActivity());
    }

    public void releaseReward() {
        if (mRewardVodAd != null) {
            mRewardVodAd.release();
            mRewardVodAd = null;
        }

        if (mRewardVodAdInfo != null) {
            mRewardVodAdInfo.release();
            mRewardVodAdInfo = null;
        }
    }


    /**
     * @param personalizedAdEnabled
     */
    @ReactMethod
    public void setPersonalizedAdEnabled(boolean personalizedAdEnabled) {
        if (this.reactContext != null) {
            ADSuyiSdk.setPersonalizedAdEnabled(personalizedAdEnabled);
        }
    }

    @Override
    public void splashSuccessCallback() {
        if (this.mSplashSuccess != null) {
            this.mSplashSuccess.invoke("success");
        }
    }

    @Override
    public void splashErrorCallback() {
        if (this.mSplashError != null) {
            this.mSplashError.invoke("error");
        }
    }


    private WritableMap convertRewardVodAdInfoToMap(ADSuyiRewardVodAdInfo adInfo) {
        WritableMap map = Arguments.createMap();
        if (adInfo != null) {
            // 添加基础信息
            map.putString("adType", "rewardVod");

            // 添加奖励信息
            Map<String, Object> rewardMap = adInfo.getRewardMap();
            if (rewardMap != null) {
                WritableMap rewardWritableMap = Arguments.createMap();
                for (Map.Entry<String, Object> entry : rewardMap.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        rewardWritableMap.putString(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Integer) {
                        rewardWritableMap.putInt(entry.getKey(), (Integer) entry.getValue());
                    } else if (entry.getValue() instanceof Boolean) {
                        rewardWritableMap.putBoolean(entry.getKey(), (Boolean) entry.getValue());
                    }
                    // 可以根据需要添加其他类型
                }
                map.putMap("reward", rewardWritableMap);
            }
        }
        return map;
    }

    private void onSendRewardEvent(String type, ADSuyiRewardVodAdInfo adInfo) {
        WritableMap params = convertRewardVodAdInfoToMap(adInfo);
        params.putString("eventType", type);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RewardAdEvent", params);
    }
    private void onSendRewardEvent(String type, WritableMap map) {
//        WritableMap params = convertRewardVodAdInfoToMap(adInfo);
        map.putString("eventType", type);
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit("RewardAdEvent", map);
    }
}
