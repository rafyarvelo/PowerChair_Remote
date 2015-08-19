/*
package com.smart.powerchair_remote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

*/
/**
 * Created by asand_000 on 8/8/2015.
 *//*

public class Bluetooth {
    //These lines are used for debugging pruposes
    private static final String TAG = "Bluetooth";
    private static final boolean D = true;

    //name for server socket record
    private static final String NAME = "Bluetooth Remote";

    //UUID
    private static final UUID App_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //Members
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    //Indicate state bluetooth is in
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    //constructor that prepares new bluetooth session
    public Bluetooth(Context context, Handler handler){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    //Set sessions current state
    private synchronized void setState(int state){
        if(D) Log.d(TAG, "setState()" + mState + "->" + state);
        mState = state;

        //Update UI
        mHandler.obtainMessage(TelemetryBridge.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    //get seesions current state
    public synchronized int getState(){
        return mState;
    }

    //Start bluetooth service and begin listening for input
    public synchronized void start(){
        if (D) Log.d(TAG, "start");

        //cancel Threads trying to create connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //cancel a currently running connection
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //start listening to socket
        if(mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        setState(STATE_LISTEN);
    }

    //ConnectThread initiates connection to another device
    public synchronized void connect(BluetoothDevice device){
        if(D) Log.d(TAG, "connect to: "+device);

        //cancel another thread trying to connect
        if(mState == STATE_CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }

            //cancel thread curently connected
            if(mConnectedThread != null){
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            //Start thread to connect to device
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }
    }

    //Start the connectThread which manages connection
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        if(D) Log.d(TAG, "connected");

        //cancel a connected thread
        if(mConnectThread != null)
        {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        //cancel thread running connection
        if(mConnectedThread != null)
        {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        //cancel accepted thread so only one device is connected to
        if(mAcceptThread != null)
        {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        //Start thread to manage connection
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //send name of connected device to display on UI
        Message msg = mHandler.obtainMessage(TelemetryBridge.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(TelemetryBridge.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    //stop all threads
    public synchronized void stop(){
        if(D) Log.d(TAG, "stop");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
        if (mAcceptThread != null) {mAcceptThread.cancel(); mAcceptThread = null;}
        setState(STATE_NONE);
    }

    //write to connected thread unsynchronized
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread localThread;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            localThread = mConnectedThread;
        }
        // Perform the write unsynchronized
        localThread.write(out);
    }

    //notify UI if connection failed
    private void connectionFailed(){
        setState(STATE_LISTEN);

        //Sends message to activity
        Message msg = mHandler.obtainMessage(TelemetryBridge.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TelemetryBridge.TOAST, "Unable to connect");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //notify UI if connection is lost
    private void connectionLost() {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(TelemetryBridge.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TelemetryBridge.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    //Thread runs while listening for incoming connections
    //Will stop running once connection is accepted or cancelled
    private class AcceptThread extends Thread{
        //local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmpSocket = null;

            //creates the new socket to listen too
            try{
                tmpSocket = mAdapter.listenUsingRfcommWithServiceRecord(NAME, App_UUID);
            } catch (IOException e){
                Log.e(TAG, "listen() failed", e);
            }

            mmServerSocket = tmpSocket;
        }

        public void run(){
            if(D) Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            //Listen to server socket if not connected
            while(mState != STATE_CONNECTED){
                try{
                    //Blocking call; Only return connection if successful otherwise exception
                    socket = mmServerSocket.accept();
                } catch (IOException e){
                    Log.e(TAG, "accept() failed", e);
                    break;
                }
            }

            //case if connection was accepted
            if(socket != null)
            {
                synchronized (Bluetooth.this){
                    switch (mState){
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            //start the connected thread
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            //not ready or previously connected and socket should terminate
                            try{
                                socket.close();
                            } catch (IOException e){
                                Log.e(TAG, "Could not close socket", e);
                            }
                            break;
                    }
                }
            }
            if(D)Log.i(TAG, "End mAccept Thread");
        }

        public void cancel(){
            if(D) Log.d(TAG, "cancel "+this);
            try{
                mmServerSocket.close();
            } catch (IOException e){
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }
    */
/**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     *//*

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(App_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                Bluetooth.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (Bluetooth.this) {
                mConnectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    */
/**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     *//*

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(TelemetryBridge.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
        */
/**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         *//*

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(TelemetryBridge.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
*/
