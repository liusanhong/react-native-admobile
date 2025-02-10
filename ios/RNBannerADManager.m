//
//  RNADmobileManager.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/5/25.
//

#import "RNBannerADManager.h"
#import "RNAdmobileBanner.h"
#import "RNBannerADEmitter.h"
#import <ADSuyiSDK/ADSuyiSDKBannerAdView.h>

@interface  RNBannerADManager ()<ADSuyiSDKBannerAdViewDelegate>

@property (nonatomic, strong) RNBannerADEmitter *emitter;

@end

@implementation RNBannerADManager

- (UIViewController*) getRootVC {
    UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    while (root.presentedViewController != nil) {
        root = root.presentedViewController;
    }

    return root;
}


RCT_EXPORT_MODULE(RNBannerAD)

//RCT_EXPORT_VIEW_PROPERTY(posId, NSString) //该方法仅处理自带属性，需要视图那边重写set方法

RCT_CUSTOM_VIEW_PROPERTY(posId,NSString,RNAdmobileBanner) { //处理自定义属性，

    view.banner.posId = json;
    [view addSubview:view.banner];
    [view.banner loadAndShow:[self getRootVC]];

    //[view.banner loadAndShowWithError:nil];

}


RCT_CUSTOM_VIEW_PROPERTY(height,CGFloat,RNAdmobileBanner) { //处理自定义属性，
    view.banner.frame = CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, [[NSString stringWithFormat:@"%@",json] floatValue]);
}
- (UIView *)view
{
    //RNAdmobileBanner 使用包裹一次的原因是因为无法重写ADSuyiSDKBannerAdView 的posId set方法
    RNAdmobileBanner *banner  =  [[RNAdmobileBanner alloc] init];
    banner.banner =[[ADSuyiSDKBannerAdView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width,58)];
    banner.banner.delegate = self;
    //banner.banner.controller = [self getRootVC];
    self.emitter = [RNBannerADEmitter allocWithZone: nil];
    return banner;
}



#pragma mark - ADSuyiSDKBannerAdViewDelegate
/**
 广告获取成功

 @param bannerView banner实例
 */
- (void)adsy_bannerViewDidReceived:(ADSuyiSDKBannerAdView *)bannerView{
    [self.emitter bannerViewDidReceived];

}

/**
 广告拉取失败

 @param bannerView banner实例
 @param errorModel 错误描述
 */
- (void)adsy_bannerViewFailToReceived:(ADSuyiSDKBannerAdView *)bannerView errorModel:(ADSuyiAdapterErrorDefine *)errorModel{
    NSLog(@"adsy_bannerViewFailToReceived:%@, %@",errorModel.errorDescription, errorModel.errorDetailDict);
//    [_bannerView removeFromSuperview];
//    _bannerView = nil;

    [self.emitter bannerViewFailToReceived];
}

/**
 广告点击

 @param bannerView 广告实例
 */
- (void)adsy_bannerViewClicked:(ADSuyiSDKBannerAdView *)bannerView{

}

/**
 广告关闭

 @param bannerView 广告实例
 */
- (void)adsy_bannerViewClose:(ADSuyiSDKBannerAdView *)bannerView{
//    _bannerView = nil;
}

/**
 广告展示

 @param bannerView 广告实例
 */
- (void)adsy_bannerViewExposure:(ADSuyiSDKBannerAdView *)bannerView{

}

- (void)adsy_bannerAdCloseLandingPage:(nonnull ADSuyiSDKBannerAdView *)bannerView {

}



@end
