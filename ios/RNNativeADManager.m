//
//  RNNativeADManager.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/11.
//

#import "RNNativeADManager.h"
#import "RNNativeADShareInstance.h"
@implementation RNNativeADManager



RCT_EXPORT_MODULE(RNNativeAD)



- (UIView *)view
{
    UIView *adview = [RNNativeADShareInstance shareInstance].adView;
    [adview setFrame:CGRectMake(0, 0, [adview bounds].size.width, [adview bounds].size.height)];
    
    UIView * view1 = [[UIView alloc]initWithFrame:(CGRect)CGRectMake(0, 0, [adview bounds].size.width, [adview bounds].size.height) ];
    [view1 setBackgroundColor:[UIColor redColor]];
    return  adview;
}


@end
