package com.ourtestseries.stepcounter;


import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<BluetoothDevice> mData;
    private OnPairButtonClickListener mListener;


    private static final String UUID_SERIAL_PORT_PROFILE = "00001101-0000-1000-8000-00805f9b34fb";

    private BluetoothSocket mSocket = null;
    private BufferedReader mBufferedReader = null;

    public DeviceListAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    public void setData(List<BluetoothDevice> data) {
        mData = data;
    }

    public void setListener(OnPairButtonClickListener listener) {
        mListener = listener;
    }

    public int getCount() {
        return (mData == null) ? 0 : mData.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_device, null);

            holder = new ViewHolder();

            holder.nameTv = (TextView) convertView.findViewById(R.id.tv_name);
            holder.addressTv = (TextView) convertView.findViewById(R.id.tv_address);
            holder.pairBtn = (Button) convertView.findViewById(R.id.btn_pair);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = mData.get(position);

        holder.nameTv.setText(device.getName());
        holder.addressTv.setText(device.getAddress());
        holder.pairBtn.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Unpair" : "Pair");
        holder.nameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {


//                    UUID uuid = UUID.fromString("205ca6f1-0b4c-40b0-a168-f1bea3c2c937"); //Standard SerialPortService ID
//                    mSocket = mData.get(position).createRfcommSocketToServiceRecord(uuid);
//
//                    Log.d("msg", "OLD############" + mData.get(position).getName() + " " + mSocket.isConnected());
//
//                    mSocket.connect();
//
//                    Log.d("msg", "new############" + mData.get(position).getName() + " " + mSocket.isConnected());


                    openDeviceConnection(mData.get(position));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.pairBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPairButtonClick(position);

                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView nameTv;
        TextView addressTv;
        TextView pairBtn;
    }

    public interface OnPairButtonClickListener {
        public abstract void onPairButtonClick(int position);
    }


    private void openDeviceConnection(BluetoothDevice aDevice) throws IOException {
        final InputStream[] aStream = {null};
        final InputStreamReader[] aReader = {null};

        try {

            boolean is_co = false;
            ParcelUuid[] parcelUuids = aDevice.getUuids();
            if (parcelUuids != null) {
                ArrayList<String> uuidStrings = new ArrayList<>(parcelUuids.length);
                for (ParcelUuid parcelUuid : parcelUuids) {

                    uuidStrings.add(parcelUuid.getUuid().toString());

                    mSocket = aDevice.createRfcommSocketToServiceRecord(UUID.fromString(parcelUuid.getUuid().toString()));
                    mSocket.connect();
                    if (mSocket.isConnected()) {
                        is_co = true;
                        break;

                    }
                    Log.d("msg", "######################" + mSocket.isConnected());
                }

            } else {

                Log.d("ds", "######################nulll");

            }

            final String[] aString = {""};
            aStream[0] = mSocket.getInputStream();
            aReader[0] = new InputStreamReader(aStream[0]);
            mBufferedReader = new BufferedReader(aReader[0]);
            aString[0] += mBufferedReader.readLine();
            Log.d("ds--->", "######################" + aString[0]);

            final Handler handler = new Handler();
            if (is_co)
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            aStream[0] = mSocket.getInputStream();
                            aReader[0] = new InputStreamReader(aStream[0]);
                            mBufferedReader = new BufferedReader(aReader[0]);
                            aString[0] += mBufferedReader.readLine();
                            Log.d("ds--->", "######################" + aString[0]);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }, 5000);


        } catch (IOException e) {
            Log.e("TAG", "Could not connect to device", e);
            close(mBufferedReader);
            close(aReader[0]);
            close(aStream[0]);
            close(mSocket);
            throw e;
        }
    }

    private void close(Closeable aConnectedObject) {
        if (aConnectedObject == null) return;
        try {
            aConnectedObject.close();
        } catch (IOException e) {
        }
        aConnectedObject = null;
    }

    private UUID getSerialPortUUID() {
        return UUID.fromString(UUID_SERIAL_PORT_PROFILE);
    }
}