// ReactNativeAdmobile.m

#import <ADSuyiSDK/ADSuyiSDK.h>
#import <ADSuyiKit/ADSuyiKitLogging.h>

#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>
#import "ReactNativeAdmobile.h"
#import "ReactNativeAdmobile+ReWardVod.h"
#import "ReactNativeAdmobile+SplashVod.h"
#import "ReactNativeAdmobile+NativeVod.h"
#import "ReactNativeAdmobile+IntertitialAd.h"

@interface ReactNativeAdmobile ()

@end


@implementation ReactNativeAdmobile


- (UIViewController*) getRootVC {
    UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    while (root.presentedViewController != nil) {
        root = root.presentedViewController;
    }

    return root;
}

RCT_EXPORT_MODULE()



RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
{
    // TODO: Implement some actually useful functionality
    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
}


RCT_EXPORT_METHOD(initAd:(NSString*)appId
                  resolver:(RCTPromiseResolveBlock)resolve
                   rejecter:(RCTPromiseRejectBlock)reject)
{

    dispatch_async(dispatch_get_main_queue(), ^{
     if (@available(iOS 14, *)) {
            [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
                // 无需对授权状态进行处理
            }];
        } else {
            // Fallback on earlier versions
        }

        [ADSuyiSDK setLogLevel:ADSuyiKitLogLevelDebug];
//        [ADSuyiSDK setOnlyPlatform:ADSuyiAdapterPlatformGDT];

        // ADSuyiSDK初始化
        [ADSuyiSDK initWithAppId:appId completionBlock:^(NSError * _Nonnull error) {
            if (error) {
                reject(@"no_events", @"There were no events", error);
                NSLog(@"SDK 初始化失败：%@", error.localizedDescription);
            }else {
                resolve(@"success");
                NSLog(@"SDK 初始化成功");
            }
        }];
    });

}

/**
开屏广告
 */
RCT_EXPORT_METHOD(splashAd:(NSString*)vodId
                  :(RCTResponseSenderBlock)onSuccess
                  :(RCTResponseSenderBlock)onError) {
    self.onSuccess = onSuccess;
    self.onError=onError;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self loadSplashAd:vodId];
    });

}
/**
 激励广告
 使用promise的方式

 */

// RCT_EXPORT_METHOD(rewardVodAd:(NSString*)vodId
//                  resolver:(RCTPromiseResolveBlock)resolve
//                   rejecter:(RCTPromiseRejectBlock)reject)
// {
//     self.resolve = resolve;
//     self.reject= reject;
//     dispatch_async(dispatch_get_main_queue(), ^{
//         [self loadRewardvodAd:vodId];
//     });
//
// }

/**
 激励广告
  回调方式

 */
RCT_EXPORT_METHOD(rewardVodAd:(NSString*)vodId
                  :(RCTResponseSenderBlock)onSuccess
                  :(RCTResponseSenderBlock)onError) {
    self.onSuccess = onSuccess;
    self.onError=onError;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self loadRewardvodAd:vodId];
    });

}

/**

 信息流广告
 */
RCT_EXPORT_METHOD(nativeAd:(NSString*)vodId
                  :(RCTResponseSenderBlock)onSuccess
                  :(RCTResponseSenderBlock)onError) {
    self.onSuccess = onSuccess;
    self.onError= onError;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self loadNativeAd:vodId];
    });

}

/**

 信息流广告
 */
RCT_EXPORT_METHOD(intertitialAd:(NSString*)vodId
                  :(RCTResponseSenderBlock)onSuccess
                  :(RCTResponseSenderBlock)onError) {
     self.onSuccess = onSuccess;
     self.onError= onError;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self loadIntertitialAd:vodId];
    });

}



/**
 个性化设置
 */
RCT_EXPORT_METHOD(setPersonalizedAdEnabled:(BOOL)personalizedAdEnabled) {

    dispatch_async(dispatch_get_main_queue(), ^{
        ADSuyiSDK.enablePersonalAd = personalizedAdEnabled;

    });

}



@end
