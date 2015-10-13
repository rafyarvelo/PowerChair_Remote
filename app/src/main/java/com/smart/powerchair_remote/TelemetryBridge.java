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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class TelemetryBridge
{

    final String deviceName = "raspberrypi-0";
    final byte[] buffer = new byte[1024];

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

    public SmartDataTypes.TMFrame getDataFromPairedDevice()
    {
        BytesUtil byte2object = new BytesUtil();
        byte[] bytesReceived = new byte[1024];
        try{
            //implement compareTo
            mmInputStream.read(buffer);
            if(buffer.length > 0)
            {
                for (byte b : buffer) {
                    bytesReceived[b] = buffer[b];
                }
                Object bytesObject = byte2object.toObject(bytesReceived);
                SmartDataTypes.TMFrame tmFrame = (SmartDataTypes.TMFrame) bytesObject;

                if(tmFrame.MsgId.compareTo(new SmartDataTypes.MsgIdType("BLT!")) == 0)
                {
                    return tmFrame;
                }
                return null;
            }
            else
            {
                return null;
            }
        } catch (IOException e){
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}