// main index.js

import { NativeModules } from 'react-native';

const { ReactNativeAdmobile } = NativeModules;

export default ReactNativeAdmobile;

export function initAd(appid) {
  return ReactNativeAdmobile.initAd(appid);
}

export function splashAd(adId,onSuccess,onError) {
  ReactNativeAdmobile.splashAd(adId,onSuccess,onError);
}

export function rewardVodAd(adId,onSuccess,onError) {
  ReactNativeAdmobile.rewardVodAd(adId,onSuccess,onError);
}

export function setPersonalizedAdEnabled(personalizedAdEnabled=false) {
  ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
}
