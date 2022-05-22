// ReactNativeAdmobile.h

#import <React/RCTBridgeModule.h>

@interface ReactNativeAdmobile : NSObject <RCTBridgeModule>

@property (nonatomic, strong) RCTPromiseResolveBlock resolve;
@property (nonatomic, strong) RCTPromiseRejectBlock reject;
@end
