//
//  ReactNativeAdmobile+NativeVod.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/10.
//

#import "ReactNativeAdmobile+NativeVod.h"
#import <ADSuyiSDK/ADSuyiSDKNativeAd.h>
#import <objc/runtime.h>
#import <ADSuyiKit/UIFont+ADSuyiKit.h>
#import <ADSuyiKit/UIColor+ADSuyiKit.h>
#import "RNNativeADShareInstance.h"
@interface ReactNativeAdmobile (NativeVod)<ADSuyiSDKNativeAdDelegate>
@property (nonatomic, strong)ADSuyiSDKNativeAd *nativeAd;

@end


@implementation ReactNativeAdmobile (NativeVod)

- (void)loadNativeAd:(NSString *)posId{
    
    self.nativeAd = [[ADSuyiSDKNativeAd alloc] initWithAdSize:CGSizeMake([UIScreen mainScreen].bounds.size.width, 100)];
    // 2、传入posId，重要
    self.nativeAd.delegate = self;
    self.nativeAd.controller = [self getRootVC];
    self.nativeAd.posId = posId;
    [self.nativeAd load:5];
}

#pragma ADSuyiSDKNativeAdDelegate

/**
 信息流广告请求成功

 @param nativeAd 广告实例
 @param adViewArray 广告模板集合
 */
- (void)adsy_nativeAdSucessToLoad:(ADSuyiSDKNativeAd *)nativeAd
                      adViewArray:(NSArray<__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *> *)adViewArray{
    
    
    for (UIView<ADSuyiAdapterNativeAdViewDelegate> *adView in adViewArray) {
        // 4、判断信息流广告是否为自渲染类型（可选实现） 可仿照所示样式demo实现 如无所需样式则需自行实现
        // 如果单纯只配置了模版信息流，那么不需要实现，如果配置了自渲染信息流，那么需要实现
        if(adView.renderType == ADSuyiAdapterRenderTypeNative) {
            // 4.1、如果是自渲染类型则可样式自定义(3种示例demo样式见下)
                // 1、常规样式
//            [self setUpUnifiedTopImageNativeAdView:adView];
                // 2、纯图样式
//            [self setUpUnifiedOnlyImageNativeAdView:adView];
                // 3、上图下文
            [self setUpUnifiedTopImageNativeAdView:adView];
        }
        // 5、注册，自渲染：注册点击事件，模板：render，重要
        [adView adsy_registViews:@[adView]];
//        [[RNNativeADShareInstance shareInstance] initWith:adView];

        
        
        // 广点通视频信息流广告会给mediaView添加事件，点击会出现半屏广告，以下为广点通官方给予的解决方案
        if([adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]
           && adView.renderType == ADSuyiAdapterRenderTypeNative
           && adView.data.shouldShowMediaView) {
            UIView *mediaView = [adView adsy_mediaViewForWidth:0.0];
            mediaView.userInteractionEnabled = NO;
        }
    }
    
}

/**
 信息流广告请求失败

 @param nativeAd 广告实例
 @param errorModel 请求错误描述
 */
- (void)adsy_nativeAdFailToLoad:(ADSuyiSDKNativeAd *)nativeAd
                     errorModel:(ADSuyiAdapterErrorDefine *)errorModel{
    
}

/**
 信息流广告渲染成功

 @param adView 广告视图
 */
- (void)adsy_nativeAdViewRenderOrRegistSuccess:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView{
    
    // 6、注册或渲染成功，此时高度正常，可以展示
    dispatch_async(dispatch_get_main_queue(), ^{
     
    [[RNNativeADShareInstance shareInstance] initWith:adView];
        
        NSString *width = [NSString stringWithFormat:@"%f", [adView bounds].size.width];
        NSString *height  = [NSString stringWithFormat:@"%f", [adView bounds].size.height];

        if (self.onSuccess) {
            self.onSuccess(@[[NSNull null], @{@"width": width,@"height":height}]);
        }
    });
    
}

/**
 信息流广告渲染失败

 @param adView 广告视图
 */
- (void)adsy_nativeAdViewRenderOrRegistFail:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView{
    
}

/**
 信息流广告被点击

 @param nativeAd 广告模板
 */
- (void)adsy_nativeAdClicked:(ADSuyiSDKNativeAd *)nativeAd
                      adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView{
    
}

/**
 信息流广告被关闭
 
 @param nativeAd 广告模板
 */
- (void)adsy_nativeAdClose:(ADSuyiSDKNativeAd *)nativeAd
                    adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView{
    
}
/**
 信息流广告被展示
 
 @param nativeAd 广告实例
 */
- (void)adsy_nativeAdExposure:(ADSuyiSDKNativeAd *)nativeAd
                       adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView{
    
}



