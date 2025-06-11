package com.reactnativeadmobile.utils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;

import cn.admobiletop.adsuyi.ad.data.ADSuyiRewardVodAdInfo;

public class SendEventManager {
    private static SendEventManager instance;
    private ReactApplicationContext reactContext;

    private SendEventManager(ReactApplicationContext reactContext) {
        this.reactContext = reactContext;
    }

    public static synchronized SendEventManager getInstance(ReactApplicationContext reactContext) {
        if (instance == null) {
            instance = new SendEventManager(reactContext);
        }
        return instance;
    }

    public void sendAdEvent(String eventName, WritableMap params) {
        if (reactContext != null && reactContext.hasActiveReactInstance()) {
            reactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    public void sendAdEvent(String eventName, Object adInfo, WritableMap params) {
        WritableMap eventParams = Arguments.createMap();
        if (adInfo != null) {
            // 这里可以添加adInfo到eventParams的逻辑
        }
        if (params != null) {
            eventParams.merge(params);
        }
        sendAdEvent(eventName, eventParams);
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

    // 添加直接发送ADSuyiRewardVodAdInfo的方法
    public void sendAdEvent(String eventName, ADSuyiRewardVodAdInfo adInfo) {
        WritableMap params = convertRewardVodAdInfoToMap(adInfo);
        sendAdEvent(eventName, params);
    }
}