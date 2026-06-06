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

import android.widget.ScrollView;

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
     * 自渲染广告展示（按参考 XML 布局实现：3:4 媒体 + 描述 + 图标标题关闭行）
     */
    private void showFeedAd() {
        ADSuyiNativeFeedAdInfo feedAdInfo = (ADSuyiNativeFeedAdInfo) mNativeAdInfo;
        Context ctx = getContext();
        int adWidthPx = mAdWidthPx;

        // 根容器：RelativeLayout，与 XML 中的 rlAdContainer 对应
        RelativeLayout rootLayout = new RelativeLayout(ctx);
        rootLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        rootLayout.setLayoutParams(new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // ========== 1. 媒体区域（图片/视频，比例 3:4） ==========
        int mediaHeight = (int) (adWidthPx * 4f / 3f);
        FrameLayout mediaContainer = new FrameLayout(ctx);
        mediaContainer.setId(View.generateViewId());
        RelativeLayout.LayoutParams mediaLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, mediaHeight);
        mediaContainer.setLayoutParams(mediaLp);

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
            if (TextUtils.isEmpty(imageUrl) && feedAdInfo.getImageUrlList() != null
                    && !feedAdInfo.getImageUrlList().isEmpty()) {
                imageUrl = feedAdInfo.getImageUrlList().get(0);
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                loadImage(imageUrl, mainImage);
            }
            mediaContainer.addView(mainImage, new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        }
        rootLayout.addView(mediaContainer, mediaLp);

        // ========== 2. 广告标识 ivAdTarget（右下角） ==========
        ImageView ivAdTarget = new ImageView(ctx);
        ivAdTarget.setId(View.generateViewId());
        int platformIconRes = feedAdInfo.getPlatformIcon();
        if (platformIconRes != 0) {
            ivAdTarget.setImageResource(platformIconRes);
        } else {
            ivAdTarget.setVisibility(View.GONE);
        }
        ivAdTarget.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams targetLp = new RelativeLayout.LayoutParams(dp2px(46), dp2px(18));
        targetLp.addRule(RelativeLayout.ALIGN_RIGHT, mediaContainer.getId());
        targetLp.addRule(RelativeLayout.ALIGN_BOTTOM, mediaContainer.getId());
        rootLayout.addView(ivAdTarget, targetLp);

        // ========== 3. 描述 tvDesc ==========
        TextView tvDesc = new TextView(ctx);
        tvDesc.setId(View.generateViewId());
        tvDesc.setText(feedAdInfo.getDesc());
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvDesc.setTextColor(Color.parseColor("#555555"));
        tvDesc.setMaxLines(2);
        tvDesc.setEllipsize(TextUtils.TruncateAt.END);
        RelativeLayout.LayoutParams descLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descLp.addRule(RelativeLayout.BELOW, mediaContainer.getId());
        descLp.setMargins(dp2px(10), dp2px(10), dp2px(10), 0);
        rootLayout.addView(tvDesc, descLp);

        // ========== 4. 图标卡片 cvIcon（CardView + ivIcon） ==========
        int iconSize = dp2px(16);
        FrameLayout cvIcon = new FrameLayout(ctx);
        cvIcon.setId(View.generateViewId());
        // 用 GradientDrawable 实现圆角
        android.graphics.drawable.GradientDrawable iconBg = new android.graphics.drawable.GradientDrawable();
        iconBg.setCornerRadius(iconSize / 2f);
        iconBg.setColor(Color.parseColor("#eeeeee"));
        cvIcon.setBackground(iconBg);
        RelativeLayout.LayoutParams iconCardLp = new RelativeLayout.LayoutParams(iconSize, iconSize);
        iconCardLp.addRule(RelativeLayout.BELOW, tvDesc.getId());
        iconCardLp.addRule(RelativeLayout.ALIGN_LEFT, tvDesc.getId());
        iconCardLp.setMargins(0, dp2px(10), 0, dp2px(10));
        rootLayout.addView(cvIcon, iconCardLp);

        ImageView ivIcon = new ImageView(ctx);
        ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        String iconUrl = feedAdInfo.getIconUrl();
        if (!TextUtils.isEmpty(iconUrl)) {
            loadImage(iconUrl, ivIcon);
        }
        cvIcon.addView(ivIcon, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // ========== 5. 关闭按钮 ivClose ==========
        ImageView ivClose = new ImageView(ctx);
        ivClose.setId(View.generateViewId());
        // 用代码绘制深色关闭 X 图标（icon_close_2 为白色，在白色背景上不可见）
        int closeSizePx = dp2px(20);
        Bitmap closeBitmap = Bitmap.createBitmap(closeSizePx, closeSizePx, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(closeBitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#333333"));
        paint.setStrokeWidth(dp2px(2));
        paint.setStyle(android.graphics.Paint.Style.STROKE);
        int pad = dp2px(4);
        canvas.drawLine(pad, pad, closeSizePx - pad, closeSizePx - pad, paint);
        canvas.drawLine(closeSizePx - pad, pad, pad, closeSizePx - pad, paint);
        ivClose.setImageBitmap(closeBitmap);
        ivClose.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        RelativeLayout.LayoutParams closeLp = new RelativeLayout.LayoutParams(dp2px(20), dp2px(20));
        closeLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        closeLp.addRule(RelativeLayout.ALIGN_TOP, cvIcon.getId());
        closeLp.addRule(RelativeLayout.ALIGN_BOTTOM, cvIcon.getId());
        closeLp.rightMargin = dp2px(10);
        rootLayout.addView(ivClose, closeLp);

        // ========== 6. 标题 tvTitle ==========
        TextView tvTitle = new TextView(ctx);
        tvTitle.setId(View.generateViewId());
        tvTitle.setText(feedAdInfo.getTitle());
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tvTitle.setTextColor(Color.parseColor("#555555"));
        tvTitle.setMaxLines(1);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        RelativeLayout.LayoutParams titleLp = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.addRule(RelativeLayout.RIGHT_OF, cvIcon.getId());
        titleLp.addRule(RelativeLayout.ALIGN_TOP, cvIcon.getId());
        titleLp.addRule(RelativeLayout.ALIGN_BOTTOM, cvIcon.getId());
        titleLp.addRule(RelativeLayout.LEFT_OF, ivClose.getId());
        titleLp.setMargins(dp2px(8), 0, 0, 0);
        rootLayout.addView(tvTitle, titleLp);

        // ========== 7. 添加到当前 View，并注册广告交互 ==========
        addView(rootLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mAdContentView = rootLayout;

        // 注册可点击视图
        feedAdInfo.registerViewForInteraction(rootLayout, mediaContainer, tvDesc, cvIcon, tvTitle, ivAdTarget);

        // 注册关闭按钮
        feedAdInfo.registerCloseView(ivClose);

        // 测量并发送高度
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
