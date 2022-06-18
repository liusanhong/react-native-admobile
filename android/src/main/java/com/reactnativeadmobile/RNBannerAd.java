package com.reactnativeadmobile;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.reactnativeadmobile.views.RNBannerView;

import cn.admobiletop.adsuyi.ad.ADSuyiBannerAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiAdInfo;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiBannerAdListener;


/**
 * describe:
 */
public class RNBannerAd extends SimpleViewManager<RNBannerView> {
    ReactApplicationContext mCallerContext;
     String TAG = "RNBannerAD";
    ADSuyiBannerAd suyiBannerAd;

    public RNBannerAd(ReactApplicationContext reactContext) {
        mCallerContext = reactContext;
    }

    @NonNull
    @Override
    public String getName() {
        return "RNBannerAD";
    }

    @NonNull
    @Override
    protected RNBannerView createViewInstance(@NonNull ThemedReactContext reactContext) {
        return new RNBannerView(reactContext);
    }

    @ReactProp(name = "posId")
    public void setPosId(RNBannerView view, @Nullable String posId) {
        Log.e(TAG,"postID::"+posId);
        view.setCodeId(posId);
    }




}
