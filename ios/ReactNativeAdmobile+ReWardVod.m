//
//  ReactNativeAdmobile+ReWardVod.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/5/22.
//

#import "ReactNativeAdmobile+ReWardVod.h"
#import <objc/runtime.h>
#import <ADSuyiSDK/ADSuyiSDKRewardvodAd.h>


@interface ReactNativeAdmobile (ReWardVod)<ADSuyiSDKRewardvodAdDelegate>

@property (nonatomic, strong)ADSuyiSDKRewardvodAd *rewardvodAd;
@property(nonatomic ,assign) BOOL isReadyToplay;
@property (nonatomic, assign) BOOL isVideoCompleted; // 添加一个变量，记录广告是否完整播放

@end

@implementation ReactNativeAdmobile (ReWardVod)



- (void)loadRewardvodAd:(NSString *)posId {
    // 1、初始化激励视频广告
    self.rewardvodAd  = [[ADSuyiSDKRewardvodAd alloc]init];
    self.rewardvodAd.delegate = self;
    self.rewardvodAd.tolerateTimeout = 5;
    self.rewardvodAd. controller = [self getRootVC];
    self.rewardvodAd.posId = posId;
    self.rewardvodAd.userId = @"erik";
    self.rewardvodAd.extraInfo = @"这是一个激励验证";
    self.rewardvodAd.rewardName = @"刷豆";
    self.rewardvodAd.rewardAmount = [NSNumber numberWithInt:2];
    // 2、加载激励视频广告
    [self.rewardvodAd loadRewardvodAd];
}

- (void)showRewardvodAd {
    if ([self.rewardvodAd rewardvodAdIsReady] && self.isReadyToplay) {
        [self.rewardvodAd   showRewardvodAd];
        self.isVideoCompleted = NO; // 初始化标志位
    }
}


#pragma mark - ADSuyiSDKRewardvodAdDelegate
/**
 广告数据加载成功回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdLoadSuccess:(ADSuyiSDKRewardvodAd *)rewardvodAd{

}

/**
 激励视频广告准备好被播放

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdReadyToPlay:(ADSuyiSDKRewardvodAd *)rewardvodAd{
    self.isReadyToplay = YES;
    dispatch_async(dispatch_get_main_queue(), ^{
//        [self.view makeToast:@"激励视频准备完成"];
        [self.rewardvodAd showRewardvodAd];
    });

}

/**
 视频数据下载成功回调，已经下载过的视频会直接回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdVideoLoadSuccess:(ADSuyiSDKRewardvodAd *)rewardvodAd{
}
/**
 视频播放页即将展示回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdWillVisible:(ADSuyiSDKRewardvodAd *)rewardvodAd{

}
/**
 视频广告曝光回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdDidVisible:(ADSuyiSDKRewardvodAd *)rewardvodAd{

}
/**
 视频播放页关闭回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdDidClose:(ADSuyiSDKRewardvodAd *)rewardvodAd{
//    if (self.onError) {
//          self.onError(@[ @{@"result":@"close"}]);
//    }
    // **只有广告未完整播放时，才触发回调**
    if (!self.isVideoCompleted && self.onError) {
        self.onError(@[@{@"result": @"close"}]);
    }
    // 4、广告内存回收
    self.rewardvodAd = nil;
}
/**
 视频广告信息点击回调

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdDidClick:(ADSuyiSDKRewardvodAd *)rewardvodAd{
    // RN成功回调
    // if (self.resolve) {
    //     self.resolve(@{@"posId":rewardvodAd.posId});

    // }
    // if (self.onSuccess) {
    //     self.onSuccess(@[[NSNull null], @{@"posId":rewardvodAd.posId}]);

    // }
}
/**
 视频广告视频播放完成

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdDidPlayFinish:(ADSuyiSDKRewardvodAd *)rewardvodAd{

}

/**
 视频广告视频达到奖励条件

 @param rewardvodAd 广告实例
 */
- (void)adsy_rewardvodAdDidRewardEffective:(ADSuyiSDKRewardvodAd *)rewardvodAd{
    NSLog(@"=======>%@", rewardvodAd);
    self.isVideoCompleted = YES; // 记录广告播放完成
    // RN成功回调
    if (self.resolve) {
        self.resolve(@{@"posId":rewardvodAd.posId});

    }
    if (self.onSuccess) {
        self.onSuccess(@[[NSNull null], @{@"posId":rewardvodAd.posId}]);
    }
}

/**
 视频广告请求失败回调

 @param rewardvodAd 广告实例
 @param errorModel 具体错误信息
 */
- (void)adsy_rewardvodAdFailToLoad:(ADSuyiSDKRewardvodAd *)rewardvodAd errorModel:(ADSuyiAdapterErrorDefine *)errorModel{
    if (self.onError) {
        self.onError(@[[NSNull null]]);
    }

    // 4、广告内存回收
    dispatch_async(dispatch_get_main_queue(), ^{
//        [self.view makeToast:errorModel.description];
    });
    self.rewardvodAd = nil;
}

/**
 视频广告播放时各种错误回调

 @param rewardvodAd 广告实例
 @param errorModel 具体错误信息
 */
- (void)adsy_rewardvodAdPlaying:(ADSuyiSDKRewardvodAd *)rewardvodAd errorModel:(ADSuyiAdapterErrorDefine *)errorModel{
//    if (self.onError) {
//        self.onError(@[[NSNull null]]);
//    }
}

- (void)adsy_rewardvodAdServerDidSucceed:(ADSuyiSDKRewardvodAd *)rewardvodAd {

}

- (void)adsy_rewardvodAdServerDidFailed:(ADSuyiSDKRewardvodAd *)rewardvodAd errorModel:(ADSuyiAdapterErrorDefine *)errorModel {
    if (self.onError) {
        self.onError(@[[NSNull null]]);
    }

}


static char *rewardvodAdKey = "rewardvodAd";

- (ADSuyiSDKRewardvodAd *)rewardvodAd {
  return objc_getAssociatedObject(self, &rewardvodAdKey);

}

- (void)setRewardvodAd:(ADSuyiSDKRewardvodAd *)rewardvodAd {
  objc_setAssociatedObject(self, &rewardvodAdKey, rewardvodAd, OBJC_ASSOCIATION_RETAIN_NONATOMIC);

}



static char *isReadyToplayKey ="isReadyToplayKey";

- (void)setIsReadyToplay:(BOOL )isReadyToplay {
  objc_setAssociatedObject(self, &isReadyToplayKey, @(isReadyToplay), OBJC_ASSOCIATION_ASSIGN);

}

- (BOOL)isReadyToplayKey{
    return [objc_getAssociatedObject(self, isReadyToplayKey) boolValue];
}


@end
