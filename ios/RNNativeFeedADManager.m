//
//  RNNativeFeedADManager.m
//  react-native-admobile
//

#import "RNNativeFeedADManager.h"
#import <ADSuyiSDK/ADSuyiSDKNativeAd.h>
#import <ADSuyiKit/UIFont+ADSuyiKit.h>
#import <ADSuyiKit/UIColor+ADSuyiKit.h>
#import <objc/runtime.h>

// 自定义关闭按钮视图，阻止事件传播到广告层
@interface RNCloseButtonView : UIView
@property (nonatomic, weak) UIView<ADSuyiAdapterNativeAdViewDelegate> *targetAdView;
@property (nonatomic, copy) dispatch_block_t onCloseAction;
@end

@implementation RNCloseButtonView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        self.userInteractionEnabled = YES;
        self.exclusiveTouch = YES;  // 独占触摸事件，防止传递给其他视图

        // 使用手势识别器，更可靠地阻止事件传播
        UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        tapGesture.cancelsTouchesInView = YES;  // 阻止事件传递给下层视图
        [self addGestureRecognizer:tapGesture];
    }
    return self;
}

- (void)handleTap:(UITapGestureRecognizer *)gesture {
    if (self.onCloseAction) {
        self.onCloseAction();
    }
}

// 扩大点击区域，实际点击区域比视觉区域大 10dp
- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event {
    CGFloat expandSize = 10;  // 扩大 10dp
    CGRect expandedBounds = CGRectInset(self.bounds, -expandSize, -expandSize);
    return CGRectContainsPoint(expandedBounds, point);
}

// 确保点击关闭按钮时，只有按钮响应
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    if ([self pointInside:point withEvent:event]) {
        return self;  // 返回自己，不传递给下层
    }
    return nil;
}

@end

@interface RNNativeFeedADView : UIView <ADSuyiSDKNativeAdDelegate>
@property (nonatomic, copy) NSString *posId;
@property (nonatomic, assign) CGFloat adWidthDp;
@property (nonatomic, assign) CGFloat adHeightDp;
@property (nonatomic, assign) CGFloat maxMediaHeightDp;
@property (nonatomic, strong) ADSuyiSDKNativeAd *nativeAd;
@property (nonatomic, weak) RNNativeFeedADView *weakSelf;
@property (nonatomic, strong) UIView<ADSuyiAdapterNativeAdViewDelegate> *currentAdView;
@property (nonatomic, strong) RNCloseButtonView *closeButtonView;
// 子视图引用，用于在图片加载后更新布局
@property (nonatomic, weak) UIImageView *mainImageView;
@property (nonatomic, weak) UIImageView *platformIconView;
@property (nonatomic, weak) UILabel *descLabelView;
@property (nonatomic, weak) UILabel *titleLabelView;
@property (nonatomic, weak) UIImageView *mainImageViewRef;
// 防止重复发送高度事件
@property (nonatomic, assign) BOOL hasSentHeightEvent;
@property (nonatomic, assign) BOOL hasPendingImageLoad;
// 直接事件回调（对齐 Android RNNativeFeedAdView 的 sendEvent 模式）
@property (nonatomic, copy) RCTDirectEventBlock onNativeRenderSuccess;
@property (nonatomic, copy) RCTDirectEventBlock onNativeFail;
@property (nonatomic, copy) RCTDirectEventBlock onNativeClose;
@end

@implementation RNNativeFeedADView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        _adWidthDp = 0;
        _adHeightDp = 0;
        _maxMediaHeightDp = 0;
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
            // 自渲染：参考安卓做法，先获取图片尺寸再渲染（避免高度变化导致抖动）
            [self setUpTopImageNativeAdViewWithImagePreload:adView];
        } else {
            // 模板渲染直接注册
            [adView adsy_registViews:@[adView]];
        }

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
    if (self.onNativeFail) {
        self.onNativeFail(@{
            @"posId": self.posId ?: @"",
            @"errorCode": @(errorModel.code),
            @"errorDescription": errorModel.errorDescription ?: @""
        });
    }
}

