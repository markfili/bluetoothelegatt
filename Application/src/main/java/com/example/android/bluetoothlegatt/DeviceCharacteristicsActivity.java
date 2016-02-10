package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

public class DeviceCharacteristicsActivity extends Activity {

    private static final String TAG = DeviceCharacteristicsActivity.class.getSimpleName();

    private BluetoothLeService mBluetoothLeService;

    private String mDeviceName;
    private String mDeviceAddress;
    private int mServiceId;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

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
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
//                mConnected = true;
//                updateConnectionState(R.string.connected);
//                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
//                mConnected = false;
//                updateConnectionState(R.string.disconnected);
//                invalidateOptionsMenu();
//                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> supportedGattServices = mBluetoothLeService.getSupportedGattServices();
                // TODO get service position from DeviceControlActivity via intent
                // display service characteristics and fill service info header
                displayGattCharacteristics(supportedGattServices.get(mServiceId));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayGattCharacteristics(BluetoothGattService service) {
        // TODO fill info header with service info and listview with characteristics
//        EXTRACTING CHARACTERISTICS
//        PART 1
//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
//                = new ArrayList<>();
//        mGattCharacteristics = new ArrayList<>();
//
//        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
//
//        ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
//                new ArrayList<>();
//
//        // get characteristics from service
//        List<BluetoothGattCharacteristic> gattCharacteristics =
//                gattService.getCharacteristics();
//
//        ArrayList<BluetoothGattCharacteristic> charas =
//                new ArrayList<>();
//
//        // Loops through available Characteristics.
//        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//            charas.add(gattCharacteristic);
//            HashMap<String, String> currentCharaData = new HashMap<>();
//            uuid = gattCharacteristic.getUuid().toString();
//            currentCharaData.put(
//                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
//            currentCharaData.put(LIST_UUID, uuid);
//            gattCharacteristicGroupData.add(currentCharaData);
//        }
//        mGattCharacteristics.add(charas);
//        gattCharacteristicData.add(gattCharacteristicGroupData);
//
//        PART 2
//        if (mGattCharacteristics = null) {
//            final BluetoothGattCharacteristic charaeristic =
//                    mGattCharacteristics.get(groupPosition).get(childposition);
//            final int charaProp = characteristic.getPrerties();
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_RD) > 0) {
//                // If there is an active notification on a characteriic, clear
//                // it first so it doesn't update the data field on the usernterface.
//                if (mNotifyCharacteristic = null) {
//                    mBluetoothLeService.setCharacteristicNofication(
//                            mNotifyCharacterist, false);
//                    mNotifyCharacterisc = null;
//                }
//                mBluetoothLeService.readCharacteristic(charaeristic);
//            }
//            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTY) > 0) {
//                mNotifyCharacteristic = charteristic;
//                mBluetoothLeService.setCharacteristicNofication(
//                        characterisc, true);
//            }
//        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS);
        mServiceId = intent.getIntExtra(DeviceControlActivity.EXTRAS_SERVICE_POSITION, 0);

        Log.i(TAG, String.format("onCreate: Device name: %s, address: %s, service position: %d", mDeviceName, mDeviceAddress, mServiceId));

        // bind bluetooth le service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
