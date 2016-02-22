package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CharacteristicActivity extends BaseBLEActivity {

    private static final String TAG = CharacteristicActivity.class.getSimpleName();

    @Bind(R.id.title_device_name)
    TextView mDeviceNameTitle;
    @Bind(R.id.title_service_name)
    TextView mServiceNameTitle;
    @Bind(R.id.title_characteristic_name)
    TextView mCharacteristicNameTitle;
    @Bind(R.id.device_address)
    TextView mDeviceAddressView;
    @Bind(R.id.service_uuid)
    TextView mServiceUuidView;
    @Bind(R.id.layout_details_service)
    RelativeLayout mServiceLayout;
    @Bind(R.id.layout_details_characteristic)
    RelativeLayout mCharacteristicLayout;
    @Bind(R.id.characteristic_uuid)
    TextView mCharacteristicUuidView;

    @Bind(R.id.edit_text_readable_data)
    EditText mReadableDataEditText;
    @Bind(R.id.edit_text_writable_data)
    EditText mWritableDataEditText;

    private String mCharacteristicUUID;
    private BluetoothGattCharacteristic mCharacteristic;
    private String[] characteristicValues;
    private String mServiceUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_characteristic);
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
        mServiceLayout.setVisibility(View.VISIBLE);
        mDeviceNameTitle.setText(mDeviceName);
        showHome();
    }

    private void getIntentData() {
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS);
        mServicePosition = intent.getIntExtra(DeviceActivity.EXTRAS_SERVICE_POSITION, 0);

        mServiceUUID = intent.getStringExtra(ServiceActivity.EXTRAS_SERVICE_UUID);
        mCharacteristicUUID = intent.getStringExtra(ServiceActivity.EXTRAS_CHARACTERISTIC_UUID);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);
        setmServicePosition(mServicePosition);

        mDeviceAddressView.setText(mDeviceAddress);
        mServiceUuidView.setText(String.format("%s", mServiceUUID));
        mCharacteristicNameTitle.setText(GattServicesAttributes.lookup(mCharacteristicUUID, getString(R.string.unknown_characteristic)));
        mServiceNameTitle.setText(GattServicesAttributes.lookup(mServiceUUID, getString(R.string.unknown_service)));
        mCharacteristicUuidView.setText(mCharacteristicUUID);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {
        String characteristicString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        if (TextUtils.isEmpty(characteristicString) || characteristicString.endsWith("\n00 00 ")) {
            Toast.makeText(this, "Characteristic has no readable data.", Toast.LENGTH_LONG).show();
        } else {
            logD(TAG, characteristicString);
            characteristicValues = characteristicString.split("\n");
            mReadableDataEditText.setText(characteristicValues[0]);
        }
    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        mCharacteristic = supportedGattServices.get(mServicePosition).getCharacteristic(UUID.fromString(mCharacteristicUUID));
        if (mCharacteristic != null) {
        // TODO PERMISSIONS
        // mCharacteristicPermission.setText(permissionToString(mCharacteristic.getPermissions()));
            mBluetoothLeService.readCharacteristic(mCharacteristic);
        }
    }

    @Override
    protected void gattDisconnected() {

    }

    @Override
    protected void gattConnected() {

    }

    @Override
    protected void gattDataWritten() {
        // TODO exit or refresh data
    }


    @OnClick(R.id.button_format_change)
    protected void onFormatChangeClick() {
        mReadableDataEditText.setText(TextUtils.equals(mReadableDataEditText.getText().toString(), characteristicValues[0]) ? characteristicValues[1] : characteristicValues[0]);
    }

    @OnClick(R.id.button_save_characteristic)
    protected void saveData() {
        if (mCharacteristic.getPermissions() == BluetoothGattCharacteristic.PERMISSION_WRITE) {
            mCharacteristic.setValue(mWritableDataEditText.getText().toString());
            boolean saved = mBluetoothLeService.writeCharacteristic(mCharacteristic);
            Log.d(TAG, "saveData: " + saved);
            if (saved) {
                Toast.makeText(this, "Written to device.", Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                // TODO handle failure
                Toast.makeText(this, "Failed writing to device. Check BLE connectivity.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "I'm sorry, Dave. I'm afraid I can't do that.", Toast.LENGTH_LONG).show();
            Toast.makeText(this, "It's not permitted by the device.", Toast.LENGTH_LONG).show();
        }
    }

    private String permissionToString(int permission) {
        switch (permission) {
            case BluetoothGattCharacteristic.PERMISSION_READ:
                return "Read";
            case BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED:
                return "Read encrypted";
            case BluetoothGattCharacteristic.PERMISSION_READ_ENCRYPTED_MITM:
                return "Read encrypted MITM";
            case BluetoothGattCharacteristic.PERMISSION_WRITE:
                return "Write";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED:
                return "Write encrypted";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_ENCRYPTED_MITM:
                return "Write encrypted MITM";
            case BluetoothGattCharacteristic.PERMISSION_WRITE_SIGNED:
                return "Write signed";
            default:
                return "Unknown";
        }
    }
}
