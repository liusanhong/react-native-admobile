// ReactNativeAdmobile.m

#import "ReactNativeAdmobile.h"
#import "ReactNativeAdmobile+ReWardVod.h"

@interface ReactNativeAdmobile ()

@end


@implementation ReactNativeAdmobile

RCT_EXPORT_MODULE()



RCT_EXPORT_METHOD(sampleMethod:(NSString *)stringArgument numberParameter:(nonnull NSNumber *)numberArgument callback:(RCTResponseSenderBlock)callback)
{
    // TODO: Implement some actually useful functionality
    callback(@[[NSString stringWithFormat: @"numberArgument: %@ stringArgument: %@", numberArgument, stringArgument]]);
}


RCT_EXPORT_METHOD(rewardVodAd:(NSString*)vodId
                 resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    self.resolve = resolve;
    self.reject= reject;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self loadRewardvodAd:vodId];
    });

}

@end
