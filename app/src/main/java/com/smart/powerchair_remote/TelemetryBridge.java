package com.smart.powerchair_remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

public class TelemetryBridge extends Activity
{
    //for debugging purposes
    private static final String TAG = "Telemetry Bridge";
    private static final boolean D = true;

    //Messages that are sent from bluetooth handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Name of connected device
    private String mConnectedDeviceName = null;

    //String buffer for outgoing data
    private StringBuffer mOutStringBuffer;

    //Local Bluetooth Adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    //instance of Bluetooth Service
    private Bluetooth bluetooth = null;

    String chairDirection;

    //member
    public TelemetryData tmData;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;

    public TelemetryBridge(Context context)
    {
        connected    = false;
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            Toast.makeText(context, "No Bluetooth Available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    public void onStart(){
        super.onStart();

        if(D) Log.e(TAG, "++ On Start ++");

        //If BT is off prompt user to enable
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else{
            if(bluetooth == null){
                setup();
            }
        }
    }

    public synchronized void onResume(){
        super.onResume();
        if(D) Log.e(TAG, "+ On Resume +");

        //In the case that bluetooth was not enabled during onStart, check that it is enabled now
        //Called when ACTION_REQUEST_ENABLE returns

        if(bluetooth!=null){
            //hasn't been started only if state is STATE_NONE
            if(bluetooth.getState() == Bluetooth.STATE_NONE){
                bluetooth.start();
            }
        }
    }

    private void setup(){
        //send the BT_Message depending on the jsDirection
        sendBtMessage(chairDirection);

        //initialize bluetooth to perform connections
        bluetooth = new Bluetooth(this, mHandler);

        //initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void setDirection(String direction){
        chairDirection = direction;
    }

    public void onPause(){
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    public void onStop(){
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    public void onDestroy(){
        super.onDestroy();

        // Stop Bluetooth
        if(bluetooth != null){
            bluetooth.stop();
            if(D) Log.e(TAG, "--- ON DESTROY ---");
        }
    }

    private void ensureDiscoverable(){
        if(D) Log.d(TAG, "ensure discoverable");
        if(mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendBtMessage(String message) {
        //make sure bluetooth is connected
        if(bluetooth.getState() != Bluetooth.STATE_CONNECTED){
            Toast.makeText(this, "Bluetooth not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        //check that there is content to be sent
        if(message.length() > 0){
            byte[] sendMessage = message.getBytes();
            bluetooth.write(sendMessage);

            mOutStringBuffer.setLength(0);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //save this data
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(D) Log.d(TAG, " onActivityResult "+ resultCode);
        switch(requestCode){
        case REQUEST_CONNECT_DEVICE:
            //when a device to connect to is returned
            if(resultCode == Activity.RESULT_OK){
                //Get MAC
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                //Get bluetooth obj
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                //Attempt to connect
                bluetooth.connect(device);
            }
            break;
        case REQUEST_ENABLE_BT:
            //request to enable bluetooth is returned
            if(resultCode == Activity.RESULT_OK){
                //Bluetooth is now enable so setup
                setup();
            } else{
                //Bluetooth was not enabled or error
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}
