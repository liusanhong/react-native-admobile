//
//  RNNativeADEmitter.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/15.
//

#import "RNNativeADEmitter.h"

@implementation RNNativeADEmitter
//.m文件
+(id)allocWithZone:(NSZone *)zone {
  static RNNativeADEmitter *sharedInstance = nil;
  static dispatch_once_t onceToken;
  dispatch_once(&onceToken, ^{
    sharedInstance = [super allocWithZone:zone];
  });
  return sharedInstance;
}


RCT_EXPORT_MODULE();



- (NSArray<NSString *> *)supportedEvents
{
  return @[@"nativeViewRenderOrRegistSuccess"];
}


- (void)nativeViewRenderOrRegistSuccess:(CGSize)size{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self sendEventWithName:@"nativeViewRenderOrRegistSuccess" body:@{@"width":[NSNumber numberWithFloat:size.width ] , @"height":[NSNumber numberWithFloat:size.height]}];
    });
}


@end
