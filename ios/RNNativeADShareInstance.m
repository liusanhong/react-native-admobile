//
//  RNNativeADShareInstance.m
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/11.
//

#import "RNNativeADShareInstance.h"

@implementation RNNativeADShareInstance

/*方式二： 利用dispatch_once方法实现    dispatch_once方法比加锁要提高速度*/
 +(RNNativeADShareInstance *)shareInstance
{
    static RNNativeADShareInstance *person = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        person = [[super allocWithZone:NULL] init];
    });
    return person;
}
 
/*因为alloc方法会调用allocWithZone方法，为了避免单例遗漏，在
 allocWithZone 调用创建单例对象的类方法
 */
+(instancetype) allocWithZone:(struct _NSZone *)zone
{
  return [self shareInstance];
}
 
-(id) initWith:(UIView *) view
{
    if (self = [super init]) {
        self.adView = view;
    }
    return self;
}


@end
