//
//  ReactNativeAdmobile+SplashVod.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/5/28.
//

#import "ReactNativeAdmobile+SplashVod.h"
#import <ADSuyiSDK/ADSuyiSDKSplashAd.h>
#import <objc/runtime.h>
#import <ADSuyiKit/ADSuyiKit.h>


@interface ReactNativeAdmobile (SplashVod)<ADSuyiSDKSplashAdDelegate>
@property (nonatomic, strong)ADSuyiSDKSplashAd *splashAd;

@end

@implementation ReactNativeAdmobile (SplashVod)


- (void)loadSplashAd:(NSString *)posId{
    if (self.splashAd) {
        return;
    }
    // 1、初始化开屏广告实例对象
    self.splashAd = [[ADSuyiSDKSplashAd alloc]init];
    self.splashAd.delegate = self;
#ifdef SUPPORT_SPLASH_ZOOMOUT
    self.splashAd.zoomOutViewDelegate = self;
#endif
    self.splashAd.controller = [self getRootVC];
    // 2、设置开屏的广告位id
//    5df7ae050f7ab37e75
    self.splashAd.posId = posId;
    /**
    开屏请求总超时时间:所有平台轮询的请求等待总时长（不包括图片渲染时间），单位秒，推荐设置为4s，最小值为3s
    开屏各平台分配逻辑:(目前只有开屏需要分配时间，并且理论上分配给到各平台的超时时间不会完全耗尽)
    1、3<=tolerateTimeout<=4:轮询首位平台的超时时间为(tolerateTimeout-1)s，次位为2s，如果后续还有平台统一为1s;
    2、tolerateTimeout>=5:轮询首位平台的超时时间为(tolerateTimeout-2)s，次位为3s，如果后续还有平台统一为2s;
    */
    self.splashAd.tolerateTimeout = 4;
    // 3、设置默认启动图(一般设置启动图的平铺颜色为背景颜色，使得视觉效果更加平滑)

    //  NSURL *url = [[NSBundle mainBundle] URLForResource:@"SST" withExtension:@"bundle"];
    //  NSBundle *bundle = [NSBundle bundleWithURL:url];



    //  UIImage *image = [UIImage imageNamed:@"lanuch"
          //                         inBundle: bundle
             //                    compatibleWithTraitCollection:nil];
                              //使用项目中的图片，主项目需要提供SharedResources.bundle文件
                              // 由于每个项目开屏图不一样，这里三方库不在提供资源文件，需要自己提供

    NSURL *url = [[NSURL alloc]init];
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad){
        url = [[NSBundle mainBundle] URLForResource:@"SharedResources_pad" withExtension:@"bundle"];
    } else {
        url = [[NSBundle mainBundle] URLForResource:@"SharedResources" withExtension:@"bundle"];
    }


       NSBundle *bundle = [NSBundle bundleWithURL:url];

       UIImage *image = [UIImage imageNamed:@"launch"
                                   inBundle: bundle
                                 compatibleWithTraitCollection:nil];

   self.splashAd.backgroundColor = [UIColor adsy_getColorWithImage:image withNewSize:[UIScreen mainScreen].bounds.size];
//   self.splashAd.backgroundColor =[UIColor whiteColor];

//    // 4、开屏广告机型适配
//    CGFloat bottomViewHeight;
//    if (kADSYCurveScreen) { // 刘海屏
//        bottomViewHeight = [UIScreen mainScreen].bounds.size.height * 0.15;
//    } else {
//        bottomViewHeight = [UIScreen mainScreen].bounds.size.height - [UIScreen mainScreen].bounds.size.width * (960 / 640.0);
//    }

//    // 5、底部视图设置，非必选项
//    UIView *bottomView = [[UIView alloc] init];
//    bottomView.backgroundColor = [UIColor whiteColor];
//    bottomView.frame = CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, bottomViewHeight);
//    UIImageView *logoImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"ADMob_Logo.png"]];
//    logoImageView.frame = CGRectMake(([UIScreen mainScreen].bounds.size.width-135)/2, (bottomViewHeight-46)/2, 135, 46);
//    [bottomView addSubview:logoImageView];

    // 6、设置开屏保底逻辑（可选）
    /**
     *功能说明：App在首次启动时，需要先请求获取广告位配置文件后，然后再去请求开屏广告，也就是首次加载开屏广告时需要两次串行网络请求，因此很容易因超时导致开屏广告展示失败。
     *解决方案：为避免开屏超时问题，开放此设置给开发者，开发者可以根据实际需求选择一家广告平台，通过API接口将必需参数传递给Suyi聚合SDK。（该设置只能指定一家广告平台，并且首次启动时只会请求该平台的广告，但App首次开屏广告将不受ADmobile后台控制，包括下载提示，广告位关闭。）
     *该设置仅会在首次加载开屏广告时，SDK会使用开发者传入的参数进行广告请求，同时获取后台配置文件的广告配置信息缓存到本地（首次请求广告平台广告和获取配置信息时并发进行），后续的开屏广告将按照缓存缓存的后台广告位配置顺序进行开屏广告请求。
     *支持穿山甲、优量汇、快手、百度
     */
//    [self.splashAd setBottomSplashWithSuyiPosid:@"5df7ae050f7ab37e75" platformListId:@"3827" platform:@"ksad" appId:@"90010" appKey:nil platformPosid:@"4000000041" renderType:ADSuyiSplashRenderTypeExpressPro];
    // 7、加载开屏广告
    [self.splashAd loadAndShowInWindow:[[[UIApplication sharedApplication] delegate] window]];
}


#pragma mark - ADSuyiSDKSplashAdDelegate
/**
 开屏展现成功

 @param splashAd 广告实例
 */
- (void)adsy_splashAdSuccessToPresentScreen:(ADSuyiSDKSplashAd *)splashAd{
#ifdef SUPPORT_SPLASH_ZOOMOUT
    if(splashAd.splashZoomOutView) {
        UIViewController *rootVc = [UIApplication sharedApplication].keyWindow.rootViewController;
        [rootVc.view addSubview:splashAd.splashZoomOutView];
    }
#endif
}

/**
 开屏展现失败

 @param splashAd 广告实例
 @param error 具体错误信息
 */
- (void)adsy_splashAdFailToPresentScreen:(ADSuyiSDKSplashAd *)splashAd failToPresentScreen:(ADSuyiAdapterErrorDefine *)error{
    splashAd = nil;
}

/**
 开屏广告点击

 @param splashAd 广告实例
 */
- (void)adsy_splashAdClicked:(ADSuyiSDKSplashAd *)splashAd{

}

/**
 开屏被关闭

 @param splashAd 广告实例
 */
- (void)adsy_splashAdClosed:(ADSuyiSDKSplashAd *)splashAd{
#ifdef SUPPORT_SPLASH_ZOOMOUT
    if(splashAd.splashZoomOutView == nil)
        splashAd = nil;
#else
    self.splashAd = nil;
#endif
}

/**
 开屏展示

 @param splashAd 广告实例
 */
- (void)adsy_splashAdEffective:(ADSuyiSDKSplashAd *)splashAd{

}




static char *splashAdKey = "splashAd";

- (ADSuyiSDKSplashAd *)splashAd {
  return objc_getAssociatedObject(self, &splashAdKey);

}

- (void)setSplashAd:(ADSuyiSDKSplashAd *)splashAd {
  objc_setAssociatedObject(self, &splashAdKey, splashAd, OBJC_ASSOCIATION_RETAIN_NONATOMIC);

}



@end
