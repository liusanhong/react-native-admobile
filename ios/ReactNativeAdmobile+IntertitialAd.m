//
//  ReactNativeAdmobile+IntertitialAd.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/11/6.
//

#import "ReactNativeAdmobile+IntertitialAd.h"
#import <ADSuyiSDK/ADSuyiSDKIntertitialAd.h>
#import <objc/runtime.h>

@interface ReactNativeAdmobile (IntertitialAd)<ADSuyiSDKIntertitialAdDelegate>
@property (nonatomic, strong) ADSuyiSDKIntertitialAd *intertitialAd;

@end


@implementation ReactNativeAdmobile (IntertitialAd)

- (void)loadIntertitialAd:(NSString *)posId {
    // 1、初始化插屏广告
    self.intertitialAd = [ADSuyiSDKIntertitialAd new];
    self.intertitialAd.controller =  [self getRootVC];
    self.intertitialAd.posId = posId;
    self.intertitialAd.delegate = self;
    self.intertitialAd.tolerateTimeout = 4;
//    if (![[SetConfigManager sharedManager].fullAdAdScenceId isEqualToString:@""])
//        self.intertitialAd.scenesId = [SetConfigManager sharedManager].fullAdAdScenceId;
    // 2、加载插屏广告
    [self.intertitialAd loadAdData];
}


#pragma mark - ADSuyiSDKIntertitialAdDelegate
/**
 ADSuyiSDKIntertitialAd请求成功回调
 
 @param interstitialAd 插屏广告实例对象
*/
- (void)adsy_interstitialAdSuccedToLoad:(ADSuyiSDKIntertitialAd *)interstitialAd{
    // 3、展示插屏广告
//    _isReady = YES;
//    [self.view makeToast:@"广告准备好"];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.intertitialAd show];
    });
}

/**
 ADSuyiSDKIntertitialAd请求失败回调

 @param interstitialAd 插屏广告实例对象
 @param error 失败原因
*/
- (void)adsy_interstitialAdFailedToLoad:(ADSuyiSDKIntertitialAd *)interstitialAd error:(ADSuyiAdapterErrorDefine *)error{
    // 4、内存回收
//    [self.view makeToast:error.description];
    if (self.onError) {
        self.onError(@[[NSNull null]]);
    }
    self.intertitialAd = nil;
}

/**
 ADSuyiSDKIntertitialAd展示在屏幕内回调

 @param interstitialAd 插屏广告实例对象
*/
- (void)adsy_interstitialAdDidPresent:(ADSuyiSDKIntertitialAd *)interstitialAd{
    if (self.onSuccess) {
        self.onSuccess(@[[NSNull null], @{@"posId":interstitialAd.posId}]);
    }
}

/**
 ADSuyiSDKIntertitialAd展示在屏幕内失败回调

 @param interstitialAd 插屏广告实例对象
*/
- (void)adsy_interstitialAdFailedToPresent:(ADSuyiSDKIntertitialAd *)interstitialAd error:(NSError *)error{
//    [self.view makeToast:error.description];
    if (self.onError) {
        self.onError(@[[NSNull null]]);
    }
}

/**
 ADSuyiSDKIntertitialAd点击回调

 @param interstitialAd 插屏广告实例对象
*/
- (void)adsy_interstitialAdDidClick:(ADSuyiSDKIntertitialAd *)interstitialAd{
    if (self.onSuccess) {
        self.onSuccess(@[[NSNull null], @{@"posId":interstitialAd.posId}]);
    }
}

/**
 ADSuyiSDKIntertitialAd关闭回调

 @param interstitialAd 插屏广告实例对象
*/
- (void)adsy_interstitialAdDidClose:(ADSuyiSDKIntertitialAd *)interstitialAd{
    // 4、内存回收
    self.intertitialAd = nil;
}

/**
 ADSuyiSDKIntertitialAd展示回调
 
 @param interstitialAd 广告实例
 */
- (void)adsy_interstitialAdExposure:(ADSuyiSDKIntertitialAd *)interstitialAd{
    if (self.onSuccess) {
        self.onSuccess(@[[NSNull null], @{@"posId":interstitialAd.posId}]);
    }
}

/**
 ADSuyiSDKIntertitialAd关闭落地页回调
 
 @param interstitialAd 广告实例
 */
-(void)adsy_interstitialAdCloseLandingPage:(ADSuyiSDKIntertitialAd *)interstitialAd{
    
}


static char *intertitialAdKey = "intertitialAd";

- (ADSuyiSDKIntertitialAd *)intertitialAd {
  return objc_getAssociatedObject(self, &intertitialAdKey);

}

- (void)setIntertitialAd:(ADSuyiSDKIntertitialAd *)intertitialAd {
  objc_setAssociatedObject(self, &intertitialAdKey, intertitialAd, OBJC_ASSOCIATION_RETAIN_NONATOMIC);

}


@end
