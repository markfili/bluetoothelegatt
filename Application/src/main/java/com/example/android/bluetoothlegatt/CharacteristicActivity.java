package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CharacteristicActivity extends BaseBLEActivity {

    private static final String TAG = CharacteristicActivity.class.getSimpleName();

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
    TextView mCharacteristicUuidView;
    @Bind(R.id.edit_text_readable_data)
    EditText editTextReadableData;
    @Bind(R.id.edit_text_writable_data)
    EditText editTextWritableData;
    @Bind(R.id.button_format_change)
    Button buttonFormatChange;

    private int mServiceType;
    private String mServiceUUID;
    private int mServiceInstanceID;
    private String mCharacteristicUUID;

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
                mBluetoothLeService.close();
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
        mServicePosition = intent.getIntExtra(DeviceActivity.EXTRAS_SERVICE_POSITION, 0);

        mServiceType = intent.getIntExtra(ServiceActivity.EXTRAS_SERVICE_TYPE, 0);
        mServiceUUID = intent.getStringExtra(ServiceActivity.EXTRAS_SERVICE_UUID);
        mServiceInstanceID = intent.getIntExtra(ServiceActivity.EXTRAS_SERVICE_ID, 0);
        mCharacteristicUUID = intent.getStringExtra(ServiceActivity.EXTRAS_CHARACTERISTIC_UUID);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);
        setmServicePosition(mServicePosition);

        mDeviceAddressView.setText(mDeviceAddress);
        mServiceIdView.setText(String.format("%d", mServicePosition));
        mServiceTypeView.setText(mServiceType == BluetoothGattService.SERVICE_TYPE_PRIMARY ? getString(R.string.primary) : getString(R.string.secondary));
        mServiceUuidView.setText(String.format("%s", mServiceUUID));
        mCharacteristicUuidView.setText(mCharacteristicUUID);

        setTitle(mDeviceName);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {

    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        displayCharacteristicData(supportedGattServices.get(mServicePosition).getCharacteristic(UUID.fromString(mCharacteristicUUID)));
    }

    private void displayCharacteristicData(BluetoothGattCharacteristic characteristic) {
        // TODO
        if (characteristic != null) {
            int properties = characteristic.getProperties();
            logD(TAG, String.valueOf(properties));
        }
//        String value = new String(characteristic.getValue());
//        editTextReadableData.setText(value);

    }

    @Override
    protected void gattDisconnected() {

    }

    @Override
    protected void gattConnected() {

    }
}
