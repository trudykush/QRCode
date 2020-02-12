package com.campionpumps.webservices.qrcode.ble;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.campionpumps.webservices.qrcode.random.ConnectActivityLogic;
import com.campionpumps.webservices.qrcode.R;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Kushal.Mishra on 29/03/2018.
 */

public class Bluetooth extends AppCompatActivity {

    ListView listViewPaired;
    ListView listViewDetected;
    ArrayList<String> arrayListpaired;
    Button buttonSearch,buttonOn,buttonDesc,buttonOff;
    ArrayAdapter<String> adapter,detectedAdapter;
    static HandleSearch handleSearch;
    BluetoothDevice bdDevice;
    BluetoothClass bdClass;
    ArrayList<BluetoothDevice> arrayListPairedBluetoothDevices;
    private ButtonClicked clicked;
    ListItemClickedOnPaired ListItemClickedOnPaired;
    BluetoothAdapter bluetoothAdapter = null;
    ArrayList<BluetoothDevice> arrayListBluetoothDevices = null;
    ListItemClicked listItemClicked;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "Bluetooth Class";
    BluetoothDevice mdevice;

    private boolean mConnected;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;

    private BluetoothServerSocket bluetoothServerSocket;

    private static final String UUID_SERIAL_PORT_PROFILE
            = "0000ffe2-0000-1000-8000-00805f9b34fb"; //0000ffe0-0000-1000-8000-00805f9b34fb
            //0000fff0-0000-1000-8000-00805f9b34fb
//            = String.valueOf(UUID.fromString("0000110e-0000-1000-8000-00805f9b34fb"));

    //5067e768-3cdb-11e8-b467-0ed5f89f718b

    private BluetoothSocket mSocket = null;
    private BufferedReader mBufferedReader = null;

    final int REQUEST_COARSE_LOCATION = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth);
        listViewDetected = findViewById(R.id.listViewDetected);
        listViewPaired = findViewById(R.id.listViewPaired);
        buttonSearch = findViewById(R.id.buttonSearch);
        buttonOn = findViewById(R.id.buttonOn);
        buttonDesc = findViewById(R.id.buttonDesc);
        buttonOff = findViewById(R.id.buttonOff);
        arrayListpaired = new ArrayList<String>();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        clicked = new ButtonClicked();
        handleSearch = new HandleSearch();
        arrayListPairedBluetoothDevices = new ArrayList<BluetoothDevice>();
        /*
         * the above declaration is just for getting the paired bluetooth devices;
         * this helps in the removing the bond between paired devices.
         */
        ListItemClickedOnPaired = new ListItemClickedOnPaired();
        arrayListBluetoothDevices = new ArrayList<BluetoothDevice>();
        adapter= new ArrayAdapter<String>(Bluetooth.this, android.R.layout.simple_list_item_1, arrayListpaired);
        detectedAdapter = new ArrayAdapter<String>(Bluetooth.this, android.R.layout.simple_list_item_single_choice);
        listViewDetected.setAdapter(detectedAdapter);
        listItemClicked = new ListItemClicked();
        detectedAdapter.notifyDataSetChanged();
        listViewPaired.setAdapter(adapter);


    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        getPairedDevices();
        buttonOn.setOnClickListener(clicked);
        buttonSearch.setOnClickListener(clicked);
        buttonDesc.setOnClickListener(clicked);
        buttonOff.setOnClickListener(clicked);
        listViewDetected.setOnItemClickListener(listItemClicked);
        listViewPaired.setOnItemClickListener(ListItemClickedOnPaired);
    }


    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Message msg = Message.obtain();
            String action = intent.getAction();

