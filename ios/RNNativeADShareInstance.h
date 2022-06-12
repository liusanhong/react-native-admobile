//
//  RNNativeADShareInstance.h
//  react-native-admobile
//
//  Created by zhoukai on 2022/6/11.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface RNNativeADShareInstance : NSObject
@property(nonatomic, strong) UIView *adView;

+(RNNativeADShareInstance *)shareInstance;
-(id) initWith:(UIView *) view ;

@end

NS_ASSUME_NONNULL_END
