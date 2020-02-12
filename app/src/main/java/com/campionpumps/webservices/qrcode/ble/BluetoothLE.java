package com.campionpumps.webservices.qrcode.ble;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.campionpumps.webservices.qrcode.R;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BluetoothLE extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mBluetoothLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private Button scan;

    BluetoothDevice bdDevice;
    ListView bluetoothDeviceDetected;
    ListItemClicked bluetoothDeviceClicked;
    ArrayAdapter<String> listViewAdapter;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;

    private final String TAG = "BluetoothLE";

    private static String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static String CHARACTERISTIC_UUID = "0000ffe2-0000-1000-8000-00805f9b34fb";
    private static String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static final UUID UUID_SERVICE = UUID.fromString(SERVICE_UUID);
    private static final UUID UUID_CHARACTERISTIC = UUID.fromString(CHARACTERISTIC_UUID);
    private static final UUID UUID_DESCRIPTOR = UUID.fromString(DESCRIPTOR_UUID);

    private static String UPDATE_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private static String UPDATE_CHARACTERISTIC_UUID = "0000fff5-0000-1000-8000-00805f9b34fb";
    private static String UPDATE_DESCRIPTOR_UUID = "00002901-0000-1000-8000-00805f9b34fb";
    private static final UUID UUID_SERVICE_UPDATE = UUID.fromString(UPDATE_SERVICE_UUID);
    private static final UUID UUID_CHARACTERISTIC_UPDATE = UUID.fromString(UPDATE_CHARACTERISTIC_UUID);
    private static final UUID UUID_DESCRIPTOR_UPDATE = UUID.fromString(UPDATE_DESCRIPTOR_UUID);


    private BluetoothGattService bluetoothGattService;
    List <BluetoothGattCharacteristic> bluetoothGattCharacteristic = new ArrayList<BluetoothGattCharacteristic>();
    Queue<BluetoothGattCharacteristic> mWriteCharacteristic = new LinkedList<BluetoothGattCharacteristic>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                    Toast.makeText(this, "The permission to get BLE location data is required", Toast.LENGTH_SHORT).show();
            }else{
                if (VERSION.SDK_INT >= VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        }else{
            Toast.makeText(this, "Location permissions already granted", Toast.LENGTH_SHORT).show();
        }

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        bluetoothDeviceDetected = findViewById(R.id.listViewDetected);
        listViewAdapter = new ArrayAdapter<>(BluetoothLE.this, android.R.layout.simple_list_item_single_choice);
        bluetoothDeviceDetected.setAdapter(listViewAdapter);
        bluetoothDeviceClicked = new ListItemClicked();

        scan = (Button) findViewById(R.id.buttonSearch);
        scan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mBluetoothLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            //   scanLeDevice(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLEScanner.stopScan(mScanCallback);

                }
            }, SCAN_PERIOD);

            ScanFilter scanfilter = new ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"))
                    .build();
            filters.add(scanfilter);

            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();

            mBluetoothLEScanner.startScan(filters, settings, mScanCallback);
        } else {
            mBluetoothLEScanner.stopScan(mScanCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            Log.i("result - RSSI", String.valueOf(result.getRssi()));

            ScanRecord record = result.getScanRecord();
            if (record != null) {
                byte[] data = record.getServiceData(ParcelUuid.fromString("0000ffe0-0000-1000-8000-00805f9b34fb"));
            }

            BluetoothDevice btDevice = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                btDevice = result.getDevice();
            }
            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
                            connectToDevice(device);
                        }
                    });
                }
            };

    public void connectToDevice(final BluetoothDevice device) {
        if (mGatt == null) {
            if (VERSION.SDK_INT >= VERSION_CODES.M) {
                mGatt = device.connectGatt(BluetoothLE.this, true, gattCallback, 2); //autoConnect: false
            } else {
                mGatt = device.connectGatt(BluetoothLE.this, true, gattCallback); //autoConnect: false
            }
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, final int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
//                        if(status == BluetoothGatt.GATT_SUCCESS)
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            //displayGattServices(services);
            Log.i("onServicesDiscovered", services.toString());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService requiredService =
                        gatt.getService(UUID_SERVICE_UPDATE);
                if (requiredService != null) {
                    BluetoothGattCharacteristic requiredCharacteristic =
                            requiredService.getCharacteristic(UUID_CHARACTERISTIC_UPDATE);


                    boolean manuallySettingNotification = setCharacteristicNotification(gatt, true);

                    /*boolean logGatt = gatt.setCharacteristicNotification(requiredCharacteristic, true);
                    Log.d(TAG, "onServicesDiscovered: setCharacteristicNotification - " + logGatt);*/

                    BluetoothGattDescriptor descriptor =
                            requiredCharacteristic.getDescriptor(UUID_DESCRIPTOR_UPDATE);

                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);

                   /* String R = "R";
                    byte[] byteToSend = "R".getBytes(StandardCharsets.UTF_8);
                    requiredCharacteristic.setValue(byteToSend);
                    boolean canWriteCharacteristic = gatt.writeCharacteristic(requiredCharacteristic);*/
                    //boolean isCharacteristicRead = gatt.readCharacteristic(requiredCharacteristic);

                }
            }
        }

        private boolean setCharacteristicNotification(BluetoothGatt bluetoothGatt, boolean enable) {
            //   Logger.d("setCharacteristicNotification");
            BluetoothGattService notifyService = bluetoothGatt.getService(UUID_SERVICE);
            BluetoothGattDescriptor descriptor = null;
            if (notifyService != null) {
                BluetoothGattCharacteristic notifyCharacteristic = notifyService.getCharacteristic(UUID_CHARACTERISTIC);
                bluetoothGatt.setCharacteristicNotification(notifyCharacteristic, enable);
                descriptor = notifyCharacteristic.getDescriptor(UUID_DESCRIPTOR);
                descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : new byte[]{0x00, 0x00});
            }
            return bluetoothGatt.writeDescriptor(descriptor); //descriptor write operation successfully started?
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//                super.onCharacteristicWrite(gatt, characteristic, status);

            byte[] value = characteristic.getValue();
            Log.d(TAG, "onCharacteristicWrite: onCharacteristicWrite ->" + value);
            boolean canReadCharacteristic = gatt.readCharacteristic(characteristic);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {

            String message = characteristic.getStringValue(Base64.NO_PADDING); // Output Bytes

            byte[] byteArray = characteristic.getValue();  // Output as Byte Arr
            String x = new String(byteArray);

            Log.i("onCharacteristicRead", characteristic.toString() + "\n" + message + "\n"
                    + Arrays.toString(byteArray));
            Log.d(TAG, "onCharacteristicRead: byteArray--String--> " + x);

            //gatt.disconnect();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            //super.onDescriptorWrite(gatt, descriptor, status);
            byte[] descValue = descriptor.getValue();
            Log.d(TAG, "onDescriptorWrite: " + Arrays.toString(descValue));
            BluetoothGattService requiredService =
                    gatt.getService(UUID_SERVICE_UPDATE);
            if (requiredService != null) {
                BluetoothGattCharacteristic requiredCharacteristic =
                        requiredService.getCharacteristic(UUID_CHARACTERISTIC_UPDATE);

                byte[] byteToSend = "R".getBytes(StandardCharsets.UTF_8);
                requiredCharacteristic.setValue(byteToSend);
                boolean canWriteCharacteristic = gatt.writeCharacteristic(requiredCharacteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
            byte[] x = characteristic.getValue();
            //bluetoothGattService.notify();
            if (x.length >= 20) {

                BluetoothGattService requiredService =
                        gatt.getService(UUID_SERVICE_UPDATE);
                if (requiredService != null) {
                    BluetoothGattCharacteristic requiredCharacteristic =
                            requiredService.getCharacteristic(UUID_CHARACTERISTIC_UPDATE);

                    byte[] byteToSend = "r1e".getBytes(StandardCharsets.UTF_8);
                    requiredCharacteristic.setValue(byteToSend);
                    boolean gotResponse = gatt.writeCharacteristic(requiredCharacteristic);
                    Log.d(TAG, "onCharacteristicChanged: r1e in Bytes" + Arrays.toString(byteToSend));
                    Log.d(TAG, "onCharacteristicRead: writeCharacteristic ->" + gotResponse);

                    char fifth = (char) x[5];
                    char sixth = (char) x[6];
                    char seventh = (char) x[7];
                    char eighth = (char) x[8];
                    Log.d(TAG, "onCharacteristicChanged: Result Value --> " + fifth + sixth + seventh + eighth);
                }
                Log.d(TAG, "onCharacteristicChanged: " + Arrays.toString(x));
            }

        }
    };
}
