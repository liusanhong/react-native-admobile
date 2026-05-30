//
//  RNNativeFeedADManager.m
//  react-native-admobile
//

#import "RNNativeFeedADManager.h"
#import "RNNativeFeedADEmitter.h"
#import <ADSuyiSDK/ADSuyiSDKNativeAd.h>
#import <ADSuyiKit/UIFont+ADSuyiKit.h>
#import <ADSuyiKit/UIColor+ADSuyiKit.h>
#import <objc/runtime.h>

@interface RNNativeFeedADView : UIView <ADSuyiSDKNativeAdDelegate>
@property (nonatomic, copy) NSString *posId;
@property (nonatomic, assign) CGFloat adWidthDp;
@property (nonatomic, strong) ADSuyiSDKNativeAd *nativeAd;
@property (nonatomic, strong) RNNativeFeedADEmitter *emitter;
@property (nonatomic, weak) RNNativeFeedADView *weakSelf;
@end

@implementation RNNativeFeedADView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        _adWidthDp = 0;
    }
    return self;
}

- (void)setPosId:(NSString *)posId {
    _posId = posId;
    if (posId.length > 0) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self loadAd];
        });
    }
}

- (void)loadAd {
    if (self.posId.length == 0) return;

    self.emitter = [RNNativeFeedADEmitter allocWithZone:nil];

    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;
    self.nativeAd = [[ADSuyiSDKNativeAd alloc] initWithAdSize:CGSizeMake(adWidth, 0)];
    self.nativeAd.delegate = self;

    // 获取当前 ViewController
    UIViewController *vc = [self getCurrentViewController];
    self.nativeAd.controller = vc;
    self.nativeAd.posId = self.posId;
    [self.nativeAd load:1];
}

- (UIViewController *)getCurrentViewController {
    UIResponder *responder = self;
    while (responder) {
        if ([responder isKindOfClass:[UIViewController class]]) {
            return (UIViewController *)responder;
        }
        responder = [responder nextResponder];
    }
    UIViewController *rootVC = [UIApplication sharedApplication].keyWindow.rootViewController;
    return rootVC;
}

#pragma mark - ADSuyiSDKNativeAdDelegate

- (void)adsy_nativeAdSucessToLoad:(ADSuyiSDKNativeAd *)nativeAd
                      adViewArray:(NSArray<__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *> *)adViewArray {
    for (UIView<ADSuyiAdapterNativeAdViewDelegate> *adView in adViewArray) {
        if (adView.renderType == ADSuyiAdapterRenderTypeNative) {
            // 自渲染：拼装原生 UI
            [self setUpTopImageNativeAdView:adView];
        }
        // 注册交互
        [adView adsy_registViews:@[adView]];

        // 广点通视频广告特殊处理
        if ([adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]
            && adView.renderType == ADSuyiAdapterRenderTypeNative
            && adView.data.shouldShowMediaView) {
            UIView *mediaView = [adView adsy_mediaViewForWidth:0.0];
            mediaView.userInteractionEnabled = NO;
        }
    }
}

- (void)adsy_nativeAdFailToLoad:(ADSuyiSDKNativeAd *)nativeAd
                     errorModel:(ADSuyiAdapterErrorDefine *)errorModel {
    [self.emitter nativeFeedAdFail:@{
        @"errorCode": @(errorModel.code),
        @"errorDescription": errorModel.errorDescription ?: @""
    }];
}

- (void)adsy_nativeAdViewRenderOrRegistSuccess:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    dispatch_async(dispatch_get_main_queue(), ^{
        // 清空旧的子视图
        for (UIView *sub in self.subviews) {
            [sub removeFromSuperview];
        }

        // 将广告视图添加到自身
        CGFloat w = adView.bounds.size.width;
        CGFloat h = adView.bounds.size.height;
        adView.frame = CGRectMake(0, 0, w, h);
        [self addSubview:adView];
        self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, w, h);

        // 发送渲染成功事件
        [self.emitter nativeFeedAdRenderSuccess:@{
            @"width": @(w),
            @"height": @(h)
        }];
    });
}

- (void)adsy_nativeAdViewRenderOrRegistFail:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    [self.emitter nativeFeedAdFail:@{
        @"errorCode": @(-1),
        @"errorDescription": @"渲染失败"
    }];
}

