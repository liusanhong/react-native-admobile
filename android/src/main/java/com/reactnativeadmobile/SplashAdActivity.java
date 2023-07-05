package com.reactnativeadmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
//import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import cn.admobiletop.adsuyi.ad.ADSuyiSplashAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiAdSize;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiSplashAdListener;

public class SplashAdActivity extends AppCompatActivity {

    ADSuyiSplashAd adSuyiSplashAd;
    String TAG = "SplashAdActivity";
    Activity mContext ;
    String mAdId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);
        mContext= this;
        mAdId = getIntent().getStringExtra("adId");

        LinearLayoutCompat contentView = findViewById(R.id.splash_container);
        // 创建开屏广告实例，第一个参数可以是Activity或Fragment，第二个参数是广告容器
        adSuyiSplashAd = new ADSuyiSplashAd(this, contentView);

// 底部logo容器高度，请根据实际情况进行计算
//        TODO
        int logoHeight = 0;
// 屏幕宽度px
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
// 屏幕高度px
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
// 创建额外参数实例
        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                // 设置整个广告视图预期宽高(目前仅头条平台需要，没有接入头条可不设置)，单位为px，如果不设置头条开屏广告视图将会以9 : 16的比例进行填充，小屏幕手机可能会出现素材被压缩的情况
                .adSize(new ADSuyiAdSize(widthPixels, heightPixels - logoHeight))
                .build();
// 如果开屏容器不是全屏可以设置额外参数
        adSuyiSplashAd.setLocalExtraParams(extraParams);

// 设置是否是沉浸式，如果为true，跳过按钮距离顶部的高度会加上状态栏高度
        adSuyiSplashAd.setImmersive(false);

// 设置自定义跳过按钮和倒计时时长（非必传，倒计时时长范围[3000,5000]建议不要传入倒计时时长） 目前不支持inmobi, ksad, oneway, ifly平台自定义跳过按钮
// 注意不要隐藏跳过按钮，可以在布局中将跳过按钮alpha设置为0，在onAdReceive回调中将alpha设置为1
//        TODO
//        adSuyiSplashAd.setSkipView(skipView, 5000);

// 设置开屏广告监听
        adSuyiSplashAd.setListener(new ADSuyiSplashAdListener() {

            @Override
            public void onADTick(long countdownSeconds) {
                // 如果没有设置自定义跳过按钮不会回调该方法（单位为秒）
                Log.e(TAG, "倒计时剩余时长（单位秒）" + countdownSeconds);
            }

            @Override
            public void onReward(ADSuyiAdInfo adSuyiAdInfo) {
                // 目前仅仅优量汇渠道会被使用
                Log.d(TAG, "广告奖励回调... ");
            }

            @Override
            public void onAdSkip(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告跳过回调，不一定准确，埋点数据仅供参考... ");
            }
            @Override
            public void onAdReceive(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告获取成功回调... ");
            }

            @Override
            public void onAdExpose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败");
            }

            @Override
            public void onAdClick(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
//                 AdCallbackUtils.doSplashSuccessCallback();
            }

            @Override
            public void onAdClose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告关闭回调，需要在此进行页面跳转");
//                jumpMain();
                mContext.finish();
            }

            @Override
            public void onAdFailed(ADSuyiError adSuyiError) {
                if (adSuyiError != null) {
                    String failedJson = adSuyiError.toString();
                    Log.e(TAG, "onAdFailed----->" + failedJson);
//                     AdCallbackUtils.doSplashErrorCallback();

                }
//                jumpMain();
                mContext.finish();
            }
        });

//        adSuyiSplashAd.loadAd("377b03ea4dff47bda1");
        adSuyiSplashAd.loadAd(mAdId);
    }
}