//            if(BluetoothDevice.ACTION_FOUND.equals(action)){
            if(BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_NAME_CHANGED.equals(action)
                    || BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){
                Toast.makeText(context, "ACTION_FOUND", Toast.LENGTH_SHORT).show();

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
               //bdDevice = device;
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();

                //UUID SERIAL_UUID = device.getUuids()[0].getUuid();

                ParcelUuid[] Uuid = device.getUuids();
                int bluetoothContent = device.describeContents();

                Log.d("UUID", Arrays.toString(Uuid));
                Log.d("bluetoothContent ", String.valueOf(bluetoothContent));
                Log.i("Bluetooth Device info ", "Device found: " + deviceName + "; MAC " + deviceHardwareAddress);


                try {
                  /*bluetoothAdapter.listenUsingRfcommWithServiceRecord(deviceName, SERVICE_UUID);
                    bluetoothServerSocket.accept();
                    bluetoothAdapter.cancelDiscovery();*/

                  openDeviceConnection(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try
                {
                    //device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    //device.getClass().getMethod("cancelPairingUserInput", boolean.class).invoke(device);
                }
                catch (Exception e) {
                    Log.i("Log", "Inside the exception: ");
                    e.printStackTrace();
                }

                if(arrayListBluetoothDevices.size()<1) // this checks if the size of bluetooth device is 0,then add the
                {                                           // device to the arraylist.
                    detectedAdapter.add(device.getName() + " \n " + device.getAddress());
                    arrayListBluetoothDevices.add(device);
                    detectedAdapter.notifyDataSetChanged();
                }
                else
                {
                    boolean flag = true;    // flag to indicate that particular device is already in the arlist or not
                    for(int i = 0; i<arrayListBluetoothDevices.size();i++)
                    {
                        if(device.getAddress().equals(arrayListBluetoothDevices.get(i).getAddress()))
                        {
                            flag = false;
                        }
                    }
                    if(flag == true)
                    {
                        detectedAdapter.add(device.getName()+"\n"+device.getAddress());
                        arrayListBluetoothDevices.add(device);
                        detectedAdapter.notifyDataSetChanged();
                    }


                /* // FOR HIGHER API > 15
//                if (intent.getAction().equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                  //  bdDevice = needed;
                    try {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int pin=intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234);
                        //the pin in case you need to accept for an specific pin
                        Log.d("", "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY",1234));
                        byte[] pinBytes;
                        pinBytes = (""+pin).getBytes("UTF-8");
                        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
                            device.setPin(pinBytes);
                            device.setPairingConfirmation(true);
                        }
                        //setPairing confirmation if neeeded

                    } catch (Exception e) {
                        Log.e("", "Error occurs when trying to auto pair");
                        e.printStackTrace();
                    }*/
                }
            }
        }
    };


    private void getPairedDevices() {
        Set<BluetoothDevice> pairedDevice = bluetoothAdapter.getBondedDevices();
        if(pairedDevice.size()>0)
        {
            for(BluetoothDevice device : pairedDevice)
            {
                arrayListpaired.add(device.getName() + "\n" + device.getAddress());
                arrayListPairedBluetoothDevices.add(device);
            }
        }
        adapter.notifyDataSetChanged();
    }

    class ListItemClicked implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            bdDevice = arrayListBluetoothDevices.get(position);
            //bdClass = arrayListBluetoothDevices.get(position);
            Log.i("Log", "The device : " + bdDevice.toString());
            /*
             * here below we can do pairing without calling the call thread(), we can directly call the
             * connect(). but for the safer side we must use the threading object.
             */
            //callThread();
