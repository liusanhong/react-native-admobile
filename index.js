// main index.js

import { NativeModules } from 'react-native';

const { ReactNativeAdmobile } = NativeModules;



 const ReactNativeAdmobileUtils ={

    initAd(appid) {
    return ReactNativeAdmobile.initAd(appid);
  },
  /**
   * 开屏广告
   * @param adId
   * @param onSuccess
   * @param onError
   */
  splashAd(adId,onSuccess,onError) {
    ReactNativeAdmobile.splashAd(adId,onSuccess,onError);
  },
  /**
   * 激励广告
   * @param adId
   * @param onSuccess
   * @param onError
   */
  rewardVodAd(adId,onSuccess,onError) {
    ReactNativeAdmobile.rewardVodAd(adId,onSuccess,onError);
  },

  /**
   * 插屏广告
   * @param adId
   * @param onSuccess
   * @param onError
   */
  intertitialAd(adId,onSuccess,onError) {
    ReactNativeAdmobile.intertitialAd(adId,onSuccess,onError);
  },

  setPersonalizedAdEnabled(personalizedAdEnabled=false) {
    ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
  }

}

export default ReactNativeAdmobileUtils;


