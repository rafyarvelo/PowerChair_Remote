package com.smart.powerchair_remote;

/**
 * Created by Rafy on 6/29/2015.
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.smart.powerchair_remote.TelemetryData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class TelemetryBridge
{

    final String deviceName = "Aaron-0";

    public TelemetryData tmData;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;
    BluetoothAdapter btAdapter;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    public TelemetryBridge()
    {
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();
        connected = false;
    }

    //Try to Establish Connection with SMART Electronics
    public boolean connect()
    {
        Set<BluetoothDevice> devices = btAdapter.getDefaultAdapter().getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                if (deviceName.equals(device.getName())) {
                    bluetoothDevice = device;

                    break;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

        try {
            mmSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            if(mmSocket.isConnected()){
                connected = true;
            }
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }

        return connected;
    }

    private boolean GetData()
    {
        return dataReceived;
    }

    private boolean SendData()
    {
        return dataSent;
    }

    public boolean GetConnected(){
        return connected;
    }

    public void sendDataToPairedDevice(String message){
        try {
            mmOutputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}