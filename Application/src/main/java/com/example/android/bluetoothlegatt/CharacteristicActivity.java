package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Bind({R.id.prop_read, R.id.prop_write, R.id.prop_notify})
    List<TextView> propertyViews;
    @Bind({R.id.ble_action_read, R.id.ble_action_write, R.id.ble_action_notify})
    List<Button> propertyButtonViews;

    @Bind(R.id.edit_text_readable_data)
    EditText mReadableDataEditText;
    @Bind(R.id.edit_text_writable_data)
    EditText mWritableDataEditText;

    @Bind(R.id.button_hex)
    Button hexButton;
    @Bind(R.id.button_ascii)
    Button ascButton;

    private String mCharacteristicUUID;
    private BluetoothGattCharacteristic mCharacteristic;
    private String[] characteristicValues;
    private String mServiceUUID;
    private Map<Integer, Integer> propertiesMap;

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

        propertiesMap = new HashMap<>();
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_READ, 0);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_WRITE, 1);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_NOTIFY, 2);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {
        String characteristicString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        if (TextUtils.isEmpty(characteristicString) || characteristicString.endsWith("\n00 00 ")) {
            logD(TAG, "gattDataAvailable characteristic has no readable data.");
            mReadableDataEditText.setHint(R.string.no_readable);
            abledInput(false);
        } else {
            logD(TAG, characteristicString);
            showProperty();
            characteristicValues = characteristicString.split("\n");
            mReadableDataEditText.setText(characteristicValues[0]);
            abledInput(true);
        }
    }

    private void abledInput(boolean enabled) {
        mWritableDataEditText.setEnabled(enabled);
        hexButton.setEnabled(enabled);
        ascButton.setEnabled(enabled);
    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        mCharacteristic = supportedGattServices.get(mServicePosition).getCharacteristic(UUID.fromString(mCharacteristicUUID));
        if (mCharacteristic == null) logD(TAG, "gattServicesDiscovered null characteristic has no readable data.");
        readCharacteristic();
    }

    private void readCharacteristic() {
        if (mCharacteristic != null) {
            logD(TAG, "reading characteristic");
            mBluetoothLeService.readCharacteristic(mCharacteristic);
        } else {
            logD(TAG, "readCharacteristic null characteristic");
        }
    }

    private void showProperty() {
        resetControls();

        if (propertiesMap.containsKey(mCharacteristic.getProperties())) {
            propertyViews.get(propertiesMap.get(mCharacteristic.getProperties())).setEnabled(true);
            propertyButtonViews.get(propertiesMap.get(mCharacteristic.getProperties())).setEnabled(true);
        }
    }

    private void resetControls() {
        resetViews(propertyViews);
        resetViews(propertyButtonViews);
    }

    private void resetViews(List<? extends View> views) {
        for (View view : views) {
            view.setEnabled(false);
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


    @OnClick({R.id.button_ascii, R.id.button_hex})
    protected void onFormatChangeClick(View button) {
        if (characteristicValues.length != 0) {
            mReadableDataEditText.setText(button.getId() == R.id.button_hex ? characteristicValues[1] : characteristicValues[0]);
        }
    }

    @OnClick({R.id.ble_action_read, R.id.ble_action_write, R.id.ble_action_notify})
    protected void saveData(View view) {
        switch (view.getId()) {
            case R.id.ble_action_read:
                readCharacteristic();
                break;
            case R.id.ble_action_write:
                if (mCharacteristic != null) {
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
                } else {
                    logD(TAG, "characteristic null");
                }
                break;

            case R.id.ble_action_notify:
                notifyDevice();
                break;
            default:
                break;
        }
    }

    private void notifyDevice() {
        mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
    }

    @OnClick({R.id.layout_details_device, R.id.layout_details_service, R.id.layout_details_characteristic})
    protected void onBackToDevicesClick(View view) {
        Intent intent = new Intent();
        switch (view.getId()) {
            case R.id.layout_details_device:
                intent.setClass(this, ScanActivity.class);
                break;
            case R.id.layout_details_service:
                intent.setClass(this, DeviceActivity.class);
                break;
            case R.id.layout_details_characteristic:
                onBackPressed();
                return;
            default:
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
