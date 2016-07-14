/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothlegatt;

import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceActivity extends BaseBLEActivity {
    private final static String TAG = DeviceActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_SERVICE_POSITION = "SERVICE_POSITION";

    @Bind(R.id.title_device_name)
    TextView mDeviceNameTitle;

    private String mDeviceName;
    private String mDeviceAddress;
    private ListView mGattServicesList;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    AdapterView.OnItemClickListener servicesListClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int mServicePosition, long id) {

            // show list of characteristics in ServiceActivity
            Intent intent = new Intent(getBaseContext(), ServiceActivity.class);

            // send intent with required data
            intent.putExtra(EXTRAS_DEVICE_NAME, mDeviceName);
            intent.putExtra(EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
            intent.putExtra(EXTRAS_SERVICE_POSITION, mServicePosition);

            mBluetoothLeService.close();
            startActivity(intent);
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter(null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        ButterKnife.bind(this);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        setmDeviceName(mDeviceName);
        setmDeviceAddress(mDeviceAddress);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnItemClickListener(servicesListClickListener);

        mDeviceNameTitle.setText(mDeviceName);
        showHome();
    }


    private void displayData(String data) {
        if (data != null) {
            // TODO DISPLAY DATA
//            mDataField.setText(data);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();

            currentServiceData.put(
                    LIST_NAME, GattServicesAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
        }

        SimpleAdapter gattServiceAdapter = new SimpleAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    @Override
    protected void gattDataAvailable(Intent intent) {
        displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
    }

    @Override
    protected void gattServicesDiscovered(List<BluetoothGattService> supportedGattServices) {
        displayGattServices(supportedGattServices);
    }

    @Override
    protected void gattDataWritten() {

    }
}
