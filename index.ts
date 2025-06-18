// main index.js

import {NativeModules} from 'react-native';

const {ReactNativeAdmobile} = NativeModules;


export namespace ReactNativeAdmobileUtils {

    export function initAd(appid: string) {
        return ReactNativeAdmobile.initAd(appid);
    }

    /**
     * 开屏广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    export function splashAd(adId: string, onSuccess:(res?:any)=>void, onError:(e?:any)=>void) {
        ReactNativeAdmobile.splashAd(adId, onSuccess, onError);
    }

    /**
     * 激励广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    export function rewardVodAd(adId: string, onSuccess:(res?:any)=>void, onError:(e?:any)=>void) {
        ReactNativeAdmobile.rewardVodAd(adId, onSuccess, onError);
    }

    export function loadRewardAd(adId: string) {
        ReactNativeAdmobile.loadRewardAd(adId);
    }
    export function showRewardAd() {
        ReactNativeAdmobile.showRewardAd();
    }

    /**
     * 插屏广告
     * @param adId
     * @param onSuccess
     * @param onError
     */
    export function intertitialAd(adId: string,onSuccess:(res?:any)=>void, onError:(e?:any)=>void) {
        ReactNativeAdmobile.intertitialAd(adId, onSuccess, onError);
    }

    export function setPersonalizedAdEnabled(personalizedAdEnabled = false) {
        ReactNativeAdmobile.setPersonalizedAdEnabled(personalizedAdEnabled);
    }

}

export default ReactNativeAdmobileUtils;


