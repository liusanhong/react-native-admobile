package com.reactnativeadmobile.views;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativeadmobile.R;
import com.reactnativeadmobile.utils.Utils;

import cn.admobiletop.adsuyi.ad.ADSuyiBannerAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiAdInfo;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiBannerAdListener;

/**
 * describe:
 */
public class RNBannerView extends LinearLayout {
    private final RNBannerView instance;
    protected ReactContext reactContext;
    protected Context mContext;
    private String _codeId = null;
    private String TAG = "RNBannerView";
    ADSuyiBannerAd suyiBannerAd;




    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isInterceptScroll()) {
                    // 拦截父布局的事件，这样能够触发穿山甲的滑动点击事件，但视频类素材无法触发
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public RNBannerView(ReactContext context) {
        super(context);
        reactContext = context;
        mContext = context;
        instance = this;
        // 初始化广告渲染组件
        inflate(mContext, R.layout.layout_ad_banner, this);
//        mContainer = findViewById(R.id.tt_video_layout_hxb);
// 这个函数很关键，不然不能触发再次渲染，让 view 在 RN 里渲染成功!!
        Utils.setupLayoutHack(this);
    }

    public void setCodeId(String codeId) {
        _codeId = codeId;
        runOnUiThread(
                () -> {
                    tryShowAd(codeId);
                }
        );
    }

    void tryShowAd(String codeId) {
        if (_codeId == null) {
            Log.d(TAG, "loadBannerAd: 属性还不完整 _codeId=" + _codeId);
            return;
        }
        if(suyiBannerAd!=null){
            suyiBannerAd.release();
        }

        // 创建Banner广告实例，第一个参数可以是Activity或Fragment，第二个参数是广告容器（请保证容器不会拦截点击、触摸等事件）
        suyiBannerAd = new ADSuyiBannerAd(reactContext.getCurrentActivity(), this);

// 设置自刷新时间间隔，0为不自动刷新，其他取值范围为[30,120]，单位秒
        suyiBannerAd.setAutoRefreshInterval(30);


        // 设置Banner广告监听
        suyiBannerAd.setListener(new ADSuyiBannerAdListener() {
            @Override
            public void onAdReceive(ADSuyiAdInfo adSuyiAdInfo) {
                Log.d(TAG, "广告获取成功回调... ");
            }

            @Override
            public void onAdExpose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.d(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败");
                WritableMap params = Arguments.createMap();
                params.putString("result", "success");
                sendEvent(reactContext, "bannerViewDidReceived", params);
            }

            @Override
            public void onAdClick(ADSuyiAdInfo adSuyiAdInfo) {
                Log.d(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
                if(suyiBannerAd!=null){
//                    instance.destroyDrawingCache();
                    suyiBannerAd.setAutoRefreshInterval(30);
                }
            }

            @Override
            public void onAdClose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.d(TAG, "广告关闭回调");
            }

            @Override
            public void onAdFailed(ADSuyiError adSuyiError) {
                if (adSuyiError != null) {
                    String failedJson = adSuyiError.toString();
                    Log.d(TAG, "onAdFailed----->" + failedJson);
                    WritableMap params = Arguments.createMap();
                    params.putString("result", "failed");
                    sendEvent(reactContext, "bannerViewFailAction", params);
                }
//                if(suyiBannerAd!=null){
////                    instance.destroyDrawingCache();
//                    suyiBannerAd.setAutoRefreshInterval(0);
//                }
            }
        });

        // banner广告场景id（场景id非必选字段，如果需要可到开发者后台创建）
//        suyiBannerAd.setSceneId(ADSuyiDemoConstant.BANNER_AD_SCENE_ID);
// 加载Banner广告，参数为广告位ID，同一个ADSuyiBannerAd只有一次loadAd有效
        suyiBannerAd.loadAd(codeId);
    }

    private boolean isInterceptScroll() {
        return true;
    }

}
