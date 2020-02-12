package com.campionpumps.webservices.qrcode.ble;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.campionpumps.webservices.qrcode.ble.BluetoothLE;

public class ListItemClicked implements AdapterView.OnItemClickListener
{
    private BluetoothLE bluetoothObject = new BluetoothLE();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        bluetoothObject.bdDevice = bluetoothObject.arrayListBluetoothDevices.get(position);

        Log.i("Log", "The device : " + bluetoothObject.bdDevice.toString());

        bluetoothObject.connectToDevice(bluetoothObject.bdDevice);
    }
}