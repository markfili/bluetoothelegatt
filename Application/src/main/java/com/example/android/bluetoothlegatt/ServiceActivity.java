package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ServiceActivity extends BaseBLEActivity {

    private static final String TAG = ServiceActivity.class.getSimpleName();
    public static final String EXTRAS_SERVICE_TYPE = "EXTRAS_SERVICE_TYPE";
    public static final String EXTRAS_SERVICE_UUID = "EXTRAS_SERVICE_UUID";
    public static final String EXTRAS_SERVICE_ID = "EXTRAS_SERVICE_ID";
    public static final String EXTRAS_CHARACTERISTIC_UUID = "EXTRAS_CHARACTERISTIC_UUID";

    @Bind(R.id.device_address)
    TextView mAddressView;
    @Bind(R.id.service_type)
    TextView mServiceTypeView;
    @Bind(R.id.service_uuid)
    TextView mServiceUUIDView;
    @Bind(R.id.service_id)
    TextView mServiceInstanceIDView;
    @Bind(R.id.characteristics_listview)
    ListView mGattCharacteristicsListView;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private int mServiceType;
    private String mServiceUUID;
    private int mServiceInstanceID;

    private AdapterView.OnItemClickListener onCharacteristicClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(ServiceActivity.this, CharacteristicActivity.class);
            intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, mDeviceName);
            intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);

            intent.putExtra(ServiceActivity.EXTRAS_CHARACTERISTIC_UUID, mBluetoothGattService.getCharacteristics().get(position).getUuid().toString());
            intent.putExtra(ServiceActivity.EXTRAS_SERVICE_TYPE, mServiceType);
            intent.putExtra(ServiceActivity.EXTRAS_SERVICE_UUID, mServiceUUID);
            intent.putExtra(ServiceActivity.EXTRAS_SERVICE_ID, mServiceInstanceID);

            mBluetoothLeService.close();
            startActivity(intent);
        }
    };
    private BluetoothGattService mBluetoothGattService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_characteristics);
        ButterKnife.bind(this);

        getIntentData();
    }

    private void getIntentData() {
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS);
        mServicePosition = intent.getIntExtra(DeviceActivity.EXTRAS_SERVICE_POSITION, 0);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);
        setmServicePosition(mServicePosition);

        mAddressView.setText(mDeviceAddress);

        setTitle(mDeviceName);
        showHome();

        Log.i(TAG, String.format("onCreate: Device name: %s, address: %s, service position: %d", mDeviceName, mDeviceAddress, mServicePosition));
    }

    protected void displayGattCharacteristics(BluetoothGattService gattService) {
        mServiceUUID = gattService.getUuid().toString();
        mServiceType = gattService.getType();
        mServiceInstanceID = gattService.getInstanceId();
        // fill info header with service info
        mServiceUUIDView.setText(mServiceUUID);
        mServiceTypeView.setText(mServiceType == BluetoothGattService.SERVICE_TYPE_PRIMARY ? getString(R.string.primary) : getString(R.string.secondary));
        mServiceInstanceIDView.setText(String.format("%d", mServiceInstanceID));

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
                    LIST_NAME, GattServicesAttributes.lookup(charUUID, unknownCharaString));
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

        mGattCharacteristicsListView.setAdapter(gattServiceAdapter);
        mGattCharacteristicsListView.setOnItemClickListener(onCharacteristicClickListener);

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

    @Override
    protected void gattDataAvailable(Intent intent) {

    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        mBluetoothGattService = supportedGattServices.get(mServicePosition);
        displayGattCharacteristics(mBluetoothGattService);
    }

    @Override
    protected void gattDisconnected() {
    }

    @Override
    protected void gattConnected() {

    }
}