#pragma mark - Helper 自渲染类型信息流处理方法（以下广告样式根据需求选择） 1、setUpUnifiedNativeAdView常规样式 2、setUpUnifiedOnlyImageNativeAdView纯图样式  3、setUpUnifiedTopImageNativeAdView上图下文样式

// 1、常规信息流示例样式
- (void)setUpUnifiedNativeAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 设计的adView实际大小，其中宽度和高度可以自己根据自己的需求设置
    CGFloat adWidth = [self getRootVC].view.frame.size.width;
    CGFloat adHeight = (adWidth - 17 * 2) / 16.0 * 9 + 67 + 38;
    adView.frame = CGRectMake(0, 0, adWidth, adHeight);
    
    // 展示关闭按钮（必要）
    UIButton *closeButton = [UIButton new];
    [adView addSubview:closeButton];
    closeButton.frame = CGRectMake(adWidth - 44, 0, 44, 44);
    [closeButton setImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
    // adsy_close该方法为协议中方法 直接添加target即可 无需实现
    [closeButton addTarget:adView action:@selector(adsy_close) forControlEvents:UIControlEventTouchUpInside];
    
    // 显示logo图片（必要）
    if(![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) { // 优量汇（广点通）会自带logo，不需要添加
        UIImageView *logoImage = [UIImageView new];
        [adView addSubview:logoImage];
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable image) {
            CGFloat maxWidth = 40;
            CGFloat logoHeight = maxWidth / image.size.width * image.size.height;
            logoImage.frame = CGRectMake(adWidth - maxWidth, adHeight - logoHeight, maxWidth, logoHeight);
            logoImage.image = image;
        }];
    }
    
    // 设置标题文字（可选，但强烈建议带上）
    UILabel *titlabel = [UILabel new];
    [adView addSubview:titlabel];
    titlabel.font = [UIFont adsy_PingFangMediumFont:14];
    titlabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    titlabel.numberOfLines = 2;
    titlabel.text = adView.data.title;
    CGSize textSize = [titlabel sizeThatFits:CGSizeMake(adWidth - 17 * 2, 999)];
    titlabel.frame = CGRectMake(17, 16, adWidth - 17 * 2, textSize.height);
    
    CGFloat height = textSize.height + 16 + 15;
    
    // 设置主图/视频（主图可选，但强烈建议带上,如果有视频试图，则必须带上）
    CGRect mainFrame = CGRectMake(17, height, adWidth - 17 * 2, (adWidth - 17 * 2) / 16.0 * 9);
    if(adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        NSString *urlStr = adView.data.imageUrl;
        if(urlStr.length > 0) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:urlStr]]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    imageView.image = image;
                });
            });
        }
    }
    
    // 设置广告标识（可选）
    height += (adWidth - 17 * 2) / 16.0 * 9 + 9;
    UILabel *adLabel = [[UILabel alloc]init];
    adLabel.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
    adLabel.textColor = [UIColor adsy_colorWithHexString:@"#FFFFFF"];
    adLabel.font = [UIFont adsy_PingFangLightFont:12];
    adLabel.text = @"广告";
    [adView addSubview:adLabel];
    adLabel.frame = CGRectMake(17, height, 36, 18);
    adLabel.textAlignment = NSTextAlignmentCenter;
    
    // 设置广告描述(可选)
    UILabel *descLabel = [UILabel new];
    descLabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    descLabel.font = [UIFont adsy_PingFangLightFont:12];
    descLabel.textAlignment = NSTextAlignmentLeft;
    descLabel.text = adView.data.desc;
    [adView addSubview:descLabel];
    descLabel.frame = CGRectMake(17 + 36 + 4, height, [self getRootVC].view.frame.size.width - 57 - 17 - 20, 18);
}

// 2、纯图样式
- (void)setUpUnifiedOnlyImageNativeAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 设计的adView实际大小，其中宽度和高度可以自己根据自己的需求设置
    CGFloat adWidth = [self getRootVC].view.frame.size.width;
    CGFloat adHeight = adWidth / 16.0 * 9;
    adView.frame = CGRectMake(0, 0, adWidth, adHeight);
    
    // 设置主图/视频（主图可选，但强烈建议带上,如果有视频试图，则必须带上）
    CGRect mainFrame = CGRectMake(0, 0, adWidth, adHeight);
    if(adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        NSString *urlStr = adView.data.imageUrl;
        if(urlStr.length > 0) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:urlStr]]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    imageView.image = image;
                });
            });
        }
    }
    
    // 展示关闭按钮（必要）
    UIButton *closeButton = [UIButton new];
    [adView addSubview:closeButton];
    closeButton.frame = CGRectMake(adWidth - 44, 0, 44, 44);
    [closeButton setImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
    [closeButton addTarget:adView action:@selector(adsy_close) forControlEvents:UIControlEventTouchUpInside];
    
    // 显示logo图片（必要）
    if(![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) { // 优量汇（广点通）会自带logo，不需要添加
        UIImageView *logoImage = [UIImageView new];
        [adView addSubview:logoImage];
//        [adView bringSubviewToFront:logoImage];
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable image) {
            CGFloat maxWidth = 40;
            CGFloat logoHeight = maxWidth / image.size.width * image.size.height;
            logoImage.frame = CGRectMake(adWidth - maxWidth, adHeight - logoHeight, maxWidth, logoHeight);
            logoImage.image = image;
        }];
    }

}

