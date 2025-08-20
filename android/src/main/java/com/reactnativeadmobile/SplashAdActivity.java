package com.reactnativeadmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.admobiletop.adsuyi.ad.ADSuyiSplashAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiAdSize;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiSplashAdListener;
import android.view.WindowManager;

public class SplashAdActivity extends AppCompatActivity {

    ADSuyiSplashAd adSuyiSplashAd;
    String TAG = "SplashAdActivity";
    Activity mContext ;
    String mAdId ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_ad);
        Log.e(TAG,"进入广告:"+System.currentTimeMillis());

        setPortalOrientation();

        mContext= this;



        mAdId = getIntent().getStringExtra("adId");

        LinearLayoutCompat contentView = findViewById(R.id.splash_container);
        // 创建开屏广告实例，第一个参数可以是Activity或Fragment，第二个参数是广告容器
        adSuyiSplashAd = new ADSuyiSplashAd(this, contentView);

        // 屏幕宽度px
        float widthPixels = getResources().getDisplayMetrics().widthPixels;
        // 屏幕高度px
        float heightPixels = getResources().getDisplayMetrics().heightPixels;


        // 沉浸式显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | // 启用沉浸模式，避免系统栏意外出现
                            View.SYSTEM_UI_FLAG_FULLSCREEN |     // 隐藏状态栏
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |// 隐藏导航栏
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |  // 保持布局稳定
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | // 保证全屏显示
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION // 保证全屏时隐藏导航栏
            );
            List<Rect> exclusionRects = new ArrayList<>();
            Rect rect = new Rect(0,0, (int) widthPixels, (int) heightPixels);
            exclusionRects.add(rect);
            decorView.setSystemGestureExclusionRects(exclusionRects);
        }

        // 原始屏幕密度
        final float originalDensity = getRealScreenDensity(this);
        // 当前屏幕密度
        final float density = getResources().getDisplayMetrics().density;

        // 当屏幕密度变大时。宽高也要根据扩大倍率重新计算
        if (density > originalDensity) {
            widthPixels = widthPixels * (density / originalDensity);
            heightPixels = heightPixels * (density / originalDensity);
        }

        // 将162dp转换为px
        int bottomImageHeightPx = (int) (162 * density + 0.5f);

        // 创建额外参数实例
        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                // 设置整个广告视图预期宽高(目前仅头条平台需要，没有接入头条可不设置)，单位为px，如果不设置头条开屏广告视图将会以9 : 16的比例进行填充，小屏幕手机可能会出现素材被压缩的情况
                .adSize(new ADSuyiAdSize((int)widthPixels, (int)heightPixels-bottomImageHeightPx))
                .build();
        // 如果开屏容器不是全屏可以设置额外参数
        adSuyiSplashAd.setLocalExtraParams(extraParams);

        // 设置是否是沉浸式，如果为true，跳过按钮距离顶部的高度会加上状态栏高度
        adSuyiSplashAd.setImmersive(false);

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
//                Log.e(TAG,System.currentTimeMillis()+"");
                Log.e(TAG, "广告获取成功回调... "+System.currentTimeMillis());
            }

            @Override
            public void onAdExpose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败:"+System.currentTimeMillis());
            }

            @Override
            public void onAdClick(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
            }

            @Override
            public void onAdClose(ADSuyiAdInfo adSuyiAdInfo) {
                Log.e(TAG, "广告关闭回调，需要在此进行页面跳转");
                mContext.finish();
            }

            @Override
            public void onAdFailed(ADSuyiError adSuyiError) {
                if (adSuyiError != null) {
                    String failedJson = adSuyiError.toString();
                    Log.e(TAG, "onAdFailed----->" + failedJson);

                }
                mContext.finish();
            }
        });

        adSuyiSplashAd.loadAd(mAdId);
    }

    public static float getRealScreenDensity(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            DisplayMetrics realMetrics = new DisplayMetrics();
            try {
                Method getRealMetrics = Display.class.getMethod("getRealMetrics", DisplayMetrics.class);
                getRealMetrics.invoke(display, realMetrics);
                return realMetrics.density;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return -1f; // 返回-1表示获取失败
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private void setPortalOrientation(){
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