- (void)adsy_nativeAdViewRenderOrRegistSuccess:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 自渲染广告在 setUpTopImageNativeAdViewWithImagePreload 中已经处理完毕
    // 这里只处理模板广告，避免重复逻辑
    if (adView.renderType == ADSuyiAdapterRenderTypeNative) {
        return;
    }

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

        // 只发送一次高度事件
        if (!self.hasSentHeightEvent) {
            self.hasSentHeightEvent = YES;
            if (self.onNativeRenderSuccess) {
                self.onNativeRenderSuccess(@{
                    @"posId": self.posId ?: @"",
                    @"width": @(w),
                    @"height": @(h)
                });
            }
        }
    });
}

- (void)adsy_nativeAdViewRenderOrRegistFail:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    if (self.onNativeFail) {
        self.onNativeFail(@{
            @"posId": self.posId ?: @"",
            @"errorCode": @(-1),
            @"errorDescription": @"渲染失败"
        });
    }
}

- (void)adsy_nativeAdClicked:(ADSuyiSDKNativeAd *)nativeAd
                      adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
}

- (void)adsy_nativeAdClose:(ADSuyiSDKNativeAd *)nativeAd
                    adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.onNativeClose) {
            self.onNativeClose(@{
                @"posId": self.posId ?: @""
            });
        }
    });
}

- (void)adsy_nativeAdExposure:(ADSuyiSDKNativeAd *)nativeAd
                       adView:(__kindof UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
}

#pragma mark - Self-rendering UI: 上图下文样式

// 限制媒体高度
- (CGFloat)limitMediaHeight:(CGFloat)mediaHeight {
    if (self.maxMediaHeightDp > 0) {
        return MIN(mediaHeight, self.maxMediaHeightDp);
    }
    return mediaHeight;
}

// 获取默认媒体高度（不使用 adHeightDp，让每个广告根据实际图片高度显示不同高度）
- (CGFloat)getFallbackMediaHeight {
    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;
    CGFloat defaultHeight = adWidth * 4.0 / 3.0;  // 默认 4:3 宽高比
    return [self limitMediaHeight:defaultHeight];
}

/**
 * 参考安卓做法：先预加载图片获取尺寸，再渲染布局
 * 这样可以在渲染前就知道准确的高度，避免后续高度变化导致抖动
 */
- (void)setUpTopImageNativeAdViewWithImagePreload:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 保存当前 adView 引用
    self.currentAdView = adView;

    // 重置标志
    self.hasSentHeightEvent = NO;
    self.hasPendingImageLoad = NO;

    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;
    CGFloat mediaWidth = adWidth - 10 * 2;
    CGFloat descHeight = 36;
    CGFloat closeSize = 20;
    CGFloat titleRowHeight = closeSize;

    // 检查是否有图片需要预加载
    NSString *imageUrl = adView.data.imageUrl;
    BOOL hasMediaView = adView.data.shouldShowMediaView;

    if (hasMediaView) {
        // 视频广告：使用默认高度直接渲染
        CGFloat mediaHeight = [self getFallbackMediaHeight];
        CGFloat adHeight = mediaHeight + 10 + descHeight + 10 + titleRowHeight;
        [self renderAdView:adView
                withImage:nil
                imageWidth:0
                imageHeight:0
                mediaWidth:mediaWidth
                mediaHeight:mediaHeight
                adWidth:adWidth
                descHeight:descHeight
                closeSize:closeSize
                adHeight:adHeight];
        return;
    }

    if (imageUrl.length > 0) {
        // 标记有待加载的图片
        self.hasPendingImageLoad = YES;

        // 先在后台获取图片尺寸（参考安卓的 loadImageBitmap）
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
            NSData *imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:imageUrl]];
            UIImage *image = [UIImage imageWithData:imageData];
            dispatch_async(dispatch_get_main_queue(), ^{
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (!strongSelf) {
                    return;  // 广告视图已被释放
                }

                CGFloat imageWidth = image ? image.size.width : 0;
                CGFloat imageHeight = image ? image.size.height : 0;

                // 根据图片尺寸计算媒体高度
                CGFloat calculatedMediaHeight = [strongSelf getFallbackMediaHeight];
                if (imageWidth > 0 && imageHeight > 0 && mediaWidth > 0) {
                    calculatedMediaHeight = MAX(1, round(mediaWidth * imageHeight / imageWidth));
                    calculatedMediaHeight = [strongSelf limitMediaHeight:calculatedMediaHeight];
                    NSLog(@"[iOS Ad] Image loaded: %.0fx%.0f, calculated media height: %.0f (max limit: %.0f)", imageWidth, imageHeight, calculatedMediaHeight, strongSelf.maxMediaHeightDp);
                } else {
                    NSLog(@"[iOS Ad] Using fallback media height: %.0f (image size: %.0fx%.0f)", calculatedMediaHeight, imageWidth, imageHeight);
                }

                CGFloat adHeight = calculatedMediaHeight + 10 + descHeight + 10 + titleRowHeight;
                NSLog(@"[iOS Ad] Final ad height: %.0f (posId: %@)", adHeight, strongSelf.posId);

                // 用已知的高度渲染布局
                [strongSelf renderAdView:adView
                                withImage:image
                              imageWidth:imageWidth
                             imageHeight:imageHeight
                               mediaWidth:mediaWidth
                              mediaHeight:calculatedMediaHeight
                                  adWidth:adWidth
                               descHeight:descHeight
                                closeSize:closeSize
                                  adHeight:adHeight];
            });
        });
    } else {
        // 无图片：使用默认高度
        CGFloat mediaHeight = [self getFallbackMediaHeight];
        CGFloat adHeight = mediaHeight + 10 + descHeight + 10 + titleRowHeight;
        [self renderAdView:adView
                withImage:nil
                imageWidth:0
                imageHeight:0
                mediaWidth:mediaWidth
                mediaHeight:mediaHeight
                adWidth:adWidth
                descHeight:descHeight
                closeSize:closeSize
                adHeight:adHeight];
    }
}

