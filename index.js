// main index.js
import { NativeModules } from 'react-native';
const { ReactNativeAdmobile } = NativeModules;
export var ReactNativeAdmobileUtils;
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

    function loadRewardAd(adId) {
        ReactNativeAdmobile.loadRewardAd(adId);
    }
    ReactNativeAdmobileUtils.loadRewardAd = loadRewardAd;

    function showRewardAd() {
        ReactNativeAdmobile.showRewardAd();
    }
    ReactNativeAdmobileUtils.showRewardAd = showRewardAd;
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
    function setPersonalizedAdEnabled(personalizedAdEnabled = false) {
        ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
    }
    ReactNativeAdmobileUtils.setPersonalizedAdEnabled = setPersonalizedAdEnabled;
})(ReactNativeAdmobileUtils || (ReactNativeAdmobileUtils = {}));
export default ReactNativeAdmobileUtils;
