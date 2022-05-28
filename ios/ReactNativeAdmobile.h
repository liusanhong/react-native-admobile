// ReactNativeAdmobile.h

#import <React/RCTBridgeModule.h>

@interface ReactNativeAdmobile : NSObject <RCTBridgeModule>
- (UIViewController*) getRootVC;
//使用promise的方式
@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;
//使用回调的形式
@property (nonatomic, strong) RCTResponseSenderBlock onSuccess;
@property (nonatomic, strong) RCTResponseSenderBlock onError;
@end
