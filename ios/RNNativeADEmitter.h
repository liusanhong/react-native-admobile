//
//  RNNativeADEmitter.h
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/15.
//

#import "RCTEventEmitter.h"

NS_ASSUME_NONNULL_BEGIN

@interface RNNativeADEmitter : RCTEventEmitter<RCTBridgeModule>

- (void)nativeViewRenderOrRegistSuccess:(CGSize)size;
- (void)nativeViewClostButtonClick;

@end

NS_ASSUME_NONNULL_END