/**
 * 渲染广告视图（此时已知所有尺寸，高度不会再变化）
 */
- (void)renderAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView
            withImage:(UIImage *)image
          imageWidth:(CGFloat)imageWidth
         imageHeight:(CGFloat)imageHeight
           mediaWidth:(CGFloat)mediaWidth
          mediaHeight:(CGFloat)mediaHeight
              adWidth:(CGFloat)adWidth
           descHeight:(CGFloat)descHeight
           closeSize:(CGFloat)closeSize
             adHeight:(CGFloat)adHeight {

    // 先清空旧的子视图
    for (UIView *sub in self.subviews) {
        [sub removeFromSuperview];
    }

    // 设置广告视图的 frame 并添加到自身（这里完成，adsy_nativeAdViewRenderOrRegistSuccess 不需要再做）
    adView.frame = CGRectMake(0, 0, adWidth, adHeight);
    [self addSubview:adView];
    self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, adWidth, adHeight);

    // ========== 1. 媒体区域 ==========
    CGRect mainFrame = CGRectMake(10, 0, mediaWidth, mediaHeight);
    UIView *mediaViewForClick = nil;

    if (adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        mediaView.userInteractionEnabled = NO;
        mediaViewForClick = mediaView;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#F5F5F5"];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        imageView.userInteractionEnabled = NO;
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        self.mainImageViewRef = imageView;
        mediaViewForClick = imageView;

        if (image) {
            // 计算居中位置（当高度被限制时）
            CGFloat actualMediaWidth = mediaWidth;
            CGFloat actualMediaHeight = mediaHeight;

            CGFloat originalCalculatedHeight = (imageWidth > 0 && imageHeight > 0) ? (mediaWidth * imageHeight / imageWidth) : mediaHeight;
            if (originalCalculatedHeight > mediaHeight && mediaHeight == self.maxMediaHeightDp) {
                actualMediaWidth = mediaHeight * imageWidth / imageHeight;
            }

            CGFloat xOffset = (mediaWidth - actualMediaWidth) / 2 + 10;
            imageView.frame = CGRectMake(xOffset, 0, actualMediaWidth, actualMediaHeight);
            imageView.image = image;
        }
    }

    // ========== 2. 平台标识图标 ==========
    if (![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) {
        UIImageView *platformIcon = [UIImageView new];
        self.platformIconView = platformIcon;
        [adView addSubview:platformIcon];
        CGFloat iconWidth = 46;
        CGFloat iconHeight = 18;
        platformIcon.frame = CGRectMake(adWidth - 10 - iconWidth, mediaHeight - iconHeight, iconWidth, iconHeight);
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable img) {
            if (img) {
                platformIcon.image = img;
            }
        }];
    }

    // ========== 3. 描述文本 ==========
    UILabel *descLabel = [[UILabel alloc] init];
    descLabel.textColor = [UIColor adsy_colorWithHexString:@"#555555"];
    descLabel.font = [UIFont systemFontOfSize:12];
    descLabel.textAlignment = NSTextAlignmentLeft;
    descLabel.text = adView.data.desc;
    descLabel.numberOfLines = 2;
    descLabel.lineBreakMode = NSLineBreakByTruncatingTail;
    self.descLabelView = descLabel;
    [adView addSubview:descLabel];

    CGFloat descTop = mediaHeight + 10;
    descLabel.frame = CGRectMake(10, descTop, adWidth - 10 * 2, descHeight);

    // ========== 4. 标题行 ==========
    CGFloat titleTop = descTop + descHeight + 10;

    UILabel *titleLabel = [[UILabel alloc] init];
    titleLabel.font = [UIFont systemFontOfSize:12];
    titleLabel.textColor = [UIColor adsy_colorWithHexString:@"#555555"];
    titleLabel.numberOfLines = 1;
    titleLabel.lineBreakMode = NSLineBreakByTruncatingTail;
    titleLabel.text = adView.data.title;
    self.titleLabelView = titleLabel;
    [adView addSubview:titleLabel];

    CGFloat titleLeft = 10;
    CGFloat titleRight = adWidth - 10 - closeSize - 10;
    titleLabel.frame = CGRectMake(titleLeft, titleTop, titleRight - titleLeft, closeSize);

    // ========== 5. 关闭按钮 ==========
    RNCloseButtonView *closeButton = [[RNCloseButtonView alloc] initWithFrame:CGRectMake(adWidth - 10 - closeSize, 10, closeSize, closeSize)];
    closeButton.targetAdView = adView;
    self.closeButtonView = closeButton;

    __weak typeof(self) weakSelf = self;
    closeButton.onCloseAction = ^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf && strongSelf.currentAdView) {
            [strongSelf.currentAdView adsy_close];
            if (strongSelf.onNativeClose) {
                strongSelf.onNativeClose(@{
                    @"posId": strongSelf.posId ?: @""
                });
            }
        }
    };

    // 绘制 X 图标
    UIGraphicsBeginImageContextWithOptions(closeButton.frame.size, NO, [UIScreen mainScreen].scale);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGFloat padding = 4;
    CGContextSetStrokeColorWithColor(context, [UIColor adsy_colorWithHexString:@"#333333"].CGColor);
    CGContextSetLineWidth(context, 2);

    CGContextMoveToPoint(context, padding, padding);
    CGContextAddLineToPoint(context, closeSize - padding, closeSize - padding);
    CGContextMoveToPoint(context, closeSize - padding, padding);
    CGContextAddLineToPoint(context, padding, closeSize - padding);

    CGContextStrokePath(context);
    UIImage *closeIcon = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    UIImageView *closeImageView = [[UIImageView alloc] initWithFrame:closeButton.bounds];
    closeImageView.image = closeIcon;
    closeImageView.contentMode = UIViewContentModeCenter;
    [closeButton addSubview:closeImageView];

    [adView addSubview:closeButton];

    // 标记已发送高度事件，防止 adsy_registViews 触发的代理回调重复发送
    self.hasPendingImageLoad = NO;
    self.hasSentHeightEvent = YES;

    // 注册交互（会触发 adsy_nativeAdViewRenderOrRegistSuccess，但由于已设置标志位，不会重复发送事件）
    if (mediaViewForClick) {
        [adView adsy_registViews:@[mediaViewForClick]];
    } else {
        [adView adsy_registViews:@[adView]];
    }

    // 立即发送高度事件（在 adsy_registViews 之前设置标志位，确保代理回调不会重复发送）
    if (self.onNativeRenderSuccess) {
        self.onNativeRenderSuccess(@{
            @"posId": self.posId ?: @"",
            @"width": @(adWidth),
            @"height": @(adHeight)
        });
    }
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

RCT_CUSTOM_VIEW_PROPERTY(adHeight, NSNumber, RNNativeFeedADView) {
    CGFloat height = [json floatValue];
    view.adHeightDp = height;
}

RCT_CUSTOM_VIEW_PROPERTY(maxMediaHeight, NSNumber, RNNativeFeedADView) {
    CGFloat height = [json floatValue];
    view.maxMediaHeightDp = height;
}

RCT_EXPORT_VIEW_PROPERTY(onNativeRenderSuccess, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onNativeFail, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onNativeClose, RCTDirectEventBlock)

@end
