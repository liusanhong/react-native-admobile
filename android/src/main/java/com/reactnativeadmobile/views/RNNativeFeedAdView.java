package com.reactnativeadmobile.views;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.RCTEventEmitter;
import com.reactnativeadmobile.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.admobiletop.adsuyi.ad.ADSuyiNativeAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiNativeAdInfo;
import cn.admobiletop.adsuyi.ad.data.ADSuyiNativeExpressAdInfo;
import cn.admobiletop.adsuyi.ad.data.ADSuyiNativeFeedAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiAdSize;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiNativeAdListener;
import cn.admobiletop.adsuyi.util.ADSuyiAdUtil;
import cn.admobiletop.adsuyi.util.ADSuyiDisplayUtil;
import cn.admobiletop.adsuyi.util.ADSuyiViewUtil;

/**
 * 自渲染信息流广告 View
 * 兼容自渲染（ADSuyiNativeFeedAdInfo）和模板（ADSuyiNativeExpressAdInfo）两种广告类型
 */
public class RNNativeFeedAdView extends LinearLayout {
    private ReactContext reactContext;
    private String mPosId;
    private int mAdWidthPx;
    private int mAdHeightPx;
    private String TAG = "RNNativeFeedAdView";
    private ADSuyiNativeAd mNativeAd;
    private ADSuyiNativeAdInfo mNativeAdInfo;

    // 自渲染广告视图引用
    private View mAdContentView;
    // 模板广告视图引用
    private View mExpressAdView;

    private float density;
    // 标记是否已发送过高度事件，避免 onRenderSuccess 和 showFeedAd 重复发送
    private boolean mHeightEventSent = false;

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        ReactContext ctx = (ReactContext) getContext();
        ctx.getJSModule(RCTEventEmitter.class).receiveEvent(getId(), eventName, params);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    public RNNativeFeedAdView(ReactContext context) {
        super(context);
        reactContext = context;
        density = context.getResources().getDisplayMetrics().density;
        setOrientation(VERTICAL);
        Utils.setupLayoutHack(this);
    }

    public void setPosId(String posId) {
        mPosId = posId;
        runOnUiThread(() -> loadAd());
    }

    public void setAdWidth(int widthPx) {
        mAdWidthPx = widthPx;
    }

    public void setAdHeight(int heightPx) {
        mAdHeightPx = heightPx;
    }

    private void loadAd() {
        if (TextUtils.isEmpty(mPosId)) {
            return;
        }
        if (reactContext == null) {
            return;
        }

        releaseAd();
        mHeightEventSent = false;

        mNativeAd = new ADSuyiNativeAd(reactContext.getCurrentActivity());

        if (mAdWidthPx == 0) {
            mAdWidthPx = reactContext.getResources().getDisplayMetrics().widthPixels;
        }

        Log.d(TAG, "loadAd posId:" + mPosId + " width:" + mAdWidthPx);

        Map<String, Object> map = new HashMap<>();
        map.put("isShowKsCloseView", true);

        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                .adSize(new ADSuyiAdSize(mAdWidthPx, 0))
                .nativeAdPlayWithMute(true)
                .nativeExtraMap(map)
                .build();
        mNativeAd.setLocalExtraParams(extraParams);
        mNativeAd.setOnlySupportPlatform(null);

        mNativeAd.setListener(new ADSuyiNativeAdListener() {
            @Override
            public void onRenderFailed(ADSuyiNativeAdInfo adInfo, ADSuyiError error) {
                Log.d(TAG, "onRenderFailed " + error.toString());
                WritableMap params = Arguments.createMap();
                params.putInt("errorCode", error.getCode());
                params.putString("errorDescription", error.getError());
                sendEvent("topNativeFail", params);
            }

            @Override
            public void onRenderSuccess(ADSuyiNativeAdInfo adInfo) {
                Log.d(TAG, "onRenderSuccess " + adInfo.toString());
                sendHeightEvent();
            }

            @Override
            public void onAdReceive(List<ADSuyiNativeAdInfo> adInfos) {
                Log.d(TAG, "onAdReceive size:" + adInfos.size());
                mNativeAdInfo = adInfos.get(0);
                showAd();
            }

            @Override
            public void onAdExpose(ADSuyiNativeAdInfo adInfo) {
                Log.d(TAG, "onAdExpose");
            }

            @Override
            public void onAdClick(ADSuyiNativeAdInfo adInfo) {
                Log.d(TAG, "onAdClick");
            }

            @Override
            public void onAdClose(ADSuyiNativeAdInfo adInfo) {
                releaseAd();
                removeAllViews();
                Log.d(TAG, "onAdClose");
                WritableMap params = Arguments.createMap();
                params.putString("result", "closed");
                sendEvent("topNativeClose", params);
            }

            @Override
            public void onAdFailed(ADSuyiError error) {
                Log.d(TAG, "onAdFailed " + error.toString());
                WritableMap params = Arguments.createMap();
                params.putInt("errorCode", error.getCode());
                params.putString("errorDescription", error.getError());
                sendEvent("topNativeFail", params);
            }
        });

        mNativeAd.loadAd(mPosId, 1);
    }

