package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by marko on 2/18/16.
 */
public class BaseActivity extends Activity {

    // set to true to enable logs, test data (paired devices) and toasts
    public static boolean DEBUG = true;
        /* DEBUG LOG & TOAST */

    public void logD(String TAG, String message) {
        if (DEBUG) Log.d(TAG, message);
    }

    public void showDebugToast(String text) {
        if (DEBUG) Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
