package com.contenidofoo1.miapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

public class ReadDeviceFragment extends Fragment {
    private static final String TAG = "ReadFragment";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    private boolean mConnected;

    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange: verificando el estado de la conexion");

            if(status == BluetoothGatt.GATT_FAILURE){
                disconnectGattServer();
                Log.d(TAG, "onConnectionStateChange: GATT_FAILURE");
                return;
            }else if(status != BluetoothGatt.GATT_SUCCESS){
                disconnectGattServer();
                Log.d(TAG, "onConnectionStateChange: GATT_NO_SUCCESS");
                return;
            }

            if(newState == BluetoothGatt.STATE_CONNECTED){
                mConnected = true;
                gatt.discoverServices();
                Log.d(TAG, "onConnectionStateChange: Intentanto descubrir servicios");

            }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                disconnectGattServer();
                Log.d(TAG, "onConnectionStateChange: STATE_DISCONNECTED");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status != BluetoothGatt.GATT_SUCCESS){
                return;
            }

            BluetoothGattService service = gatt.getService(UUID.fromString(DeviceListFragment.SERVICE_UUID));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
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

        String address = getArguments().getString(DeviceListFragment.DEVICE_ADDRESS);
        Log.d(TAG, "onCreateView: Device addresss  " + address);

    BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
    bluetoothAdapter = bluetoothManager.getAdapter();

        BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = bluetoothDevice.connectGatt(getContext(), false, mGattCallback);
        Log.d(TAG, "onCreateView: Intentanto conectarse al server GATT");



        return view;
    }

    private void disconnectGattServer(){
        mConnected = false;
        if(bluetoothGatt != null){
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }

    }

}
