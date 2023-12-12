package com.samsung.vortex.webrtc.models;

import static com.samsung.vortex.utils.Utils.TAG;

import android.util.Log;
import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    @JavascriptInterface
    public void onPeerConnected(){
        Log.i(TAG, "onPeerConnected: called");
    }
}
