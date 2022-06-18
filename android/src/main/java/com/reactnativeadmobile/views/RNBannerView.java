package com.reactnativeadmobile.views;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.react.bridge.ReactContext;
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
    protected ReactContext reactContext;
    protected Context mContext;
    private String _codeId = null;
    private String TAG = "RNBannerView";
    ADSuyiBannerAd suyiBannerAd;


    public RNBannerView(ReactContext context) {
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
            Log.d(TAG, "loadBannerAd: 属性还不完整 _codeId=" + _codeId);
            return;
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
            }

            @Override
            public void onAdClick(ADSuyiAdInfo adSuyiAdInfo) {
                Log.d(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
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
                }
            }
        });

        // banner广告场景id（场景id非必选字段，如果需要可到开发者后台创建）
//        suyiBannerAd.setSceneId(ADSuyiDemoConstant.BANNER_AD_SCENE_ID);
// 加载Banner广告，参数为广告位ID，同一个ADSuyiBannerAd只有一次loadAd有效
        suyiBannerAd.loadAd(codeId);
    }
}
