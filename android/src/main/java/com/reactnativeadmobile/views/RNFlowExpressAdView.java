package com.reactnativeadmobile.views;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.reactnativeadmobile.R;
import com.reactnativeadmobile.utils.Utils;
import com.tianmu.ad.NativeExpressAd;
import com.tianmu.ad.bean.NativeExpressAdInfo;
import com.tianmu.ad.entity.TianmuAdSize;
import com.tianmu.ad.error.TianmuError;
import com.tianmu.ad.listener.NativeExpressAdListener;

import java.util.List;


/**
 * describe:
 */
public class RNFlowExpressAdView extends LinearLayout {
    protected ReactContext reactContext;
    protected Context mContext;
    private String _codeId = null;
    private String TAG = "RNBannerView";
    NativeExpressAd nativeExpressAd;


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

    public RNFlowExpressAdView(ReactContext context) {
        super(context);
        reactContext = context;
        mContext = context;
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
            Log.d(TAG, "loadFlowExpressAd: 属性还不完整 _codeId=" + _codeId);
            return;
        }
        int widthPixels = getResources().getDisplayMetrics().widthPixels;

        NativeExpressAd nativeExpressAd = new NativeExpressAd(reactContext.getCurrentActivity(), new TianmuAdSize(widthPixels,0));
        nativeExpressAd.setListener(new NativeExpressAdListener() {
            @Override
            public void onRenderFailed(NativeExpressAdInfo nativeExpressAdInfo, TianmuError error) {
                // 广告渲染失败
            }

            @Override
            public void onAdReceive(List<NativeExpressAdInfo> nativeExpressAdInfos) {
                // 广告获取成功回调
                if (nativeExpressAdInfos == null || nativeExpressAdInfos.size() == 0) {
                    return;
                }
                NativeExpressAdInfo nativeExpressAdInfo = nativeExpressAdInfos.get(0);
                if (nativeExpressAdInfo != null) {
                    // 获取广告视图
                    View view = nativeExpressAdInfo.getNativeExpressAdView();

                    // 将广告视图添加到容器中
                    RecyclerView itemView = new RecyclerView(reactContext,null);

                    ((ViewGroup) itemView).addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    // 渲染广告，一定要最后调用
                    nativeExpressAdInfo.render();
                }
            }

            @Override
            public void onAdExpose(NativeExpressAdInfo nativeExpressAdInfo) {
                // 广告展示回调
            }

            @Override
            public void onAdClick(NativeExpressAdInfo nativeExpressAdInfo) {
                // 广告点击回调
            }

            @Override
            public void onAdClose(NativeExpressAdInfo nativeExpressAdInfo) {
                // 广告关闭回调
            }

            @Override
            public void onAdFailed(TianmuError error) {
                // 广告失败回调
            }
        });
        nativeExpressAd.loadAd(codeId);

    }

    private boolean isInterceptScroll() {
        return true;
    }
}
