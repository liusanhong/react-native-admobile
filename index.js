// main index.js

import { NativeModules } from 'react-native';

const { ReactNativeAdmobile } = NativeModules;

export default ReactNativeAdmobile;

export function initAd(appid) {
  return ReactNativeAdmobile.initAd(appid);
}
/**
 * 开屏广告
 * @param adId
 * @param onSuccess
 * @param onError
 */
export function splashAd(adId,onSuccess,onError) {
  ReactNativeAdmobile.splashAd(adId,onSuccess,onError);
}
/**
 * 激励广告
 * @param adId
 * @param onSuccess
 * @param onError
 */
export function rewardVodAd(adId,onSuccess,onError) {
  ReactNativeAdmobile.rewardVodAd(adId,onSuccess,onError);
}

/**
 * 插屏广告
 * @param adId
 * @param onSuccess
 * @param onError
 */
export function intertitialAd(adId,onSuccess,onError) {
  ReactNativeAdmobile.intertitialAd(adId,onSuccess,onError);
}

export function setPersonalizedAdEnabled(personalizedAdEnabled=false) {
  ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
}
