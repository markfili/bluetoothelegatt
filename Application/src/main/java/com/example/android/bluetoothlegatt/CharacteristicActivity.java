package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacteristicActivity extends BaseBLEActivity {

    @Bind(R.id.device_address)
    TextView mDeviceAddressView;
    @Bind(R.id.service_type)
    TextView mServiceTypeView;
    @Bind(R.id.service_uuid)
    TextView mServiceUuidView;
    @Bind(R.id.service_id)
    TextView mServiceIdView;
    @Bind(R.id.layout_characteristic)
    RelativeLayout mCharacteristicLayout;
    @Bind(R.id.characteristic_uuid)
    TextView characteristicUuidView;

    private int mServiceType;
    private String mServiceUUID;
    private int mServiceInstanceID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic_details);
        ButterKnife.bind(this);
        getIntentData();
        showUI();
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

    private void showUI() {
        mCharacteristicLayout.setVisibility(View.VISIBLE);
        setTitle(mDeviceName);
        showHome();
    }

    private void getIntentData() {
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS);
        mServiceId = intent.getIntExtra(DeviceActivity.EXTRAS_SERVICE_POSITION, 0);

        mServiceType = intent.getIntExtra(ServiceActivity.EXTRAS_SERVICE_TYPE, 0);
        mServiceUUID = intent.getStringExtra(ServiceActivity.EXTRAS_SERVICE_UUID);
        mServiceInstanceID = intent.getIntExtra(ServiceActivity.EXTRAS_SERVICE_ID, 0);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);
        setmServiceId(mServiceId);

        mDeviceAddressView.setText(mDeviceAddress);
        mServiceIdView.setText(String.format("%d", mServiceId));
        mServiceTypeView.setText(mServiceType == BluetoothGattService.SERVICE_TYPE_PRIMARY ? getString(R.string.primary) : getString(R.string.secondary));
        mServiceUuidView.setText(String.format("%s", mServiceUUID));

        setTitle(mDeviceName);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {

    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {

    }

    @Override
    protected void gattDisconnected() {

    }

    @Override
    protected void gattConnected() {

    }
}
