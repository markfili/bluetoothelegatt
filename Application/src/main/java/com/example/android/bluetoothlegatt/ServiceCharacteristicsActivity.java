package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceCharacteristicsActivity extends Activity {

    private static final String TAG = ServiceCharacteristicsActivity.class.getSimpleName();

    private BluetoothLeService mBluetoothLeService;

    private ListView mGattCharacteristicsList;
    private TextView mServiceType;
    private TextView mServiceUUID;
    private TextView mServiceInstID;


    private String mDeviceName;
    private String mDeviceAddress;
    private int mServiceId;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

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
            Log.i(TAG, "onReceive: " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                // TODO
                Toast.makeText(ServiceCharacteristicsActivity.this, "Connected to " + mDeviceName, Toast.LENGTH_LONG).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                // TODO
                Toast.makeText(ServiceCharacteristicsActivity.this, "Disconnected from " + mDeviceName, Toast.LENGTH_LONG).show();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> supportedGattServices = mBluetoothLeService.getSupportedGattServices();

                Log.i(TAG, "onReceive: ACTION_GATT_SERVICES_DISCOVERED services count " + supportedGattServices.size());

                // display service characteristics and fill service info header
                displayGattCharacteristics(supportedGattServices.get(mServiceId));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // TODO
                Toast.makeText(ServiceCharacteristicsActivity.this, "Data available from " + mDeviceName, Toast.LENGTH_LONG).show();
            }
        }
    };

    private void displayGattCharacteristics(BluetoothGattService gattService) {

        // fill info header with service info
        mServiceUUID.setText(gattService.getUuid().toString());
        mServiceType.setText("" + gattService.getType());
        mServiceInstID.setText("" + gattService.getInstanceId());

        // dummy text for item title
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);

        // fill listview with characteristics
        ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                new ArrayList<>();

        String charUUID;

        // loops through available Characteristics.
        List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
            HashMap<String, String> currentCharaData = new HashMap<>();
            charUUID = gattCharacteristic.getUuid().toString();
            currentCharaData.put(
                    LIST_NAME, SampleGattAttributes.lookup(charUUID, unknownCharaString));
            currentCharaData.put(LIST_UUID, charUUID);
            gattCharacteristicGroupData.add(currentCharaData);
        }

        // setup characteristic adapter with characteristic data
        SimpleAdapter gattServiceAdapter = new SimpleAdapter(
                this,
                gattCharacteristicGroupData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

        mGattCharacteristicsList.setAdapter(gattServiceAdapter);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceServicesActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceServicesActivity.EXTRAS_DEVICE_ADDRESS);
        mServiceId = intent.getIntExtra(DeviceServicesActivity.EXTRAS_SERVICE_POSITION, 0);

        Log.i(TAG, String.format("onCreate: Device name: %s, address: %s, service position: %d", mDeviceName, mDeviceAddress, mServiceId));

        TextView mAddressView = (TextView) findViewById(R.id.device_address);
        mServiceUUID = (TextView) findViewById(R.id.service_uuid);
        mServiceType = (TextView) findViewById(R.id.service_type);
        mServiceInstID = (TextView) findViewById(R.id.service_id);

        mAddressView.setText(mDeviceAddress);

        mGattCharacteristicsList = (ListView) findViewById(R.id.characteristics_recyclerview);

        setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // bind bluetooth le service
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null && mBluetoothLeService.checkBTState(this)) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothLeService.close();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mBluetoothLeService.close();
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
