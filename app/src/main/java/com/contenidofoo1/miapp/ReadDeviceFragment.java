package com.contenidofoo1.miapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static android.content.Context.BIND_AUTO_CREATE;

public class ReadDeviceFragment extends Fragment {
    private static final String TAG = "ReadFragment";
    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    private String mDeviceAddress;
    private boolean mConnected;
    private List<BluetoothGattCharacteristic> characteristics;
    private BluetoothGattCharacteristic counterCharacteristic;
    private HashMap<String,String> charValues;

    private BluetoothLeService mBluetoothLeService;

    private TextView counterTextView;
    private TextView brandTextView;
    private TextView deviceNumberTextView;
    private EditText newValueEditText;
    private Button saveButton;
    private Button updateButton;
    private Switch valveSwitch;

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
                charValues = new HashMap<>();
                BluetoothGattService service = mBluetoothLeService.getGattService();
                characteristics = service.getCharacteristics();
                for(BluetoothGattCharacteristic characteristic : characteristics){
                    if(characteristic.getUuid().toString().equals(BluetoothLeService.COUNTER_UUID)){
                        Log.d(TAG, "onReceive: Properties of Counter " + characteristic.getProperties());
                        counterCharacteristic = characteristic;
                    }
                }
                requestCharacteristic();
            }else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)){
                Log.d(TAG, "onReceive: ACTION_DATA_AVAILABLE");
                Log.d(TAG, "onReceive: " + intent.getStringExtra(BluetoothLeService.UUID_DATA)
                        + " : " + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                charValues.put(intent.getStringExtra(BluetoothLeService.UUID_DATA),
                        intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                characteristics.remove(characteristics.size() - 1 );
                if(characteristics.size() > 0){
                    requestCharacteristic();
                }else{
                    Toast.makeText(context, "LECTURA COMPLETA DE CHARACTERISTICAS", Toast.LENGTH_LONG).show();
                    actualizarUI();
                    enableNotify();
                }
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
        counterTextView = view.findViewById(R.id.count_text_read_device);
        brandTextView = view.findViewById(R.id.brand_text_read_device);
        deviceNumberTextView = view.findViewById(R.id.number_text_read_device);
        newValueEditText = view.findViewById(R.id.new_value_edit_text);
        saveButton = view.findViewById(R.id.save_button_read_device);
        updateButton = view.findViewById(R.id.update_button_read_device);
        valveSwitch = view.findViewById(R.id.valve_switch_read_device);

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
        Log.d(TAG, "onPause: ");
        super.onPause();
        getActivity().unregisterReceiver(mGattUpdateReceiver);

    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
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

    private void requestCharacteristic(){
        mBluetoothLeService.readCharacteristic(characteristics.get(characteristics.size() - 1));
    }

    private void actualizarUI(){
        counterTextView.setText(charValues.get(BluetoothLeService.COUNTER_UUID));
        brandTextView.setText(charValues.get(BluetoothLeService.BRAND_UUID));
        deviceNumberTextView.setText(charValues.get(BluetoothLeService.DEVICENUMBER_UUID));
        if(charValues.get(BluetoothLeService.VALVE_UUID).equals("Abierta")){
            valveSwitch.setChecked(true);
        }

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue = newValueEditText.getText().toString();
                byte[] message = new byte[0];
                try{
                    message = newValue.getBytes("UTF-8");
                }catch (UnsupportedEncodingException e){
                    Log.e(TAG, "onClick: Failed to convert message string to byte array");
                }
                counterCharacteristic.setValue(message);
                Log.d(TAG, "onClick: Escribiendo en caracteristica " + newValue);
                mBluetoothLeService.writeCharacteristic(counterCharacteristic);
            }
        });
    }

    private void enableNotify(){
        counterCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        /*BluetoothGattDescriptor descriptor = counterCharacteristic.getDescriptor(convertFromInteger(0x2902));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        Log.d(TAG, "enableNotify: " + mBluetoothLeService.writeDescriptor(descriptor));*/
        mBluetoothLeService.setCharacteristicNotification(counterCharacteristic, true);
    }

    public UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

}
