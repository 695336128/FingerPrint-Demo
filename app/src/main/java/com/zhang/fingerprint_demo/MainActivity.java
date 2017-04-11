package com.zhang.fingerprint_demo;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


/**
 * 撸一个指纹识别的demo
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MSG_AUTH_SUCCESS = 100;
    public static final int MSG_AUTH_FAILED = 101;
    public static final int MSG_AUTH_ERROR = 102;
    public static final int MSG_AUTH_HELP = 103;

    private static final String TAG = "MainActivity";

    private Context mContext;

    /**
     * 指纹识别
     */
    private FingerprintManagerCompat mFingerprintManager = null;

    private MyAuthCallback myAuthCallback = null;

    /**
     * 取消指纹扫描CryptoObjectHelper
     */
    private CancellationSignal cancellationSignal = null;

    private TextView mResultInfo;

    private Button mCancelBtn;

    private Button mStartBtn;

    private LinearLayout buttonPanel;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "msg: " + msg.what + " ,arg1: " + msg.arg1);
            switch (msg.what) {
                case MSG_AUTH_SUCCESS:
                    setResultInfo(R.string.fingerprint_success);
                    mCancelBtn.setEnabled(false);
                    mStartBtn.setEnabled(true);
                    cancellationSignal = null;
                    break;
                case MSG_AUTH_FAILED:
                    setResultInfo(R.string.fingerprint_not_recognized);
                    mCancelBtn.setEnabled(false);
                    mStartBtn.setEnabled(true);
                    cancellationSignal = null;
                    break;
                case MSG_AUTH_ERROR:
                    handleErrorCode(msg.arg1);
                    break;
                case MSG_AUTH_HELP:
                    handleHelpCode(msg.arg1);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        initView();
        initFingerManager();
    }

    private void initView() {
        mResultInfo = (TextView) findViewById(R.id.fingerprint_status);
        mCancelBtn = (Button) findViewById(R.id.cancel_button);
        mStartBtn = (Button) findViewById(R.id.start_button);
        buttonPanel = (LinearLayout) findViewById(R.id.buttonPanel);

        mCancelBtn.setOnClickListener(this);
        mStartBtn.setOnClickListener(this);
    }

    private void initFingerManager() {
        // 用v4包中的方法
        mFingerprintManager = FingerprintManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查设备上有没有指纹识别的硬件
            if (!mFingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(mContext, "该6.0设备不支持指纹识别", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if (!MyApplication.hasFingerPrintApi){
                Toast.makeText(mContext, "该5.0设备不支持指纹识别", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 检查设备是否处于安全保护中
        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (!mKeyguardManager.isKeyguardSecure()) {
            Toast.makeText(mContext, "设备没有处于安全保护中", Toast.LENGTH_SHORT).show();
            return;
        }

        // 设备是否有注册的指纹
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(mContext, "设备中没有已注册的指纹", Toast.LENGTH_SHORT).show();
            // 目前位置google仍然没有开放让普通app启动指纹注册界面的权限，因此无法跳到指纹注册界面
            return;
        }

        myAuthCallback = new MyAuthCallback(handler);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                // set button state
                mCancelBtn.setEnabled(false);
                mStartBtn.setEnabled(true);

                // cancel fingerprint auth here.
                cancellationSignal.cancel();
                cancellationSignal = null;
                break;
            case R.id.start_button:
                // reset result info.
                mResultInfo.setText("Touch sensor");
                mResultInfo.setTextColor(ContextCompat.getColor(mContext, R.color.hint_color));
                checkFinger();
                mStartBtn.setEnabled(false);
                mCancelBtn.setEnabled(true);

                break;
        }
    }

    /**
     * 指纹识别
     */
    private void checkFinger() {
        try {
            CryptoObjectHelper cryptoObjectHelper = new CryptoObjectHelper();
            if (cancellationSignal == null) {
                cancellationSignal = new CancellationSignal();
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                mFingerprintManager.authenticate(cryptoObjectHelper.buildCryptoObject(), 0, cancellationSignal, myAuthCallback, null);
            } else {
                mFingerprintManager.authenticate(null, 0, cancellationSignal, myAuthCallback, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Fingerprint init failed! Try again!", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * help，可以恢复的异常
     * @param code
     */
    private void handleHelpCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ACQUIRED_GOOD:
                setResultInfo(R.string.AcquiredGood_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
                setResultInfo(R.string.AcquiredImageDirty_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_INSUFFICIENT:
                setResultInfo(R.string.AcquiredInsufficient_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_PARTIAL:
                setResultInfo(R.string.AcquiredPartial_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_FAST:
                setResultInfo(R.string.AcquiredTooFast_warning);
                break;
            case FingerprintManager.FINGERPRINT_ACQUIRED_TOO_SLOW:
                setResultInfo(R.string.AcquiredToSlow_warning);
                break;
        }
    }

    /**
     * 返回值为error，不可恢复的错误
     * @param code
     */
    private void handleErrorCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                setResultInfo(R.string.ErrorCanceled_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                setResultInfo(R.string.ErrorHwUnavailable_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                setResultInfo(R.string.ErrorLockout_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                setResultInfo(R.string.ErrorNoSpace_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                setResultInfo(R.string.ErrorTimeout_warning);
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                setResultInfo(R.string.ErrorUnableToProcess_warning);
                break;
        }
    }

    private void setResultInfo(int stringId) {
        if (mResultInfo != null) {
            if (stringId == R.string.fingerprint_success) {
                mResultInfo.setTextColor(ContextCompat.getColor(this, R.color.success_color));
            } else {
                mResultInfo.setTextColor(ContextCompat.getColor(this, R.color.warning_color));
            }
            mResultInfo.setText(stringId);
        }
    }
}