- (void)adsy_nativeAdClicked:(ADSuyiSDKNativeAd *)nativeAd
                      adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
}

- (void)adsy_nativeAdClose:(ADSuyiSDKNativeAd *)nativeAd
                    adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.emitter nativeFeedAdClose];
    });
}

- (void)adsy_nativeAdExposure:(ADSuyiSDKNativeAd *)nativeAd
                       adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
}

#pragma mark - Self-rendering UI: 上图下文样式

- (void)setUpTopImageNativeAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;
    CGFloat adHeight = (adWidth - 17 * 2) / 16.0 * 9 + 70;
    adView.frame = CGRectMake(0, 0, adWidth, adHeight);

    // Logo
    if (![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) {
        UIImageView *logoImage = [UIImageView new];
        [adView addSubview:logoImage];
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable image) {
            CGFloat maxWidth = 40;
            CGFloat logoHeight = maxWidth / image.size.width * image.size.height;
            logoImage.frame = CGRectMake(adWidth - maxWidth, adHeight - logoHeight, maxWidth, logoHeight);
            logoImage.image = image;
        }];
    }

    // 主图/视频
    CGRect mainFrame = CGRectMake(17, 0, adWidth - 17 * 2, (adWidth - 17 * 2) / 16.0 * 9);
    if (adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        NSString *urlStr = adView.data.imageUrl;
        if (urlStr.length > 0) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:urlStr]]];
                dispatch_async(dispatch_get_main_queue(), ^{
                    imageView.image = image;
                });
            });
        }
    }

    // 广告标识
    UILabel *adLabel = [[UILabel alloc] init];
    adLabel.backgroundColor = [UIColor adsy_colorWithHexString:@"#CCCCCC"];
    adLabel.textColor = [UIColor adsy_colorWithHexString:@"#FFFFFF"];
    adLabel.font = [UIFont adsy_PingFangLightFont:12];
    adLabel.text = @"广告";
    [adView addSubview:adLabel];
    adLabel.frame = CGRectMake(17, (adWidth - 17 * 2) / 16.0 * 9 + 9, 36, 18);
    adLabel.textAlignment = NSTextAlignmentCenter;

    // 描述
    UILabel *descLabel = [UILabel new];
    descLabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    descLabel.font = [UIFont adsy_PingFangLightFont:12];
    descLabel.textAlignment = NSTextAlignmentLeft;
    descLabel.text = adView.data.desc;
    [adView addSubview:descLabel];
    descLabel.frame = CGRectMake(17 + 36 + 4, (adWidth - 17 * 2) / 16.0 * 9 + 9, adWidth - 57 - 17 - 20, 18);

    // 标题
    UILabel *titleLabel = [UILabel new];
    [adView addSubview:titleLabel];
    titleLabel.font = [UIFont adsy_PingFangMediumFont:14];
    titleLabel.textColor = [UIColor adsy_colorWithHexString:@"#333333"];
    titleLabel.numberOfLines = 2;
    titleLabel.text = adView.data.title;
    CGSize textSize = [titleLabel sizeThatFits:CGSizeMake(adWidth - 17 * 2, 999)];
    titleLabel.frame = CGRectMake(17, (adWidth - 17 * 2) / 16.0 * 9 + 30, adWidth - 17 * 2, textSize.height);

    // 关闭按钮
    UIButton *closeButton = [UIButton new];
    [adView addSubview:closeButton];
    [adView bringSubviewToFront:closeButton];
    closeButton.frame = CGRectMake(adWidth - 54, 0, 54, 18);
    closeButton.titleLabel.font = [UIFont systemFontOfSize:12];
    [closeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [closeButton setBackgroundColor:[UIColor colorWithRed:69/255 green:69/255 blue:69/255 alpha:0.7]];
    [closeButton setTitle:@"关闭" forState:UIControlStateNormal];
    [closeButton addTarget:adView action:@selector(adsy_close) forControlEvents:UIControlEventTouchDown];
}

@end

#pragma mark - ViewManager

@implementation RNNativeFeedADManager

RCT_EXPORT_MODULE(RNNativeFeedAD)

- (UIView *)view {
    return [[RNNativeFeedADView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(posId, NSString)

RCT_CUSTOM_VIEW_PROPERTY(adWidth, NSNumber, RNNativeFeedADView) {
    CGFloat width = [json floatValue];
    view.adWidthDp = width;
}

@end
