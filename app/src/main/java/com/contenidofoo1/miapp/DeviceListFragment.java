package com.contenidofoo1.miapp;


import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceListFragment extends Fragment {
    public static final String DEVICE_NAME = "nombre del dispositivo";
    public static final String DEVICE_ADDRESS = "direccion del dispositivo";

    private RecyclerView recyclerView;
    private BleDeviceAdapter bleDeviceAdapter;
    private RecyclerView.LayoutManager layoutManager;

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
            }
        });
        recyclerView.setAdapter(bleDeviceAdapter);

        String name = getArguments().getString(DEVICE_NAME);
        String address = getArguments().getString(DEVICE_ADDRESS);

        return view;
    }

}
