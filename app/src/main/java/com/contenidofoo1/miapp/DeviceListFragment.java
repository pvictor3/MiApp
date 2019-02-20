package com.contenidofoo1.miapp;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment {
    public static final String DEVICE_NAME = "nombre del dispositivo";
    public static final String DEVICE_ADDRESS = "direccion del dispositivo";
    public static final int REQUEST_ENABLE_BT = 1000;
    public static final int REQUEST_FINE_LOCATION = 1001;
    public static final int SCAN_PERIOD = 10000;
    private static final String TAG = "blelist";
    private static final String SERVICE_UUID = "32c64438-23c1-40e3-8a85-2dddc120e432";

    private RecyclerView recyclerView;
    private BleDeviceAdapter bleDeviceAdapter;
    private RecyclerView.LayoutManager layoutManager;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler();
    private boolean mScanning;
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mbluetoothLeScanner;
    private Handler mHandler;

    public static DeviceListFragment newInstance(){
        DeviceListFragment fragment = new DeviceListFragment();
        return fragment;
    }

    public DeviceListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_device_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_device_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        bleDeviceAdapter = new BleDeviceAdapter(new BleDeviceAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(BluetoothDevice bluetoothDevice) {
                //Abrir nueva activity
                Toast.makeText(getContext(), "Conectando al dispositivo seleccionado", Toast.LENGTH_SHORT).show();
                Bundle args = new Bundle();
                args.putString(PlaceHolderFragment.SECTION_TITLE, "Nuevo Fragment");
                Fragment fragment = PlaceHolderFragment.newInstance("Nuevo Fragment");
                fragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                fragmentTransaction.replace(R.id.main_content,fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            }
        });
        recyclerView.setAdapter(bleDeviceAdapter);

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        startScan();
        return view;
    }

    private void startScan(){
        if(!hasPermissions() && mScanning){
            return;
        }

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(UUID.fromString(SERVICE_UUID)))
                .build();
        //filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mScanCallback = new BtleScanCallback();
        mbluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mbluetoothLeScanner.startScan(filters, settings, mScanCallback);
        mScanning = true;
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    private boolean hasPermissions(){
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return false;
        }else if(!hasLocationPermission()){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            return false;
        }
        return true;
    }

    private boolean hasLocationPermission(){
        return ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void stopScan(){
        if(mScanning && bluetoothAdapter != null && bluetoothAdapter.isEnabled() && mbluetoothLeScanner != null)
        {
            mbluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
    }

    private void scanComplete(){
        Log.d(TAG, "scanComplete: Escaner finalizado");
    }

    private class BtleScanCallback extends ScanCallback{
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bleDeviceAdapter.addDevice(result.getDevice());
            bleDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult result : results){
                bleDeviceAdapter.addDevice(result.getDevice());
                bleDeviceAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "onScanFailed: BLE Scan Failed with code" + errorCode);
        }
    }

}
