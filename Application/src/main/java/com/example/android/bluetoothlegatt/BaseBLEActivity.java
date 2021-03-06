package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

/**
 * Base Activity class with methods used to handle connections to a Bluetooth device.
 * Created by marko on 2/17/16.
 */
public abstract class BaseBLEActivity extends BaseActivity {

    private static final String TAG = BaseBLEActivity.class.getSimpleName();

    public static final long DEBUG_SCAN_PERIOD = 10;

    protected BluetoothLeService mBluetoothLeService;
    protected String mDeviceName;
    protected String mDeviceAddress;
    protected int mServicePosition;
    private boolean mConnected = false;

    @Override
    protected void onStart() {
        super.onStart();
        // bind bluetooth le service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        logD(TAG, "onStart: binding service");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        logD(TAG, "onResume: registering receiver");
        if (mBluetoothLeService != null && mBluetoothLeService.checkBTState(this)) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            logD(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        logD(TAG, "onPause: unregistering receiver");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.close();
        logD(TAG, "onDestroy: unbinding service");
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                reconnectToDevice();
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            logD(TAG, "onReceive: " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                showDebugToast("Connected to " + mDeviceName);
                logD(TAG, "onReceive: Connected to " + mDeviceName);
                gattConnected();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                showDebugToast("Disconnected from " + mDeviceName);
                logD(TAG, "onReceive: Disconnected from " + mDeviceName);
                gattDisconnected();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> supportedGattServices = mBluetoothLeService.getSupportedGattServices();
                gattServicesDiscovered(supportedGattServices);
                logD(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED services count " + supportedGattServices.size());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                showDebugToast("Data available from " + mDeviceName);
                logD(TAG, "onReceive: Data available from " + mDeviceName);
                gattDataAvailable(intent);
            } else if (BluetoothLeService.ACTION_DATA_WRITTEN.equals(action)) {
                showDebugToast("Data written to " + mDeviceName);
                logD(TAG, "onReceive ACTION DATA WRITTEN: Data written to " + mDeviceName);
                gattDataWritten();
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

    protected void gattDisconnected() {
        mConnected = false;
        updateConnectionState(R.string.disconnected);
        invalidateOptionsMenu();
    }

    protected void gattConnected() {
        mConnected = true;
        updateConnectionState(R.string.connected);
        invalidateOptionsMenu();
    }

    protected abstract void gattDataWritten();

    protected void reconnectToDevice() {
        mBluetoothLeService.disconnect();
        if (mBluetoothLeService.checkBTState(this)) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // TODO update connection state
//                mConnectionState.setText(resourceId);
            }
        });
    }

    public void setmDeviceName(String mDeviceName) {
        this.mDeviceName = mDeviceName;
    }

    public void setmDeviceAddress(String mDeviceAddress) {
        this.mDeviceAddress = mDeviceAddress;
    }

    public void setmServicePosition(int mServicePosition) {
        this.mServicePosition = mServicePosition;
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
