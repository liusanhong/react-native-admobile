// main index.js

import { NativeModules } from 'react-native';

const { ReactNativeAdmobile } = NativeModules;

export default ReactNativeAdmobile;

export function initAd(appid) {
  ReactNativeAdmobile.initAd(appid);
}

export function splashAd() {
  ReactNativeAdmobile.splashAd();
}
