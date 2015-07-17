package com.smart.powerchair_remote;

/**
 * Created by Rafy on 6/29/2015.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.powerchair_remote.TelemetryData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.logging.LogRecord;

public class TelemetryBridge
{
    /*private static final int SUCCESS_CONNECT = 0;
    private static final int MESSAGE_READ = 1;*/
    public TelemetryData tmData;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;

    /*Button newConnection;
    ArrayAdapter<String> listAdapter;
    ListView listView;
    ArrayList<String> pairedDevices;

    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;
    //might need to make our own. can generate from online
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SUCCESS_CONNECT:
                    //Do something, some kind of UI
                    ConnectedThread connectedThread = new ConnectedThread((BluetoothSocket)msg.obj);
                    Toast.makeText(getApplicationContext(), "DEVICE CONNECTED", Toast.LENGTH_SHORT).show();
                    String s = "successfully connected";
                    connectedThread.write(s.getBytes());
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[])msg.obj;
                    String string = new String(readBuf);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };*/

    public TelemetryBridge()
    {
        connected    = false;
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();
        /*initializeBluetooth();
        //checks to make sure Bluetooth compatible device is used
        if(btAdapter == null){
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            finish();
        }
        //check to make sure bluetooth is enabled
        else{
            if(!btAdapter.isEnabled()){
                //if not enabled make a request for user to enable and check to make sure the user did
                //enable and not click cancel
                turnOnBlueTooth();
            }
            getPairedDevices();
            startDiscovery();
        }
*/
    }

   /* //can have button call this
    private  void startDiscovery(){
        //cancel first so discovery doesnt start while already started
        btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    private void turnOnBlueTooth() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    private void getPairedDevices(){
        devicesArray = btAdapter.getBondedDevices();

        //check to make sure bluetooth compatible devices are found
        if(devicesArray.size()>0){
            //loop through all available devices and add to list
            for(BluetoothDevice device:devicesArray){
                pairedDevices.add(device.getName());
            }

        }
    }
    //If cancel is hit do not continue on
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_CANCELED){
            Toast.makeText(getApplicationContext(), "This application requires Bluetooth to be enabled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeBluetooth(){
        newConnection = (Button)findViewById(R.id.bConnect);
        listView = (ListView)findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = new ArrayList<String>();
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        devices = new ArrayList<BluetoothDevice>();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(BluetoothDevice.ACTION_FOUND.equals(action)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    devices.add(device);
                    String s = "";
                    for(int x = 0; x < pairedDevices.size(); x++){
                        if(device.getName().equals(pairedDevices.get(x))){
                            s="Paired";
                            break;
                        }
                    }


                    listAdapter.add(device.getName()+" "+"\n"+device.getAddress());
                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){

                }
                else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                }
                //State changed makes sure the user doesnt turn bluetooth off while app is running
                else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    if(btAdapter.getState() == btAdapter.STATE_OFF){
                        turnOnBlueTooth();
                    }
                }
            }
        };
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(receiver, filter);
    }


    protected void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }
*/
    //Try to Establish Connection with SMART Electronics
    public boolean connect()
    {
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

    /*public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
        if(btAdapter.isDiscovering()){
            btAdapter.cancelDiscovery();
        }
        if(listAdapter.getItem(arg2).contains("Paired")){
            BluetoothDevice selectedDevice = devices.get(arg2);
            ConnectThread connect = new ConnectThread(selectedDevice);
            connect.start();
        }
        else{
            Toast.makeText(getApplicationContext(), "This device is not paired", Toast.LENGTH_SHORT).show();
        }
    }

    private class ConnectThread extends Thread
{
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device) {
        // Use a temporary object that is later assigned to mmSocket,
        // because mmSocket is final
        BluetoothSocket tmp = null;
        mmDevice = device;

        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        btAdapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        mHandler.obtainMessage(SUCCESS_CONNECT, mmSocket).sendToTarget();
    }

    *//** Will cancel an in-progress connection, and close the socket *//*
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer;  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    buffer = new byte[1024];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        *//* Call this from the main activity to send data to the remote device *//*
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        *//* Call this from the main activity to shutdown the connection *//*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }*/

}
