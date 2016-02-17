package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Base Activity class with methods used to handle connections to a Bluetooth device.
 * Created by marko on 2/17/16.
 */
public abstract class BaseBLEActivity extends Activity {

    private static final String TAG = BaseBLEActivity.class.getSimpleName();
    protected BluetoothLeService mBluetoothLeService;

    protected String mDeviceName;
    protected String mDeviceAddress;
    protected int mServiceId;

    @Override
    protected void onStart() {
        super.onStart();
        // bind bluetooth le service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "onStart: binding service");
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Log.d(TAG, "onResume: registering receiver");
        if (mBluetoothLeService != null && mBluetoothLeService.checkBTState(this)) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        Log.d(TAG, "onPause: unregistering receiver");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.close();
        Log.d(TAG, "onDestroy: unbinding service");
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG, "onReceive: " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Toast.makeText(BaseBLEActivity.this, "Connected to " + mDeviceName, Toast.LENGTH_LONG).show();
                Log.i(TAG, "onReceive: Connected to " + mDeviceName);
                gattConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Toast.makeText(BaseBLEActivity.this, "Disconnected from " + mDeviceName, Toast.LENGTH_LONG).show();
                Log.i(TAG, "onReceive: Disconnected from " + mDeviceName);
                gattDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> supportedGattServices = mBluetoothLeService.getSupportedGattServices();
                gattServicesDiscovered(supportedGattServices);
                Log.i(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED services count " + supportedGattServices.size());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Toast.makeText(BaseBLEActivity.this, "Data available from " + mDeviceName, Toast.LENGTH_LONG).show();
                Log.i(TAG, "onReceive: Data available from " + mDeviceName);
                gattDataAvailable(intent);
            }
        }
    };

    protected void showHome() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected abstract void gattDataAvailable(Intent intent);

    protected abstract void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices);

    protected abstract void gattDisconnected();

    protected abstract void gattConnected();

    protected void reconnectToDevice() {
        mBluetoothLeService.disconnect();
        if (mBluetoothLeService.checkBTState(this)) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    public void setmDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public void setmServiceId(int mServiceId) {
        this.mServiceId = mServiceId;
    }


    // Code to manage Service lifecycle.
    protected final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

}
