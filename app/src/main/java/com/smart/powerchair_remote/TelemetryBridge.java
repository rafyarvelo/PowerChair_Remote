package com.smart.powerchair_remote;

import java.util.*;
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
    String deviceName = "RNBT-4DF6";
    //final String deviceName = "Rafy-PC-0";

    public TelemetryData tmData;
    private boolean connected = false;
    private boolean dataSent;
    private boolean dataReceived;
    BluetoothAdapter btAdapter;
    BluetoothDevice bluetoothDevice = null;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;

    static TelemetryBridge ptr = null;
    ArrayList availableDevices;

    static TelemetryBridge Instance()
    {
        //TelemetryBridge ptr = null;

        if (ptr == null)
        {
            ptr = new TelemetryBridge();
        }

        return ptr;
    }


    private TelemetryBridge()
    {
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();

        //connected = false;
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
            if(bluetoothDevice != null)
            {
                mmSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                mmSocket.connect();
                if(mmSocket.isConnected()){
                    connected = true;
                }
                mmOutputStream = mmSocket.getOutputStream();
            }
            else
            {
                connected = false;
            }
        } catch (IOException e) {
            connected = false;
            e.printStackTrace();
        }
        return connected;
    }

    public void disconnect() throws IOException {
        if(mmSocket.isConnected())
        {
            mmSocket.close();
            if(!mmSocket.isConnected())
            {
                connected = false;
            }
        }
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

    public String GetDevice(){
        return deviceName;
    }

    public void SetDevice(String device){
        deviceName = device;
    }

    public ArrayList GetAvailableDevices()
    {
        availableDevices = new ArrayList();

        Set<BluetoothDevice> devices = btAdapter.getDefaultAdapter().getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                availableDevices.add(device.getName());
            }
        }
        return availableDevices;
    }



    public void sendDataToPairedDevice(String message){
        try {
            mmOutputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Get Raw Buffer
    public byte[] getDataFromPairedDevice() throws IOException {
        InputStream mmInputStream;
        mmInputStream = mmSocket.getInputStream();
        byte[] buffer = new byte[1024];

        try{
            if(mmInputStream.available()>0)
                mmInputStream.read(buffer);
        } catch (IOException e){
            e.printStackTrace();
        }
        /*try {
            mmInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return buffer;
    }
}