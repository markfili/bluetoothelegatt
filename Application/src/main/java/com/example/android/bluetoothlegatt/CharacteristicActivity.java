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

    // codes for activating indicator and button according to available device property
    private static final int READ_VIEW_GROUP = 0;
    private static final int WRITE_VIEW_GROUP = 1;
    private static final int NOTIFY_VIEW_GROUP = 2;

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

        // setup properties values for comparison
        propertiesMap = new HashMap<>();
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_READ, READ_VIEW_GROUP);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_WRITE, WRITE_VIEW_GROUP);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE, WRITE_VIEW_GROUP);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE, WRITE_VIEW_GROUP);
        propertiesMap.put(BluetoothGattCharacteristic.PROPERTY_NOTIFY, NOTIFY_VIEW_GROUP);
    }

    /**
     * Initializes reading of the Characteristic.
     */
    private void readCharacteristic() {
        if (mCharacteristic != null) {
            logD(TAG, "reading characteristic");
            mBluetoothLeService.readCharacteristic(mCharacteristic);
        } else {
            logD(TAG, "readCharacteristic null characteristic");
        }
    }

    /**
     * Enables views related to properties that are available on BLE device.
     */
    private void showProperty() {
        resetControls();
        if (propertiesMap.containsKey(mCharacteristic.getProperties())) {
            propertyViews.get(propertiesMap.get(mCharacteristic.getProperties())).setEnabled(true);
            propertyButtonViews.get(propertiesMap.get(mCharacteristic.getProperties())).setEnabled(true);
        }
    }

    /**
     * Disables every property related view - buttons and TextView properties indicators.
     */
    private void resetControls() {
        resetViews(propertyViews);
        resetViews(propertyButtonViews);
    }

    private void resetViews(List<? extends View> views) {
        for (View view : views) {
            view.setEnabled(false);
        }
    }

    /**
     * Converts HEX to String.
     * NumberFormatException will be thrown while parsing
     * to indicate that the text is already in String.
     *
     * @param trimmedHexValues String values made from mWritableDataEditText's HEX current text.
     * @return char[] if conversion to String was success, null if text format is already a String and cannot be parsed.
     */
    private char[] convertTostring(String[] trimmedHexValues) {
        char[] chars = new char[trimmedHexValues.length];
        try {
            for (int i = 0; i < trimmedHexValues.length; i++) {
                // parse HEX values to characters, throw exception due to non-HEX characters
                chars[i] = (char) Integer.parseInt(trimmedHexValues[i], 16);
            }
            return chars;
        } catch (NumberFormatException ignored) {
            // do nothing if the values entered are not HEX,
            // the conversion will occur in the next ASC/HEX conversion
            logD(TAG, "asc button with " + ignored.getMessage());
            return null;
        }
    }


    /**
     * Checks if writableString contains String or HEX formatted text and converts it appropriately.
     *
     * @param writableString String to which to check format.
     * @return byte array constructed either by converting text to bytes, or converting HEX formatted String to real HEX values.
     */
    private byte[] getBytesFromString(String writableString) {
        String writableNoSpaces = writableString.replace(" ", "");

        byte[] valueBytes;
        // if entered value is in HEX
        if (writableNoSpaces.matches("[0-9a-fA-F]+")) {
            // convert String in HEX format to byte array
            valueBytes = toHexValue(writableNoSpaces);
        } else {
            valueBytes = writableString.getBytes();
        }
        return valueBytes;
    }

    /**
     * Converts text with HEX format content to real HEX values.
     *
     * @param text to convert
     * @return byte array with HEX values.
     */
    private byte[] toHexValue(String text) {
        byte[] values = new byte[text.length()];
        for (int i = 0; i < text.toCharArray().length; i++) {
            values[i] = Byte.parseByte(text.substring(i, i + 1), 16);
        }
        return values;
    }

    /**
     * Enables notifications on BLE peripheral device.
     */
    private void notifyDevice() {
        mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
    }

    private void setReadableEditText(int position) {
        if (characteristicValues != null)
            if (characteristicValues.length != 0) {
                mReadableDataEditText.setText(characteristicValues[position]);
            }
    }

    @Override
    protected void gattDataAvailable(Intent intent) {
        String characteristicString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
        if (TextUtils.isEmpty(characteristicString) || characteristicString.endsWith("\n00 00 ")) {
            logD(TAG, "gattDataAvailable characteristic has no readable data.");
            mReadableDataEditText.setHint(R.string.no_readable);
            hexButton.setEnabled(false);
            ascButton.setEnabled(false);
        } else {
            logD(TAG, characteristicString);
            showProperty();
            characteristicValues = characteristicString.split("\n");
            mReadableDataEditText.setText(characteristicValues[0]);
            abledInput(true);
        }
    }

    private void abledInput(boolean enabled) {
        hexButton.setEnabled(enabled);
        ascButton.setEnabled(!enabled);
    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        mCharacteristic = supportedGattServices.get(mServicePosition).getCharacteristic(UUID.fromString(mCharacteristicUUID));
        if (mCharacteristic == null) logD(TAG, "gattServicesDiscovered null characteristic has no readable data.");
        readCharacteristic();
    }

    @Override
    protected void gattDataWritten() {
        // TODO exit or refresh data
    }


    /**
     * Handles clicks on conversion control buttons.
     * Converts from ASC to HEX and vice-versa depending on button clicked.
     *
     * @param button clicked.
     */
    @OnClick({R.id.button_ascii, R.id.button_hex})
    protected void onFormatChangeClick(View button) {
        String writableText = mWritableDataEditText.getText().toString();

        if (button.getId() == R.id.button_hex) {
            setReadableEditText(1);

            // convert writable EditText value from String to HEX
            if (!TextUtils.isEmpty(writableText)) {

                // check if text is already in HEX
                if (!writableText.replace(" ", "").matches("[0-9a-fA-F]+")) {
                    byte[] data = writableText.getBytes();

                    StringBuilder stringBuilder = new StringBuilder(data.length);
                    // append text value's bytes in pairs
                    for (byte byteChar : data) {
                        // format "DE AD F1 89 "
                        stringBuilder.append(String.format("%02X ", byteChar));
                    }
                    mWritableDataEditText.setText("");
                    mWritableDataEditText.append(stringBuilder);
                }
            }
            ascButton.setEnabled(true);
            hexButton.setEnabled(false);

        } else if (button.getId() == R.id.button_ascii) {
            setReadableEditText(0);

            // convert writable EditText value from HEX to String (ASCII)
            if (!TextUtils.isEmpty(writableText)) {
                // trim surrounding whitespace
                writableText = writableText.trim();

                // remove whitespaces and extract entries
                String[] trimmedHexValues = writableText.split(" ");

                // check if text entered is already in HEX
                char[] chars = convertTostring(trimmedHexValues);

                // if it wasn't already HEX, show converted text
                if (chars != null) {
                    mWritableDataEditText.setText("");
                    mWritableDataEditText.append(new String(chars));
                }
            }
            hexButton.setEnabled(true);
            ascButton.setEnabled(false);
        }
    }


    /**
     * Handles click actions on property control elements: buttons Read, Write and Notify.
     *
     * @param button Button clicked.
     */
    @OnClick({R.id.ble_action_read, R.id.ble_action_write, R.id.ble_action_notify})
    protected void saveData(View button) {
        switch (button.getId()) {
            case R.id.ble_action_read:
                readCharacteristic();
                break;
            case R.id.ble_action_write:
                if (mCharacteristic != null) {

//                    if (mCharacteristic.getPermissions() == BluetoothGattCharacteristic.PERMISSION_WRITE) {
                        // get string from writable box
                        String writableString = mWritableDataEditText.getText().toString().trim();

                        // check if writableString is in HEX or text format
                        byte[] valueBytes = getBytesFromString(writableString);
                        // set value to Characteristic
                        mCharacteristic.setValue(valueBytes);
                        // try to save the characteristic
                        boolean saved = mBluetoothLeService.writeCharacteristic(mCharacteristic);

                        Log.d(TAG, "saveData: " + saved);

                        if (saved) {
                            Toast.makeText(this, "Written to device.", Toast.LENGTH_LONG).show();
                            onBackPressed();
                        } else {
                            // TODO handle failure
                            Toast.makeText(this, "Failed writing to device. Check BLE connectivity.", Toast.LENGTH_LONG).show();
                        }
//                    } else {
//                        Toast.makeText(this, "I'm sorry, Dave. I'm afraid I can't do that.", Toast.LENGTH_LONG).show();
//                        Toast.makeText(this, "It's not permitted by the device.", Toast.LENGTH_LONG).show();
//                    }
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


    /**
     * Handles click events on layouts with details.
     *
     * @param layout clicked.
     */
    @OnClick({R.id.layout_details_device, R.id.layout_details_service, R.id.layout_details_characteristic})
    protected void onBackToDevicesClick(View layout) {
        Intent intent = new Intent();
        switch (layout.getId()) {
            case R.id.layout_details_device:
                intent.setClass(this, ScanActivity.class);
                break;
            case R.id.layout_details_service:
                intent.setClass(this, DeviceActivity.class);
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, mDeviceName);
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
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
