package com.reactnativeadmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import cn.admobiletop.adsuyi.ad.ADSuyiRewardVodAd;
import cn.admobiletop.adsuyi.ad.data.ADSuyiRewardVodAdInfo;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiExtraParams;
import cn.admobiletop.adsuyi.ad.entity.ADSuyiRewardExtra;
import cn.admobiletop.adsuyi.ad.error.ADSuyiError;
import cn.admobiletop.adsuyi.ad.listener.ADSuyiRewardVodAdListener;
import cn.admobiletop.adsuyi.util.ADSuyiAdUtil;

public class RewardVodActivity extends AppCompatActivity {
    private String TAG = "RewardVodActivity";
    ADSuyiRewardVodAd rewardVodAd;
    private ADSuyiRewardVodAdInfo rewardVodAdInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_vod);


        // 创建激励视频广告实例
        rewardVodAd = new ADSuyiRewardVodAd(this);

        ADSuyiRewardExtra adSuyiRewardExtra = new ADSuyiRewardExtra("3798624");
// 设置激励视频服务端验证的自定义信息
        adSuyiRewardExtra.setCustomData("设置激励视频服务端验证的自定义信息");
// 设置激励视频服务端激励名称(mintegral渠道不支持)
        adSuyiRewardExtra.setRewardName("激励名称");
// 设置激励视频服务端激励数量(mintegral渠道不支持)
        adSuyiRewardExtra.setRewardAmount(1);

// 创建额外参数实例
        ADSuyiExtraParams extraParams = new ADSuyiExtraParams.Builder()
                // 设置激励视频额外参数（可不设置）
//                .rewardExtra(adSuyiRewardExtra)
                // 设置视频类广告是否静音（部分渠道支持）
                .setVideoWithMute(false)
                .build();

        rewardVodAd.setLocalExtraParams(extraParams);

// 设置激励视频广告监听
        rewardVodAd.setListener(new ADSuyiRewardVodAdListener() {

            @Override
            public void onAdReceive(ADSuyiRewardVodAdInfo rewardVodAdInfo) {
                // 激励视频广告对象一次成功拉取的广告数据只允许展示一次
                Log.d(TAG, "广告获取成功回调... ");
                RewardVodActivity.this.rewardVodAdInfo = rewardVodAdInfo;
                Log.d(TAG, "onAdReceive----->");
                ADSuyiAdUtil.showRewardVodAdConvenient(RewardVodActivity.this, rewardVodAdInfo);
            }

            @Override
            public void onVideoCache(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                // 部分渠道存在激励展示类广告，不会回调该方法，建议在onAdReceive做广告展示处理
                Log.d(TAG, "onVideoCache----->");
                Log.d(TAG, "广告视频缓存成功回调... ");
            }

            @Override
            public void onVideoComplete(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                Log.d(TAG, "onVideoComplete----->");
                Log.d(TAG, "广告观看完成回调... ");
            }

            @Override
            public void onVideoError(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo, ADSuyiError adSuyiError) {
                Log.d(TAG, "onVideoError----->");
                Log.d(TAG, "广告播放错误回调... ");
            }

            @Override
            public void onReward(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                Log.d(TAG, "onReward----->");
                Log.d(TAG, "广告激励发放回调... ");
            }

            @Override
            public void onAdExpose(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                Log.d(TAG, "onAdExpose----->");
                Log.d(TAG, "广告展示回调，有展示回调不一定是有效曝光，如网络等情况导致上报失败");
            }

            @Override
            public void onAdClick(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                Log.d(TAG, "onAdClick----->");
                Log.d(TAG, "广告点击回调，有点击回调不一定是有效点击，如网络等情况导致上报失败");
            }

            @Override
            public void onAdClose(ADSuyiRewardVodAdInfo adSuyiRewardVodAdInfo) {
                Log.d(TAG, "onAdClose----->");
                Log.d(TAG, "广告关闭回调");
                RewardVodActivity.this.finish();
            }

            @Override
            public void onAdFailed(ADSuyiError adSuyiError) {
                if (adSuyiError != null) {
                    String failedJosn = adSuyiError.toString();
                    Log.d(TAG, "onAdFailed----->" + failedJosn);
                }
            }
        });

// 激励广告场景id（场景id非必选字段，如果需要可到开发者后台创建）
//        rewardVodAd.setSceneId("e6d23c341789ad76f4");
// 加载激励视频广告，参数为广告位ID
        rewardVodAd.loadAd("e6d23c341789ad76f4");


    }
}