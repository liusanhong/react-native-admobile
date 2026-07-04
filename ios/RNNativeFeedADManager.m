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
@property (nonatomic, strong) RNNativeFeedADEmitter *emitter;
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
            // 自渲染：拼装原生 UI（内部会在关闭按钮创建后注册交互）
            [self setUpTopImageNativeAdView:adView];
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
    [self.emitter nativeFeedAdFail:@{
        @"posId": self.posId ?: @"",
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

        // 如果有待加载的图片，延迟发送高度事件（等图片加载完成后再发）
        // 避免多次发送导致列表跳动
        if (!self.hasPendingImageLoad && !self.hasSentHeightEvent) {
            self.hasSentHeightEvent = YES;
            [self.emitter nativeFeedAdRenderSuccess:@{
                @"posId": self.posId ?: @"",
                @"width": @(w),
                @"height": @(h)
            }];
        }
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
        [self.emitter nativeFeedAdClose:@{
            @"posId": self.posId ?: @""
        }];
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

// 获取默认媒体高度
- (CGFloat)getFallbackMediaHeight {
    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;
    CGFloat defaultHeight = adWidth * 4.0 / 3.0;
    if (self.adHeightDp > 0) {
        defaultHeight = self.adHeightDp;
    }
    return [self limitMediaHeight:defaultHeight];
}

- (void)setUpTopImageNativeAdView:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView {
    // 保存当前 adView 引用，用于后续排除关闭按钮
    self.currentAdView = adView;

    // 重置标志
    self.hasSentHeightEvent = NO;
    self.hasPendingImageLoad = NO;

    CGFloat adWidth = self.adWidthDp > 0 ? self.adWidthDp : [UIScreen mainScreen].bounds.size.width;

    // 使用默认高度初始化，后续根据图片实际尺寸调整（匹配 Android 实现）
    CGFloat mediaWidth = adWidth - 10 * 2;  // 左右各 10dp margin
    CGFloat mediaHeight = [self getFallbackMediaHeight];  // 应用 maxHeight 限制
    CGFloat descHeight = 36;  // 描述行高度 (2行)
    CGFloat closeSize = 20;  // 关闭按钮尺寸
    CGFloat titleRowHeight = closeSize;  // 标题行高度
    CGFloat adHeight = mediaHeight + 10 + descHeight + 10 + titleRowHeight;  // 总高度

    adView.frame = CGRectMake(0, 0, adWidth, adHeight);

    // ========== 1. 媒体区域 (图片/视频) ==========
    CGRect mainFrame = CGRectMake(10, 0, mediaWidth, mediaHeight);
    UIView *mediaViewForClick = nil;  // 保存媒体视图用于注册点击

    if (adView.data.shouldShowMediaView) {
        UIView *mediaView = [adView adsy_mediaViewForWidth:mainFrame.size.width];
        mediaView.frame = mainFrame;
        mediaView.userInteractionEnabled = NO;
        mediaViewForClick = mediaView;
        [adView addSubview:mediaView];
    } else {
        UIImageView *imageView = [UIImageView new];
        imageView.backgroundColor = [UIColor adsy_colorWithHexString:@"#F5F5F5"];
        imageView.contentMode = UIViewContentModeScaleAspectFit;  // 匹配 Android 的 FIT_CENTER：保持宽高比，完整显示图片
        imageView.userInteractionEnabled = NO;  // 禁止用户交互
        [adView addSubview:imageView];
        imageView.frame = mainFrame;
        self.mainImageViewRef = imageView;  // 保存引用用于后续更新
        mediaViewForClick = imageView;

        NSString *urlStr = adView.data.imageUrl;
        if (urlStr.length > 0) {
            // 标记有待加载的图片，延迟发送高度事件
            self.hasPendingImageLoad = YES;

            // 先获取图片尺寸，再根据宽高比调整布局（匹配 Android 的 calculateMediaHeight 逻辑）
            __weak typeof(self) weakSelf = self;
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                NSData *imageData = [NSData dataWithContentsOfURL:[NSURL URLWithString:urlStr]];
                UIImage *image = [UIImage imageWithData:imageData];
                dispatch_async(dispatch_get_main_queue(), ^{
                    __strong typeof(weakSelf) strongSelf = weakSelf;
                    if (strongSelf && image && strongSelf.mainImageViewRef) {
                        CGFloat imageWidth = image.size.width;
                        CGFloat imageHeight = image.size.height;

                        // 根据图片实际宽高比重新计算媒体高度（匹配 Android）
                        CGFloat calculatedMediaHeight = mediaHeight;
                        if (imageWidth > 0 && imageHeight > 0 && mediaWidth > 0) {
                            calculatedMediaHeight = MAX(1, round(mediaWidth * imageHeight / imageWidth));
                            // 应用 maxHeight 限制
                            calculatedMediaHeight = [strongSelf limitMediaHeight:calculatedMediaHeight];
                        }

                        // 更新图片视图 frame 和 image（匹配 Android FIT_CENTER 行为）
                        // 当高度被 maxHeight 限制时，需要调整宽度来保持宽高比
                        CGFloat actualMediaWidth = mediaWidth;
                        CGFloat actualMediaHeight = calculatedMediaHeight;

                        // 如果高度被 maxHeight 限制了，需要反推正确的宽度以保持图片宽高比
                        CGFloat originalCalculatedHeight = mediaWidth * imageHeight / imageWidth;
                        if (originalCalculatedHeight > calculatedMediaHeight && calculatedMediaHeight == strongSelf.maxMediaHeightDp) {
                            // 高度被限制了，反推宽度
                            actualMediaWidth = calculatedMediaHeight * imageWidth / imageHeight;
                        }

                        // 设置居中的 frame
                        CGFloat xOffset = (mediaWidth - actualMediaWidth) / 2 + 10;
                        strongSelf.mainImageViewRef.frame = CGRectMake(xOffset, 0, actualMediaWidth, actualMediaHeight);
                        strongSelf.mainImageViewRef.image = image;
                        strongSelf.mainImageViewRef.contentMode = UIViewContentModeScaleAspectFit;

                        // 直接更新布局，不使用动画
                        [strongSelf updateAdViewLayout:adView
                                       withMediaHeight:calculatedMediaHeight
                                            mediaWidth:mediaWidth
                                               adWidth:adWidth
                                            descHeight:descHeight
                                            closeSize:closeSize];
                    }
                });
            });
        }
    }

    // ========== 2. 平台标识图标 (右下角) ==========
    if (![adView.adsy_platform isEqualToString:ADSuyiAdapterPlatformGDT]) {
        UIImageView *platformIcon = [UIImageView new];
        self.platformIconView = platformIcon;
        [adView addSubview:platformIcon];
        CGFloat iconWidth = 46;
        CGFloat iconHeight = 18;
        platformIcon.frame = CGRectMake(adWidth - 10 - iconWidth, mediaHeight - iconHeight, iconWidth, iconHeight);
        [adView adsy_platformLogoImageDarkMode:NO loadImageBlock:^(UIImage * _Nullable image) {
            if (image) {
                platformIcon.image = image;
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

    // ========== 4. 标题行 (标题 + 关闭按钮) ==========
    CGFloat titleTop = descTop + descHeight + 10;

    // 标题
    UILabel *titleLabel = [[UILabel alloc] init];
    titleLabel.font = [UIFont systemFontOfSize:12];
    titleLabel.textColor = [UIColor adsy_colorWithHexString:@"#555555"];
    titleLabel.numberOfLines = 1;
    titleLabel.lineBreakMode = NSLineBreakByTruncatingTail;
    titleLabel.text = adView.data.title;
    self.titleLabelView = titleLabel;
    [adView addSubview:titleLabel];

    CGFloat titleLeft = 10;
    CGFloat titleRight = adWidth - 10 - closeSize - 10;  // 右边距 - 关闭按钮 - 间距
    titleLabel.frame = CGRectMake(titleLeft, titleTop, titleRight - titleLeft, closeSize);

    // 关闭按钮 (X 图标) - 放在右上角，使用自定义视图防止触发广告点击
    CGFloat closeTop = 10;  // 距离顶部 10dp
    RNCloseButtonView *closeButton = [[RNCloseButtonView alloc] initWithFrame:CGRectMake(adWidth - 10 - closeSize, closeTop, closeSize, closeSize)];
    closeButton.targetAdView = adView;

    // 保存关闭按钮引用，以便后续移除
    self.closeButtonView = closeButton;

    // 设置关闭回调
    __weak typeof(self) weakSelf = self;
    closeButton.onCloseAction = ^{
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if (strongSelf && strongSelf.currentAdView) {
            [strongSelf.currentAdView adsy_close];
            [strongSelf.emitter nativeFeedAdClose:@{
                @"posId": strongSelf.posId ?: @""
            }];
        }
    };

    // 绘制 X 图标
    UIGraphicsBeginImageContextWithOptions(closeButton.frame.size, NO, [UIScreen mainScreen].scale);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGFloat padding = 4;
    CGContextSetStrokeColorWithColor(context, [UIColor adsy_colorWithHexString:@"#333333"].CGColor);
    CGContextSetLineWidth(context, 2);

    // 第一条线 (左上到右下)
    CGContextMoveToPoint(context, padding, padding);
    CGContextAddLineToPoint(context, closeSize - padding, closeSize - padding);

    // 第二条线 (右上到左下)
    CGContextMoveToPoint(context, closeSize - padding, padding);
    CGContextAddLineToPoint(context, padding, closeSize - padding);

    CGContextStrokePath(context);
    UIImage *closeIcon = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    UIImageView *closeImageView = [[UIImageView alloc] initWithFrame:closeButton.bounds];
    closeImageView.image = closeIcon;
    closeImageView.contentMode = UIViewContentModeCenter;
    [closeButton addSubview:closeImageView];

    // 将关闭按钮添加到 adView
    [adView addSubview:closeButton];

    // 注册交互：只注册媒体视图（图片/视频区域），不包含关闭按钮
    // 只有点击媒体区域才会触发广告点击
    if (mediaViewForClick) {
        [adView adsy_registViews:@[mediaViewForClick]];
    } else {
        // 如果没有媒体视图，注册整个 adView
        [adView adsy_registViews:@[adView]];
    }
}

/**
 * 在获取图片实际尺寸后，更新广告视图布局
 * 匹配 Android 的 calculateMediaHeight 逻辑
 * 移除动画以避免瀑布流闪烁
 */
- (void)updateAdViewLayout:(UIView<ADSuyiAdapterNativeAdViewDelegate> *)adView
           withMediaHeight:(CGFloat)mediaHeight
                mediaWidth:(CGFloat)mediaWidth
                   adWidth:(CGFloat)adWidth
                descHeight:(CGFloat)descHeight
                closeSize:(CGFloat)closeSize {
    // 更新整个 adView 的高度
    CGFloat titleRowHeight = closeSize;
    CGFloat newAdHeight = mediaHeight + 10 + descHeight + 10 + titleRowHeight;
    CGRect newAdViewFrame = adView.frame;
    newAdViewFrame.size.height = newAdHeight;
    adView.frame = newAdViewFrame;

    // 更新平台图标位置
    if (self.platformIconView) {
        CGFloat iconWidth = 46;
        CGFloat iconHeight = 18;
        self.platformIconView.frame = CGRectMake(adWidth - 10 - iconWidth, mediaHeight - iconHeight, iconWidth, iconHeight);
    }

    // 更新描述文本位置
    if (self.descLabelView) {
        CGFloat descTop = mediaHeight + 10;
        self.descLabelView.frame = CGRectMake(10, descTop, adWidth - 10 * 2, descHeight);
    }

    // 更新标题和关闭按钮位置
    CGFloat titleTop = (mediaHeight + 10 + descHeight + 10);
    if (self.titleLabelView) {
        CGFloat titleLeft = 10;
        CGFloat titleRight = adWidth - 10 - closeSize - 10;
        self.titleLabelView.frame = CGRectMake(titleLeft, titleTop, titleRight - titleLeft, closeSize);
    }

    if (self.closeButtonView) {
        CGRect closeFrame = self.closeButtonView.frame;
        closeFrame.origin.y = 10;
        self.closeButtonView.frame = closeFrame;
    }

    // 重置待加载标志并通知 JS 层高度变化（只发送一次）
    self.hasPendingImageLoad = NO;
    if (!self.hasSentHeightEvent) {
        self.hasSentHeightEvent = YES;
        [self.emitter nativeFeedAdRenderSuccess:@{
            @"posId": self.posId ?: @"",
            @"width": @(adWidth),
            @"height": @(newAdHeight)
        }];
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

@end
