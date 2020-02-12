package com.campionpumps.webservices.qrcode.random;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.lang.reflect.Method;
import java.util.Observable;

public class ConnectActivityLogic extends Fragment
{
    // The connection to the device, if we are connected.
    private BluetoothGatt mGatt;

    // This is used to allow GUI fragments to subscribe to state change notifications.
    public static class StateObservable extends Observable
    {
        private void notifyChanged() {
            setChanged();
            notifyObservers();
        }
    };

    // When the logic state changes, State.notifyObservers(this) is called.
    public final StateObservable State = new StateObservable();

    public ConnectActivityLogic()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);

        // Actually set it in response to ACTION_PAIRING_REQUEST.
        final IntentFilter pairingRequestFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        pairingRequestFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
        getActivity().getApplicationContext().registerReceiver(mPairingRequestRecevier, pairingRequestFilter);

        // Update the UI.
        State.notifyChanged();

        // Note that we don't actually need to request permission - all apps get BLUETOOTH and BLUETOOTH_ADMIN permissions.
        // LOCATION_COARSE is only used for scanning which I don't need (MAC is hard-coded).

        // Connect to the device.
        connectGatt();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // Disconnect from the device if we're still connected.
        disconnectGatt();

        // Unregister the broadcast receiver.
        getActivity().getApplicationContext().unregisterReceiver(mPairingRequestRecevier);
    }

    // The state used by the UI to show connection progress.
    public ConnectionState getConnectionState()
    {
        return mState;
    }

    // Internal state machine.
    public enum ConnectionState
    {
        IDLE,
        CONNECT_GATT,
        DISCOVER_SERVICES,
        READ_CHARACTERISTIC,
        FAILED,
        SUCCEEDED,
    }
    private ConnectionState mState = ConnectionState.IDLE;

    // When this fragment is created it is given the MAC address and PIN to connect to.
    public byte[] macAddress()
    {
        return getArguments().getByteArray("mac");
    }
    public int pinCode()
    {
        return getArguments().getInt("pin", -1);
    }

    // Start the connection process.
    private void connectGatt()
    {
        // Disconnect if we are already connected.
        disconnectGatt();

        // Update state.
        mState = ConnectionState.CONNECT_GATT;
        State.notifyChanged();

        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(macAddress());

        // Connect!
        mGatt = device.connectGatt(getActivity(), false, mBleCallback);
    }

    private void disconnectGatt()
    {
        if (mGatt != null)
        {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
        }
    }

    // See https://android.googlesource.com/platform/external/bluetooth/bluedroid/+/master/stack/include/gatt_api.h
    private static final int GATT_ERROR = 0x85;
    private static final int GATT_AUTH_FAIL = 0x89;

    private android.bluetooth.BluetoothGattCallback mBleCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    // Connected to the device. Try to discover services.
                    if (gatt.discoverServices())
                    {
                        // Update state.
                        mState = ConnectionState.DISCOVER_SERVICES;
                        State.notifyChanged();
                    }
                    else
                    {
                        // Couldn't discover services for some reason. Fail.
                        disconnectGatt();
                        mState = ConnectionState.FAILED;
                        State.notifyChanged();
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    // If we try to discover services while bonded it seems to disconnect.
                    // We need to debond and rebond...

                    switch (mState)
                    {
                        case IDLE:
                            // Do nothing in this case.
                            break;
                        case CONNECT_GATT:
                            // This can happen if the bond information is incorrect. Delete it and reconnect.
                            deleteBondInformation(gatt.getDevice());
                            connectGatt();
                            break;
                        case DISCOVER_SERVICES:
                            // This can also happen if the bond information is incorrect. Delete it and reconnect.
                            deleteBondInformation(gatt.getDevice());
                            connectGatt();
                            break;
                        case READ_CHARACTERISTIC:
                            // Disconnected while reading the characteristic. Probably just a link failure.
                            gatt.close();
                            mState = ConnectionState.FAILED;
                            State.notifyChanged();
                            break;
                        case FAILED:
                        case SUCCEEDED:
                            // Normal disconnection.
                            break;
                    }
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            super.onServicesDiscovered(gatt, status);

            // Services have been discovered. Now I try to read a characteristic that requires MitM protection.
            // This triggers pairing and bonding.

           /* BluetoothGattService nameService = gatt.getService(UUIDs.NAME_SERVICE);
            if (nameService == null)
            {
                // Service not found.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
                return;
            }
            BluetoothGattCharacteristic characteristic = nameService.getCharacteristic(UUIDs.NAME_CHARACTERISTIC);
            if (characteristic == null)
            {
                // Characteristic not found.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
                return;
            }

            // Read the characteristic.
            gatt.readCharacteristic(characteristic);
            mState = ConnectionState.READ_CHARACTERISTIC;
            State.notifyChanged();*/
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                // Characteristic read. Check it is the right one.
//                if (!UUIDs.NAME_CHARACTERISTIC.equals(characteristic.getUuid()))
//                {
//                    // Read the wrong characteristic. This shouldn't happen.
//                    disconnectGatt();
//                    mState = ConnectionState.FAILED;
//                    State.notifyChanged();
//                    return;
//                }

                // Get the name (the characteristic I am reading just contains the device name).
                byte[] value = characteristic.getValue();
                if (value == null)
                {
                    // Hmm...
                }

                disconnectGatt();
                mState = ConnectionState.SUCCEEDED;
                State.notifyChanged();

                // Success! Save it to the database or whatever...
            }
            else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION)
            {
                // This is where the tricky part comes
                if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE)
                {
                    // Bonding required.
                    // The broadcast receiver should be called.
                }
                else
                {
                    // ?
                }
            }
            else if (status == GATT_AUTH_FAIL)
            {
                // This can happen because the user ignored the pairing request notification for too long.
                // Or presumably if they put the wrong PIN in.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
            }
            else if (status == GATT_ERROR)
            {
                // I thought this happened if the bond information was wrong, but now I'm not sure.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
            }
            else
            {
                // That's weird.
                disconnectGatt();
                mState = ConnectionState.FAILED;
                State.notifyChanged();
            }
        }
    };


    private final BroadcastReceiver mPairingRequestRecevier = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction()))
            {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);

                if (type == BluetoothDevice.PAIRING_VARIANT_PIN)
                {
                   // device.setPin(Util.IntToPasskey(pinCode()));
                    abortBroadcast();
                }
                else
                {
                   // L.w("Unexpected pairing type: " + type);
                }
            }
        }
    };

    public static void deleteBondInformation(BluetoothDevice device)
    {
        try
        {
            // FFS Google, just unhide the method.
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        }
        catch (Exception e)
        {
           // L.e(e.getMessage());
        }
    }
}