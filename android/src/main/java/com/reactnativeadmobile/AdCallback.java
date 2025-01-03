package com.reactnativeadmobile;

import com.facebook.react.bridge.Callback;

/**
 * describe:
 */
public interface AdCallback {
    void rewordSuccessCallback();
    void rewordErrorCallback(String backStr);
    void splashSuccessCallback();
    void splashErrorCallback();
}