//            connect(bdDevice);
            connectDevice(bdDevice);
            pairDevice(bdDevice);
            Boolean isBonded = false;
            try {
                isBonded = createBond(bdDevice);
                if(isBonded)
                {
                    //arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                    //adapter.notifyDataSetChanged();
                    getPairedDevices();
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }//connect(bdDevice);
            Log.i("Log", "The bond is created: "+isBonded);
        }
    }
    class ListItemClickedOnPaired implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
            bdDevice = arrayListPairedBluetoothDevices.get(position);
            try {
                Boolean removeBonding = removeBond(bdDevice);
                if(removeBonding)
                {
                    arrayListpaired.remove(position);
                    adapter.notifyDataSetChanged();
                }


                Log.i("Log", "Removed"+removeBonding);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    /*private void callThread() {
        new Thread(){
            public void run() {
                Boolean isBonded = false;
                try {
                    isBonded = createBond(bdDevice);
                    if(isBonded)
                    {
                        arrayListpaired.add(bdDevice.getName()+"\n"+bdDevice.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }//connect(bdDevice);
                Log.i("Log", "The bond is created: "+isBonded);
            }
        }.start();
    }*/

    // Gatt connection
    private void connectDevice(BluetoothDevice device) {
        //   log("Connecting to " + device.getAddress());
        GattClientCallback gattClientCallback = new GattClientCallback();
        mGatt = device.connectGatt(this, false, gattClientCallback);
    }

    private class GattClientCallback extends BluetoothGattCallback {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            // log("onConnectionStateChange newState: " + newState);

            if (status == BluetoothGatt.GATT_FAILURE) {
               // logError("Connection Gatt failure status " + status);
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
             //   logError("Connection not GATT sucess status " + status);
                disconnectGattServer();
                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //  log("Connected to device " + gatt.getDevice().getAddress());
                setConnected(true);
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //  log("Disconnected from device");
                disconnectGattServer();
            }
        }
    }

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void disconnectGattServer() {
        //  log("Closing Gatt connection");
        //clearLogs();
        mConnected = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }




    private Boolean connect(BluetoothDevice bdDevice) {
        Boolean bool = false;
        try {
            Log.i("Log", "service method is called ");
            Class cl = Class.forName("android.bluetooth.BluetoothDevice");
            Class[] par = {};
            Method method = cl.getMethod("createBond", par);
            Object[] args = {};
            bool = (Boolean) method.invoke(bdDevice);//, args);// this invoke creates the detected devices paired.
            //Log.i("Log", "This is: "+bool.booleanValue());
            //Log.i("Log", "devicesss: "+bdDevice.getName());
        } catch (Exception e) {
            Log.i("Log", "Inside catch of serviceFromDevice Method");
            e.printStackTrace();
        }
        return bool;
    }


    public boolean removeBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method removeBondMethod = btClass.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
        return returnValue;
    }

    public boolean createBond(BluetoothDevice btDevice)
            throws Exception
    {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue;
    }


    class ButtonClicked implements OnClickListener
    {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonOn:
                    onBluetooth();
                    break;
                case R.id.buttonSearch:
                    arrayListBluetoothDevices.clear();
                    startSearching();
                    break;
                case R.id.buttonDesc:
                    makeDiscoverable();
                    break;
                case R.id.buttonOff:
                    offBluetooth();
                    break;
                default:
                    break;
            }
        }
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Log.d(TAG, "Start Pairing... with: " + device.getName());
            device.createBond();
            try {
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(device, (Object[]) null);

                ConnectActivityLogic connectForPairing = new ConnectActivityLogic();
//                connectForPairing.onCreate();

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Pairing finished.");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myReceiver);
    }

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_COARSE_LOCATION);
        }
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_COARSE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceedDiscovery(); // --->
                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    protected void proceedDiscovery() {

        bluetoothAdapter.startDiscovery();
        Bluetooth.this.getSystemService(BLUETOOTH_SERVICE);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(myReceiver, filter);

    }

    @RequiresApi(api = VERSION_CODES.KITKAT)
    private void startSearching() {

        Log.i("Log", "in the start searching method");
        checkLocationPermission();

        proceedDiscovery();

        //Working perfect
        /*IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);*/

        /* // To start the bluetooth settings page
        startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        */

/*         //For Higher API.. > 15
        IntentFilter intentForPin = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            intentForPin = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        }
        registerReceiver(myReceiver, intentForPin);*/

    }
    private void onBluetooth() {
        if(!bluetoothAdapter.isEnabled())
        {
            //bluetoothAdapter.enable(); //Perfect Code - Turns on Bluetooth without as
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Log.i("Log", "Bluetooth is Enabled");
        }
    }
    private void offBluetooth() {
        if(bluetoothAdapter.isEnabled())
        {
            bluetoothAdapter.disable();
        }
    }
    private void makeDiscoverable() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
        Log.i("Log", "Discoverable ");
    }
    class HandleSearch extends Handler
    {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 111:

                    break;

                default:
                    break;
            }
        }
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device)
            throws IOException {
       /* if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
                return (BluetoothSocket) m.invoke(device, getSerialPortUUID());
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }*/
        return  device.createRfcommSocketToServiceRecord(getSerialPortUUID());
    }


    private void openDeviceConnection(BluetoothDevice aDevice)
            throws IOException {
        InputStream aStream = null;
        InputStreamReader aReader = null;
        try {
            /*mSocket = aDevice
                    .createInsecureRfcommSocketToServiceRecord( getSerialPortUUID() );*/
//            mSocket = createBluetoothSocket(aDevice);
            String UUID = Arrays.toString(aDevice.getUuids());

//            mSocket = aDevice.createRfcommSocketToServiceRecord(getSerialPortUUID());
            /*UUID localUUID = java.util.UUID.fromString("c2f696f8-8d5f-4180-9eb9-0d1872278bcb");
            mSocket = aDevice.createRfcommSocketToServiceRecord(localUUID);*/
            String deviceAddress = aDevice.getAddress();

            aDevice = bluetoothAdapter.getRemoteDevice(deviceAddress);

            Handler handler = new Handler(Looper.getMainLooper());
            final BluetoothDevice finalADevice = aDevice;
            handler.post(new Runnable() {
                @Override
                public void run() {

                    if (finalADevice != null) {

                      //  mGatt = finalADevice.connectGatt(getApplicationContext(), true, mGattCallback);
                        //scanLeDevice(false);// will stop after first device detection
                    }
                }
            });

            mSocket = aDevice.createInsecureRfcommSocketToServiceRecord(getSerialPortUUID());

            if (mSocket != null) {
                mSocket.connect();
                aStream = mSocket.getInputStream();
                aReader = new InputStreamReader( aStream );
                mBufferedReader = new BufferedReader( aReader );
            }

            String aString = mBufferedReader.readLine();

        } catch ( IOException e ) {
            Log.e( TAG, "Could not connect to device" + e.getMessage() + " - - - -", e );
            close( mBufferedReader );
            close( aReader );
            close( aStream );
            close( mSocket );
            throw e;
        }
    }

    private void close(Closeable aConnectedObject) {
        if ( aConnectedObject == null ) return;
        try {
            aConnectedObject.close();
        } catch ( IOException e ) {
        }
        aConnectedObject = null;
    }

    private UUID getSerialPortUUID() {
        return UUID.fromString( UUID_SERIAL_PORT_PROFILE );
    }
}