// 3、上图下文样式
- (void)setUpUnifiedTopImageNativeAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 设计的adView实际大小，其中宽度和高度可以自己根据自己的需求设置
    CGFloat adWidth = [self getRootVC].view.frame.size.width;
    CGFloat adHeight = (adWidth - 17 * 2) / 16.0 * 9 + 70;
    adView.frame = CGRectMake(0, 0, adWidth, adHeight);
    
    // 显示logo图片（必要）
    if(![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) { // 优量汇（广点通）会自带logo，不需要添加
        UIImageView *logoImage = [UIImageView new];
        [adView addSubview:logoImage];
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable image) {
            CGFloat maxWidth = 40;
            CGFloat logoHeight = maxWidth / image.size.width * image.size.height;
            logoImage.frame = CGRectMake(adWidth - maxWidth, adHeight - logoHeight, maxWidth, logoHeight);
            logoImage.image = image;
        }];
    }
    
    // 设置主图/视频（主图可选，但强烈建议带上,如果有视频试图，则必须带上）
    CGRect mainFrame = CGRectMake(17, 0, adWidth - 17 * 2, (adWidth - 17 * 2) / 16.0 * 9);
    if(adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        NSString *urlStr = adView.data.imageUrl;
        if(urlStr.length > 0) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:urlStr]]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    imageView.image = image;
                });
            });
        }
    }
    
    // 设置广告标识（可选）
    UILabel *adLabel = [[UILabel alloc]init];
    adLabel.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
    adLabel.textColor = [UIColor adsy_colorWithHexString:@"#FFFFFF"];
    adLabel.font = [UIFont adsy_PingFangLightFont:12];
    adLabel.text = @"广告";
    [adView addSubview:adLabel];
    adLabel.frame = CGRectMake(17, (adWidth - 17 * 2) / 16.0 * 9 + 9, 36, 18);
    adLabel.textAlignment = NSTextAlignmentCenter;
    
    // 设置广告描述(可选)
    UILabel *descLabel = [UILabel new];
    descLabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    descLabel.font = [UIFont adsy_PingFangLightFont:12];
    descLabel.textAlignment = NSTextAlignmentLeft;
    descLabel.text = adView.data.desc;
    [adView addSubview:descLabel];
    descLabel.frame = CGRectMake(17 + 36 + 4, (adWidth - 17 * 2) / 16.0 * 9 + 9, [self getRootVC].view.frame.size.width - 57 - 17 - 20, 18);
    
    // 设置标题文字（可选，但强烈建议带上）
    UILabel *titlabel = [UILabel new];
    [adView addSubview:titlabel];
    titlabel.font = [UIFont adsy_PingFangMediumFont:14];
    titlabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    titlabel.numberOfLines = 2;
    titlabel.text = adView.data.title;
    CGSize textSize = [titlabel sizeThatFits:CGSizeMake(adWidth - 17 * 2, 999)];
    titlabel.frame = CGRectMake(17, (adWidth - 17 * 2) / 16.0 * 9 + 30, adWidth - 17 * 2, textSize.height);
    
    // 展示关闭按钮（必要）
    UIButton *closeButton = [UIButton new];
    [adView addSubview:closeButton];
    [adView bringSubviewToFront:closeButton];
    closeButton.frame = CGRectMake(adWidth - 44, 0, 44, 44);
    [closeButton setImage:[UIImage imageNamed:@"close"] forState:UIControlStateNormal];
    // adsy_close方法为协议中方法 直接添加target即可 无需实现
    [closeButton addTarget:adView action:@selector(adsy_close) forControlEvents:UIControlEventTouchUpInside];
    
}


static char *nativeAdKey = "nativeAd";

- (ADSuyiSDKNativeAd *)nativeAd {
  return objc_getAssociatedObject(self, &nativeAdKey);

}

- (void)setNativeAd:(ADSuyiSDKNativeAd *)nativeAd {
  objc_setAssociatedObject(self, &nativeAdKey, nativeAd, OBJC_ASSOCIATION_RETAIN_NONATOMIC);

}



@end
