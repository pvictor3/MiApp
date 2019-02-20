package com.contenidofoo1.miapp;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


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

    private RecyclerView recyclerView;
    private BleDeviceAdapter bleDeviceAdapter;
    private RecyclerView.LayoutManager layoutManager;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler();
    private boolean mScanning;

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
            }
        });
        recyclerView.setAdapter(bleDeviceAdapter);

        String name = getArguments().getString(DEVICE_NAME);
        String address = getArguments().getString(DEVICE_ADDRESS);

        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        startScan();
        return view;
    }

    private void startScan(){
        if(!hasPermissions() && mScanning){
            return;
        }

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

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d(TAG, "Device discovered");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String name = device.getName();
                    Log.d(TAG, "run: " + name);
                    bleDeviceAdapter.addDevice(device);
                    bleDeviceAdapter.notifyDataSetChanged();
                }
            });
        }
    };

}