    private void showAd() {
        if (mNativeAdInfo == null || ADSuyiAdUtil.adInfoIsRelease(mNativeAdInfo)) {
            Log.d(TAG, "广告未获取或已释放");
            return;
        }

        removeAllViews();

        if (mNativeAdInfo.isNativeExpress()) {
            showExpressAd();
        } else {
            showFeedAd();
        }
    }

    /**
     * 模板广告展示
     */
    private void showExpressAd() {
        ADSuyiNativeExpressAdInfo expressAdInfo = (ADSuyiNativeExpressAdInfo) mNativeAdInfo;
        mExpressAdView = expressAdInfo.getNativeExpressAdView(this);

        FrameLayout container = new FrameLayout(getContext());
        container.addView(mExpressAdView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ADSuyiViewUtil.addAdViewToAdContainer(this, container,
                new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        expressAdInfo.render(this);
    }

    /**
     * 自渲染广告展示
     */
    private void showFeedAd() {
        ADSuyiNativeFeedAdInfo feedAdInfo = (ADSuyiNativeFeedAdInfo) mNativeAdInfo;
        Context ctx = getContext();
        int adWidthPx = mAdWidthPx;
        int paddingPx = dp2px(12);

        // 根容器
        LinearLayout rootLayout = new LinearLayout(ctx);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#ffffff"));

        // === 1. 主图/视频区域（16:9 比例，和 SDK Demo 一致） ===
        int mediaHeight = (int) (adWidthPx * 9f / 16f);
        FrameLayout mediaContainer = new FrameLayout(ctx);
        mediaContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mediaHeight));

        if (feedAdInfo.hasMediaView()) {
            // 视频广告
            View mediaView = feedAdInfo.getMediaView(mediaContainer);
            if (mediaView != null) {
                mediaContainer.addView(mediaView, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
            }
        } else {
            // 图片广告
            ImageView mainImage = new ImageView(ctx);
            mainImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            String imageUrl = feedAdInfo.getImageUrl();
            if (TextUtils.isEmpty(imageUrl) && feedAdInfo.getImageUrlList() != null && !feedAdInfo.getImageUrlList().isEmpty()) {
                imageUrl = feedAdInfo.getImageUrlList().get(0);
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                loadImage(imageUrl, mainImage);
            }
            mediaContainer.addView(mainImage, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        rootLayout.addView(mediaContainer);

        // === 2. 底部信息栏（图标 + 标题描述 + CTA按钮） ===
        LinearLayout bottomBar = new LinearLayout(ctx);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setPadding(paddingPx, dp2px(8), paddingPx, dp2px(8));

        // 图标
        int iconSize = dp2px(36);
        ImageView iconImage = new ImageView(ctx);
        iconImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String iconUrl = feedAdInfo.getIconUrl();
        if (!TextUtils.isEmpty(iconUrl)) {
            loadImage(iconUrl, iconImage);
        }
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconLp.rightMargin = dp2px(8);
        bottomBar.addView(iconImage, iconLp);

        // 标题和描述
        LinearLayout textContainer = new LinearLayout(ctx);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        textContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView titleView = new TextView(ctx);
        titleView.setText(feedAdInfo.getTitle());
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        titleView.setTextColor(Color.parseColor("#333333"));
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setMaxLines(1);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        textContainer.addView(titleView);

        TextView descView = new TextView(ctx);
        descView.setText(feedAdInfo.getDesc());
        descView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        descView.setTextColor(Color.parseColor("#999999"));
        descView.setMaxLines(1);
        descView.setEllipsize(TextUtils.TruncateAt.END);
        textContainer.addView(descView);

        bottomBar.addView(textContainer);

        // CTA 按钮
        String ctaText = feedAdInfo.getCtaText();
        if (TextUtils.isEmpty(ctaText)) {
            ctaText = "查看";
        }
        TextView ctaButton = new TextView(ctx);
        ctaButton.setText(ctaText);
        ctaButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        ctaButton.setTextColor(Color.WHITE);
        ctaButton.setBackgroundColor(Color.parseColor("#4A90D9"));
        ctaButton.setGravity(Gravity.CENTER);
        ctaButton.setPadding(dp2px(12), dp2px(4), dp2px(12), dp2px(4));
        LinearLayout.LayoutParams ctaLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp2px(28));
        ctaLp.leftMargin = dp2px(8);
        bottomBar.addView(ctaButton, ctaLp);

        rootLayout.addView(bottomBar);

        // === 3. 广告标识 + 平台 logo + 关闭按钮 ===
        RelativeLayout footerBar = new RelativeLayout(ctx);
        footerBar.setPadding(paddingPx, 0, paddingPx, dp2px(6));

        // 平台 logo
        int platformIconRes = feedAdInfo.getPlatformIcon();
        if (platformIconRes != 0) {
            ImageView logoView = new ImageView(ctx);
            logoView.setImageResource(platformIconRes);
            RelativeLayout.LayoutParams logoLp = new RelativeLayout.LayoutParams(dp2px(40), dp2px(16));
            logoLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            logoLp.addRule(RelativeLayout.CENTER_VERTICAL);
            footerBar.addView(logoView, logoLp);
        }

        // "广告"标签
        TextView adLabel = new TextView(ctx);
        adLabel.setText("广告");
        adLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        adLabel.setTextColor(Color.parseColor("#CCCCCC"));
        adLabel.setBackgroundResource(android.R.drawable.edit_text);
        adLabel.setPadding(dp2px(2), 0, dp2px(2), 0);
        adLabel.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams labelLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp2px(14));
        labelLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        labelLp.addRule(RelativeLayout.CENTER_VERTICAL);
        footerBar.addView(adLabel, labelLp);

        // 关闭按钮
        TextView closeBtn = new TextView(ctx);
        closeBtn.setText("✕");
        closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        closeBtn.setTextColor(Color.parseColor("#999999"));
        closeBtn.setGravity(Gravity.CENTER);
        int closeSize = dp2px(24);
        RelativeLayout.LayoutParams closeLp = new RelativeLayout.LayoutParams(closeSize, closeSize);
        closeLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeLp.addRule(RelativeLayout.CENTER_VERTICAL);
        footerBar.addView(closeBtn, closeLp);

        rootLayout.addView(footerBar);

        // 添加到自身
        addView(rootLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAdContentView = rootLayout;

        // 注册交互（点击、曝光等），必须调用
        feedAdInfo.registerViewForInteraction(rootLayout, mediaContainer, ctaButton);

        // 注册关闭按钮，必须调用，关闭后回调 onAdClose
        feedAdInfo.registerCloseView(closeBtn);

        // 立即测量并发送高度（自渲染不需要等 onRenderSuccess）
        post(() -> sendHeightEvent());
    }

    /**
     * 测量并发送广告高度事件
     */
    private void sendHeightEvent() {
        if (mHeightEventSent) {
            return;
        }
        mHeightEventSent = true;

        View targetView = mAdContentView != null ? mAdContentView : mExpressAdView;
        if (targetView == null || mNativeAdInfo == null) {
            return;
        }

        int[] size = measureView(targetView);
        int adHeightPx = size[1] > 0 ? size[1] : mAdHeightPx;

        if ("gdt".equals(mNativeAdInfo.getPlatform())) {
            if (adHeightPx < 143) {
                adHeightPx = 143;
            }
        }

        WritableMap params = Arguments.createMap();
        params.putDouble("adWidth", ADSuyiDisplayUtil.px2dp(mAdWidthPx));
        params.putDouble("height", ADSuyiDisplayUtil.px2dp(adHeightPx));
        params.putString("platform", mNativeAdInfo.getPlatform());
        params.putBoolean("isExpress", mNativeAdInfo.isNativeExpress());
        sendEvent("topNativeRenderSuccess", params);

        targetView.requestLayout();
        requestLayout();
        Utils.setupLayoutHack(this);

        Log.d(TAG, "sendHeightEvent height:" + ADSuyiDisplayUtil.px2dp(adHeightPx) + "dp");
    }

    private int[] measureView(View view) {
        int[] size = new int[2];
        int widthSpec = View.MeasureSpec.makeMeasureSpec(mAdWidthPx, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);
        size[0] = view.getMeasuredWidth();
        size[1] = view.getMeasuredHeight();
        Log.d(TAG, "measureView width:" + size[0] + " height:" + size[1]);
        return size;
    }

    private int dp2px(int dp) {
        return (int) (dp * density + 0.5f);
    }

    private void releaseAd() {
        if (mNativeAd != null) {
            mNativeAd.release();
            mNativeAd = null;
        }
        if (mNativeAdInfo != null) {
            mNativeAdInfo.release();
            mNativeAdInfo = null;
        }
        mAdContentView = null;
        mExpressAdView = null;
    }

    /**
     * 在后台线程加载网络图片并设置到 ImageView
     */
    private void loadImage(String imageUrl, ImageView imageView) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setDoInput(true);
                conn.connect();
                InputStream is = conn.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                is.close();
                conn.disconnect();
                if (bitmap != null) {
                    new Handler(Looper.getMainLooper()).post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                Log.e(TAG, "loadImage failed: " + e.getMessage());
            }
        }).start();
    }
}
