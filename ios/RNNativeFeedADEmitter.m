//
//  RNNativeFeedADEmitter.m
//  react-native-admobile
//

#import "RNNativeFeedADEmitter.h"

@implementation RNNativeFeedADEmitter

+ (id)allocWithZone:(NSZone *)zone {
    static RNNativeFeedADEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"nativeFeedAdRenderSuccess", @"nativeFeedAdFail", @"nativeFeedAdClose"];
}

- (void)nativeFeedAdRenderSuccess:(NSDictionary *)sizeInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self sendEventWithName:@"nativeFeedAdRenderSuccess" body:sizeInfo];
    });
}

- (void)nativeFeedAdFail:(NSDictionary *)errorInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self sendEventWithName:@"nativeFeedAdFail" body:errorInfo];
    });
}

- (void)nativeFeedAdClose {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self sendEventWithName:@"nativeFeedAdClose" body:nil];
    });
}

@end
