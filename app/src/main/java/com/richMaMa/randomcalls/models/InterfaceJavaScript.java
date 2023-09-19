package com.richMaMa.randomcalls.models;

import android.webkit.JavascriptInterface;

import com.richMaMa.randomcalls.Activities.CallActivity;

public class InterfaceJavaScript {

    CallActivity callActivity;
    public InterfaceJavaScript(CallActivity callActivity){
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();
    }

}
