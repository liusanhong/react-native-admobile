package com.reactnativeadmobile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
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

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        // 沉浸式显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
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
                Method method = View.class.getMethod("setSystemGestureExclusionRects", List.class);
                method.invoke(decorView, exclusionRects);
            } catch (Exception e) {

            }
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

        // 动态获取底部图片高度
        int bottomImageHeightPx = getBottomImageHeightPx();
        
        // 动态调整图片高度，消除广告和底部图之间的空隙
        // 根据经验值，通常需要减去2-3像素来完全消除空隙
        int adjustedBottomImageHeightPx = Math.max(0, bottomImageHeightPx - 3);
        
        Log.d(TAG, "原始图片高度: " + bottomImageHeightPx + "px");
        Log.d(TAG, "调整后图片高度: " + adjustedBottomImageHeightPx + "px");
        Log.d(TAG, "广告高度: " + (heightPixels - adjustedBottomImageHeightPx) + "px");
        Log.d(TAG, "屏幕总高度: " + heightPixels + "px");
        Log.d(TAG, "调整像素数: " + (bottomImageHeightPx - adjustedBottomImageHeightPx) + "px");
        
        // 创建额外参数实例
        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                // 设置整个广告视图预期宽高(目前仅头条平台需要，没有接入头条可不设置)，单位为px，如果不设置头条开屏广告视图将会以9 : 16的比例进行填充，小屏幕手机可能会出现素材被压缩的情况
                .adSize(new ADSuyiAdSize((int)widthPixels, (int)heightPixels-adjustedBottomImageHeightPx))
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
        
        // 延迟检查布局，确保没有空隙
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndFixGap();
            }
        }, 100); // 100ms后检查
    }
    
    /**
     * 检查并修复广告和底部图之间的空隙
     */
    private void checkAndFixGap() {
        try {
            View splashContainer = findViewById(R.id.splash_container);
            View bottomImage = findViewById(R.id.bottom_image);
            
            if (splashContainer != null && bottomImage != null) {
                int containerBottom = splashContainer.getBottom();
                int imageTop = bottomImage.getTop();
                int gap = imageTop - containerBottom;
                
                Log.d(TAG, "容器底部位置: " + containerBottom + "px");
                Log.d(TAG, "图片顶部位置: " + imageTop + "px");
                Log.d(TAG, "检测到空隙: " + gap + "px");
                
                if (gap > 0) {
                    Log.w(TAG, "发现空隙，需要进一步调整布局");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "检查空隙失败: " + e.getMessage());
        }
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

    /**
     * 获取底部图片的实际高度（像素）
     * @return 底部图片高度（像素）
     */
    private int getBottomImageHeightPx() {
        try {
            // 获取屏幕密度
            final float density = getResources().getDisplayMetrics().density;
            
            // 获取屏幕宽度
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            
            // 加载图片资源
            android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
            options.inJustDecodeBounds = true; // 只获取图片尺寸，不加载图片内容
            
            // 从drawable资源加载图片信息
            android.graphics.BitmapFactory.decodeResource(getResources(), R.drawable.launch_screen_bottom, options);
            
            if (options.outWidth > 0 && options.outHeight > 0) {
                // 计算图片在屏幕上的实际显示高度
                float imageAspectRatio = (float) options.outHeight / options.outWidth;
                float exactHeight = screenWidth * imageAspectRatio;
                int actualHeight = Math.round(exactHeight); // 使用四舍五入避免截断误差
                
                Log.d(TAG, "图片原始尺寸: " + options.outWidth + "x" + options.outHeight);
                Log.d(TAG, "屏幕宽度: " + screenWidth + "px");
                Log.d(TAG, "精确计算高度: " + exactHeight + "px");
                Log.d(TAG, "四舍五入后高度: " + actualHeight + "px");
                
                return actualHeight;
            } else {
                // 如果获取图片尺寸失败，使用默认值
                Log.w(TAG, "无法获取图片尺寸，使用默认高度: " + (162 * density) + "px");
                return (int) (162 * density);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取图片高度失败: " + e.getMessage());
            // 发生异常时使用默认值
            final float density = getResources().getDisplayMetrics().density;
            return (int) (162 * density);
        }
    }


    @SuppressLint("SourceLockedOrientationActivity")
    private void setPortalOrientation(){
        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
