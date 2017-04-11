package com.zhang.fingerprint_demo;

import android.os.Handler;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;

/**
 * Created by zhang . DATA: 2017/4/10 . Description :
 */

public class MyAuthCallback extends FingerprintManagerCompat.AuthenticationCallback {

    private Handler handler = null;

    public MyAuthCallback(Handler handler) {
        super();
        this.handler = handler;
    }

    /**
     * 这个接口会再系统指纹认证出现不可恢复的错误的时候才会调用，并且参数errorCode就给出了错误码，标识了错误的原因。
     * 这个时候app能做的只能是提示用户重新尝试一遍。
     */
    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Log.d("fate", "onAuthenticationError: " + errString);
        super.onAuthenticationError(errMsgId, errString);
        if (handler != null){
            handler.obtainMessage(MainActivity.MSG_AUTH_ERROR, errMsgId, 0).sendToTarget();
        }


    }


    /**
     * 出现了可以恢复的异常才会调用的，如手指移动太快
     */
    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        super.onAuthenticationHelp(helpMsgId, helpString);
        if (handler != null) {
            handler.obtainMessage(MainActivity.MSG_AUTH_HELP, helpMsgId, 0).sendToTarget();
        }
    }

    /**
     * 认证成功之后回调
     */
    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        if (handler != null) {
            handler.obtainMessage(MainActivity.MSG_AUTH_SUCCESS).sendToTarget();
        }
    }

    /**
     * 这个接口会在系统指纹认证失败的情况的下才会回调
     * 认证失败是指所有的信息都采集完整，并且没有任何异常，但是这个指纹和之前注册的指纹是不相符的
     */
    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        if (handler != null) {
            handler.obtainMessage(MainActivity.MSG_AUTH_SUCCESS).sendToTarget();
        }
    }
}
