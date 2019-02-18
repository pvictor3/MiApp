package com.contenidofoo1.miapp;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class BleDeviceAdapter extends RecyclerView.Adapter<BleDeviceAdapter.ViewHolder> {

    public interface OnItemClickListener{
        void onItemClickListener(BluetoothDevice bluetoothDevice);
    }

    private ArrayList<BluetoothDevice> mBleDeviceList;
    private OnItemClickListener listener;

    public BleDeviceAdapter( OnItemClickListener listener){
        super();
        mBleDeviceList = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_device_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.nameText.setText(mBleDeviceList.get(i).getName());
        viewHolder.addressText.setText(mBleDeviceList.get(i).getAddress());
        viewHolder.bind(mBleDeviceList.get(i), listener);
    }

    @Override
    public int getItemCount() {
        return mBleDeviceList.size();
    }

    public void addDevice(BluetoothDevice device){
        if(!mBleDeviceList.contains(device)){
            mBleDeviceList.add(device);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView nameText;
        public TextView addressText;

        public ViewHolder(View v){
            super(v);
            nameText = v.findViewById(R.id.device_name_text_list);
            addressText = v.findViewById(R.id.address_device_text_list);
        }

        public void bind(final BluetoothDevice device, final OnItemClickListener listener){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClickListener(device);
                }
            });
        }

    }
}
