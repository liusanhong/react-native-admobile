package com.reactnativeadmobile;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativeadmobile.views.RNBannerView;
import com.reactnativeadmobile.views.RNFlowExpressAdView;

import cn.admobiletop.adsuyi.ad.ADSuyiBannerAd;


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

    @ReactProp(name = "posId")
    public void setPosId(RNFlowExpressAdView view, @Nullable String posId) {
        Log.e(TAG,"postID::"+posId);
        view.setCodeId(posId);
    }




}
