package com.reactnativeadmobile.views;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactnativeadmobile.R;
import com.reactnativeadmobile.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.admobiletop.adsuyi.ad.ADSuyiNativeAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiAdInfo;
import cn.admobiletop.adsuyi.ad.data.ADSuyiNativeAdInfo;
import cn.admobiletop.adsuyi.ad.data.ADSuyiNativeExpressAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiAdSize;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiNativeAdListener;
import cn.admobiletop.adsuyi.util.ADSuyiAdUtil;
import cn.admobiletop.adsuyi.util.ADSuyiDisplayUtil;
import cn.admobiletop.adsuyi.util.ADSuyiViewUtil;

/**
 * describe:
 */
public class RNFlowExpressAdView extends LinearLayout {
    protected ReactContext reactContext;
    protected Context mContext;
    private RNFlowExpressAdView mCurrent;
    private String _codeId = null;
    private int mAdWidthPx;
    private int mAdHeightPx;
    private String TAG = "RNFlowExpressAdView";
    ADSuyiNativeAd nativeAd;
    private ADSuyiNativeAdInfo adSuyiNativeAdInfo;

    private View nativeExpressAdView;

    /**
     * 通过 RCTEventEmitter 按 view ID 定向发送事件到 JS 端（非全局广播）
     */
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        ReactContext ctx = (ReactContext) getContext();
        ctx.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), eventName, params);
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

    public RNFlowExpressAdView(ReactContext context) {
        super(context);
        reactContext = context;
        mContext = context;
        mCurrent = this;
        // 初始化广告渲染组件
        inflate(mContext, R.layout.layout_ad_banner, this);
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

    public void setAdWidth(int widthPx) {
        mAdWidthPx = widthPx;
    }

    public void setAdHeight(int heightPx) {
        mAdHeightPx = heightPx;
    }

    void tryShowAd(String codeId) {
        if (_codeId == null) {
            Log.d(TAG, "loadFlowExpressAd: 属性还不完整 _codeId=" + _codeId);
            return;
        }
        Log.d(TAG, "codeId:" + codeId);

        // 创建信息流广告实例
        nativeAd = new ADSuyiNativeAd(reactContext.getCurrentActivity());

        if (mAdWidthPx == 0) {
            mAdWidthPx = getResources().getDisplayMetrics().widthPixels;
        }

        Log.d("adDisplay", "load ad width : " + mAdWidthPx);

        Map<String, Object> map = new HashMap<>();
        map.put("isShowKsCloseView", true);

// 创建额外参数实例
        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                // 设置整个广告视图预期宽高(目前仅头条，艾狄墨搏平台需要，没有接入头条、艾狄墨搏可不设置)，单位为px，高度如果小于等于0则高度自适应
                .adSize(new ADSuyiAdSize(mAdWidthPx, 0))
                // 设置广告视图中MediaView的预期宽高(目前仅Inmobi平台需要,Inmobi的MediaView高度为自适应，没有接入Inmobi平台可不设置)，单位为px
                .nativeAdMediaViewSize(new ADSuyiAdSize((int) (mAdWidthPx - 24 * getResources().getDisplayMetrics().density)))
                // 设置信息流广告适配播放是否静音，默认静音，目前优量汇、百度、汇量、快手、Admobile支持修改
                .nativeAdPlayWithMute(true)
                .nativeExtraMap(map)
                .build();
// 设置一些额外参数，有些平台的广告可能需要传入一些额外参数，如果有接入头条、Inmobi平台，该参数必须设置
        nativeAd.setLocalExtraParams(extraParams);
        // 设置仅支持的广告平台，设置了这个值，获取广告时只会去获取该平台的广告，null或空字符串为不限制，默认为null，方便调试使用，上线时建议不设置
        nativeAd.setOnlySupportPlatform(null);

// 设置广告监听
        nativeAd.setListener(new ADSuyiNativeAdListener() {
            @Override
            public void onRenderFailed(ADSuyiNativeAdInfo adInfo, ADSuyiError error) {
                // 广告渲染失败，可在此回调中移除视图和释放广告对象
                Log.d(TAG, "onRenderFailed " + adInfo.toString());
                WritableMap params = Arguments.createMap();
                params.putInt("errorCode", error.getCode());
                params.putString("errorDescription", error.getError());
                sendEvent("topNativeFail", params);
            }

            /**
             * 广告渲染成功回调
             * @param adInfo
             */
            @Override
            public void onRenderSuccess(ADSuyiNativeAdInfo adInfo) {
                Log.d(TAG, "onRenderSuccess " + adInfo.toString());
                // 所有平台统一在渲染成功后回传高度，避免测量时机过早拿到 0
                rendAd();
            }

            @Override
            public void onAdReceive(List<ADSuyiNativeAdInfo> adInfos) {
                // 广告获取成功回调...
                Log.d(TAG, "onAdReceive" + adInfos.toString());
                adSuyiNativeAdInfo = adInfos.get(0);
                int sise = adInfos.size();
                Log.d(TAG, "onAdReceive sise::" + sise);
                showAd();

            }

            @Override
            public void onAdExpose(ADSuyiNativeAdInfo adInfo) {
                // 广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败
                Log.d(TAG, "onAdExpose" + adInfo.toString());
            }

            @Override
            public void onAdClick(ADSuyiNativeAdInfo adInfo) {
                // 广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败
                Log.d(TAG, "onAdClick" + adInfo.toString());

            }

            @Override
            public void onAdClose(ADSuyiNativeAdInfo adInfo) {
                // 广告关闭回调，可在此回调中移除视图和释放广告对象
                releaseAd();
                removeAllViews();
                Log.d(TAG, "onAdClose" + adInfo.toString());
                WritableMap params = Arguments.createMap();
                params.putString("result", "closed");
                sendEvent("topNativeClose", params);
            }

            @Override
            public void onAdFailed(ADSuyiError error) {
                // 广告获取失败回调...
                Log.d(TAG, "onAdFailed" + error.toString());

                WritableMap params = Arguments.createMap();
                params.putInt("errorCode", error.getCode());
                params.putString("errorDescription", error.getError());
                sendEvent("topNativeFail", params);

            }
        });

// 请求广告数据，参数一广告位ID，参数二请求数量[1,3]
        nativeAd.loadAd(codeId, 1);
    }

    /**
     * 展示广告
     */
    private void showAd() {
        if (ADSuyiAdUtil.adInfoIsRelease(adSuyiNativeAdInfo)) {
            Log.d(TAG, "广告已被释放");
            return;
        }
        if (adSuyiNativeAdInfo == null) {
            Log.d(TAG, "未获取到广告，请先请求广告");
            return;
        }

        ADSuyiNativeExpressAdInfo nativeExpressAdInfo;
        if (!adSuyiNativeAdInfo.isNativeExpress()) {
            Log.d(TAG, "当前请求到广告非信息流模板广告，请使用信息流模板广告位");
            return;
        } else {
            // 将广告对象转换成模板广告
            nativeExpressAdInfo = (ADSuyiNativeExpressAdInfo) adSuyiNativeAdInfo;
        }

        // 当前是信息流模板广告，getNativeExpressAdView获取的是整个模板广告视图
        nativeExpressAdView = nativeExpressAdInfo.getNativeExpressAdView(this);
        // 将广告视图添加到容器中的便捷方法
        ADSuyiViewUtil.addAdViewToAdContainer(
                this,
                nativeExpressAdView,
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );
        setBackgroundColor(Color.parseColor("#ffffff"));
        // 渲染广告视图, 必须调用, 因为是模板广告, 所以传入ViewGroup和响应点击的控件可能并没有用
        // 务必在最后调用
        nativeExpressAdInfo.render(this);

        // 高度回传统一在 onRenderSuccess 回调中处理，此处不再立即调用
    }

    /**
     * 渲染广告并回传高度信息
     */
    private void rendAd() {
        if (nativeExpressAdView == null) {
            return;
        }
        if (adSuyiNativeAdInfo == null) {
            return;
        }
        int[] size = unDisplayViewSize(nativeExpressAdView);

        int adHeightPx = size[1] > 0 ? size[1] : mAdHeightPx;

        WritableMap params = Arguments.createMap();
        params.putDouble("adWidth", ADSuyiDisplayUtil.px2dp(mAdWidthPx));
        if ("gdt".equals(adSuyiNativeAdInfo.getPlatform())) {
            if (adHeightPx < 143) {
                adHeightPx = 143;
                Log.d("adDisplay", "adHeightPx" + adHeightPx);
            }
        }
        params.putDouble("height", ADSuyiDisplayUtil.px2dp(adHeightPx));
        sendEvent("topNativeRenderSuccess", params);

        nativeExpressAdView.requestLayout();
        requestLayout();

        Utils.setupLayoutHack(this);
    }

    public int[] unDisplayViewSize(View view) {
        int size[] = new int[2];
        int width = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        size[0] = view.getMeasuredWidth();
        size[1] = view.getMeasuredHeight();

        Log.d("adDisplay", "android measuredWidth:" + size[0] + " measuredHeight:" + size[1]);
        return size;
    }

    private boolean isInterceptScroll() {
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void releaseAd() {
        if (nativeAd != null) {
            nativeAd.release();
            nativeAd = null;
        }
        if (adSuyiNativeAdInfo != null) {
            adSuyiNativeAdInfo.release();
            adSuyiNativeAdInfo = null;
        }
    }
}
