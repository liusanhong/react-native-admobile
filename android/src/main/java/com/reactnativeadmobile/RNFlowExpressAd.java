package com.reactnativeadmobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativeadmobile.views.RNFlowExpressAdView;

import java.util.Map;

import cn.admobiletop.adsuyi.util.ADSuyiDisplayUtil;


/**
 * describe:
 */
public class RNFlowExpressAd extends SimpleViewManager<RNFlowExpressAdView> {
    ReactApplicationContext mCallerContext;
     String TAG = "RNFlowExpressAd";

    public RNFlowExpressAd(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
//        return "RNFlowExpressAd";
        return "RNNativeAD";
    }

    @NonNull
    @Override
    protected RNFlowExpressAdView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNFlowExpressAdView(reactContext);
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
    public void setPosId(RNFlowExpressAdView view, @Nullable String posId) {
        Log.e(TAG,"postID::"+posId);
        view.setCodeId(posId);
    }

    @ReactProp(name = "adWidth")
    public void setAdWidth(RNFlowExpressAdView view, int adWidth) {
        Log.d("adDisplay", "android adWidth px:" + ADSuyiDisplayUtil.dp2px(adWidth));
        view.setAdWidth(ADSuyiDisplayUtil.dp2px(adWidth));
    }

    @ReactProp(name = "adHeight")
    public void setAdHeight(RNFlowExpressAdView view, int adHeight) {
        Log.d("adDisplay", "android adHeight px:" + ADSuyiDisplayUtil.dp2px(adHeight));
        view.setAdHeight(ADSuyiDisplayUtil.dp2px(adHeight));
    }


}
