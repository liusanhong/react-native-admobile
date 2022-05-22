//
//  ReactNativeAdmobile+ReWardVod.h
//  react-native-admobile
//
//  Created by zhoukai on 2022/5/22.
//

#import "ReactNativeAdmobile.h"
#import <ADSuyiSDK/ADSuyiSDKRewardvodAd.h>

NS_ASSUME_NONNULL_BEGIN

@interface ReactNativeAdmobile (ReWardVod)<ADSuyiSDKRewardvodAdDelegate>
- (void)loadRewardvodAd:(NSString *)posId;
@end

NS_ASSUME_NONNULL_END
