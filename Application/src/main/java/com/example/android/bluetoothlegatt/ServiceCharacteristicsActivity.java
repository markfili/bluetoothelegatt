package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceCharacteristicsActivity extends BaseDeviceActivity {

    private static final String TAG = ServiceCharacteristicsActivity.class.getSimpleName();

    private ListView mGattCharacteristicsList;
    private TextView mServiceType;
    private TextView mServiceUUID;
    private TextView mServiceInstID;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceServicesActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceServicesActivity.EXTRAS_DEVICE_ADDRESS);
        mServiceId = intent.getIntExtra(DeviceServicesActivity.EXTRAS_SERVICE_POSITION, 0);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);
        setmServiceId(mServiceId);

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

    protected void displayGattCharacteristics(BluetoothGattService gattService) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {
        
    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        displayGattCharacteristics(supportedGattServices.get(mServiceId));
    }

    @Override
    protected void gattDisconnected() {
    }

    @Override
    protected void gattConnected() {

    }
}
