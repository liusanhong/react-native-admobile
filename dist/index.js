"use strict";
// main index.js
exports.__esModule = true;
exports.ReactNativeAdmobileUtils = void 0;
var react_native_1 = require("react-native");
var ReactNativeAdmobile = react_native_1.NativeModules.ReactNativeAdmobile;
var ReactNativeAdmobileUtils;
(function (ReactNativeAdmobileUtils) {
    function initAd(appid) {
        return ReactNativeAdmobile.initAd(appid);
    }
    ReactNativeAdmobileUtils.initAd = initAd;
    /**
     * 开屏广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    function splashAd(adId, onSuccess, onError) {
        ReactNativeAdmobile.splashAd(adId, onSuccess, onError);
    }
    ReactNativeAdmobileUtils.splashAd = splashAd;
    /**
     * 激励广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    function rewardVodAd(adId, onSuccess, onError) {
        ReactNativeAdmobile.rewardVodAd(adId, onSuccess, onError);
    }
    ReactNativeAdmobileUtils.rewardVodAd = rewardVodAd;
    /**
     * 插屏广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    function intertitialAd(adId, onSuccess, onError) {
        ReactNativeAdmobile.intertitialAd(adId, onSuccess, onError);
    }
    ReactNativeAdmobileUtils.intertitialAd = intertitialAd;
    function setPersonalizedAdEnabled(personalizedAdEnabled) {
        if (personalizedAdEnabled === void 0) { personalizedAdEnabled = false; }
        ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
    }
    ReactNativeAdmobileUtils.setPersonalizedAdEnabled = setPersonalizedAdEnabled;
})(ReactNativeAdmobileUtils = exports.ReactNativeAdmobileUtils || (exports.ReactNativeAdmobileUtils = {}));
exports["default"] = ReactNativeAdmobileUtils;
