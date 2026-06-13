package com.reactnativeadmobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativeadmobile.views.RNNativeFeedAdView;

import java.util.Map;

import cn.admobiletop.adsuyi.util.ADSuyiDisplayUtil;

/**
 * 自渲染信息流广告 ViewManager
 */
public class RNNativeFeedAd extends SimpleViewManager<RNNativeFeedAdView> {
    ReactApplicationContext mCallerContext;
    String TAG = "RNNativeFeedAd";

    public RNNativeFeedAd(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNNativeFeedAD";
    }

    @NonNull
    @Override
    protected RNNativeFeedAdView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNNativeFeedAdView(reactContext);
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
                .put("topNativeRenderSuccess", MapBuilder.of("registrationName", "onNativeRenderSuccess"))
                .put("topNativeFail", MapBuilder.of("registrationName", "onNativeFail"))
                .put("topNativeClose", MapBuilder.of("registrationName", "onNativeClose"))
                .build();
    }

    @ReactProp(name = "posId")
    public void setPosId(RNNativeFeedAdView view, @Nullable String posId) {
        Log.d(TAG, "setPosId:" + posId);
        view.setPosId(posId);
    }

    @ReactProp(name = "adWidth")
    public void setAdWidth(RNNativeFeedAdView view, int adWidth) {
        Log.d(TAG, "setAdWidth dp:" + adWidth + " px:" + ADSuyiDisplayUtil.dp2px(adWidth));
        view.setAdWidth(ADSuyiDisplayUtil.dp2px(adWidth));
    }

    @ReactProp(name = "adHeight")
    public void setAdHeight(RNNativeFeedAdView view, int adHeight) {
        Log.d(TAG, "setAdHeight dp:" + adHeight + " px:" + ADSuyiDisplayUtil.dp2px(adHeight));
        view.setAdHeight(ADSuyiDisplayUtil.dp2px(adHeight));
    }

    @ReactProp(name = "maxMediaHeight")
    public void setMaxMediaHeight(RNNativeFeedAdView view, int maxMediaHeight) {
        Log.d(TAG, "setMaxMediaHeight dp:" + maxMediaHeight + " px:" + ADSuyiDisplayUtil.dp2px(maxMediaHeight));
        view.setMaxMediaHeight(ADSuyiDisplayUtil.dp2px(maxMediaHeight));
    }
}
