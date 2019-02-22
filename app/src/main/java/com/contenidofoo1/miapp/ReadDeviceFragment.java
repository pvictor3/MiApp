package com.contenidofoo1.miapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;

public class ReadDeviceFragment extends Fragment {
    private static final String TAG = "ReadFragment";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    private String mDeviceAddress;
    private boolean mConnected;

    private BluetoothLeService mBluetoothLeService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if(!mBluetoothLeService.initialize()){
                Log.e(TAG, "onServiceConnected: Unable to initialize Bluetooth");
                getActivity().finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)){
                mConnected = true;
            }else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)){
                mConnected = false;
            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)){
                BluetoothGattService service = mBluetoothLeService.getGattService();
                for(BluetoothGattCharacteristic characteristic : service.getCharacteristics()){
                    Log.d(TAG, "onReceive: Characteristic UUID " + characteristic.getUuid());
                }

            }else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                Log.d(TAG, "onReceive: ACTION_DATA_AVAILABLE");
            }
        }
    };

    public ReadDeviceFragment() {
        // Required empty public constructor
    }

    public static ReadDeviceFragment newInstance() {
        ReadDeviceFragment fragment = new ReadDeviceFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_read_device, container, false);

        mDeviceAddress = getArguments().getString(DeviceListFragment.DEVICE_ADDRESS);
        Log.d(TAG, "onCreateView: Device addresss  " + mDeviceAddress);

        Intent gattServiceIntent = new Intent(getContext(), BluetoothLeService.class);
        getActivity().bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        Log.d(TAG, "onResume: BroadcastReceiver registrado!!!");
        if(mBluetoothLeService != null){
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "onResume: Connect request result = " + result);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);
        getActivity().unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void disconnectGattServer(){
        mConnected = false;
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

    }

}
