//
//  RNBannerADEmitter.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/12.
//

#import "RNBannerADEmitter.h"

@implementation RNBannerADEmitter

//.m文件
+(id)allocWithZone:(NSZone *)zone {
  static RNBannerADEmitter *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [super allocWithZone:zone];
  });
  return sharedInstance;
}


RCT_EXPORT_MODULE();



- (NSArray<NSString *> *)supportedEvents
{
  return @[@"bannerViewFailAction"];
}

- (void)bannerViewFailToReceived {
    dispatch_async(dispatch_get_main_queue(), ^{
    [self sendEventWithName:@"bannerViewFailAction" body:nil];
    });
}
@end
