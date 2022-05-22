// main index.js

import { NativeModules } from 'react-native';

const { ReactNativeAdmobile } = NativeModules;

export default ReactNativeAdmobile;

export function initAd(appid) {
  return ReactNativeAdmobile.initAd(appid);
}

export function splashAd() {
  ReactNativeAdmobile.splashAd();
}

export function rewardVodAd() {
  ReactNativeAdmobile.rewardVodAd();
}
