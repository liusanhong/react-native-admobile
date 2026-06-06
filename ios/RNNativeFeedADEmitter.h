//
//  RNNativeFeedADEmitter.h
//  react-native-admobile
//

#import "RCTEventEmitter.h"

NS_ASSUME_NONNULL_BEGIN

@interface RNNativeFeedADEmitter : RCTEventEmitter<RCTBridgeModule>

- (void)nativeFeedAdRenderSuccess:(NSDictionary *)sizeInfo;
- (void)nativeFeedAdFail:(NSDictionary *)errorInfo;
- (void)nativeFeedAdClose:(NSDictionary *)info;

@end

NS_ASSUME_NONNULL_END
