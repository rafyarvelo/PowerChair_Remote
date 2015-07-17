package com.smart.powerchair_remote;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.Display;
import android.widget.Toast;

import com.smart.powerchair_remote.TelemetryBridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import static android.view.View.inflate;
//https://www.youtube.com/watch?v=4P5j0wWnEE0&list=PLQrQKDQmvSfxEmYOugNkYLSEs5oLxs5u6&index=7
//Tutorial 7
//Failing bc it cant find the ID bc activity is not associated with xml page? try moving telementry bridge to main??
//View not set up


//This is the Main Activity
public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                   TelemetryFragment.OnFragmentInteractionListener,
                   RemoteFragment.OnRemoteInteractionListener,
                   HelpFragment.OnHelpSelectedListener,
                   GoogleMapsFragment.OnFragmentInteractionListener,
                   AdapterView.OnItemClickListener
        {

    private final int SMART_REMOTE = 0;
    private final int TM_STREAM    = 1;
    private final int MAP_SCREEN   = 2;
    private final int HELP_SCREEN  = 3;

    private static final int SUCCESS_CONNECT = 0;
    private static final int MESSAGE_READ = 1;
    public TelemetryData tmData;
    TelemetryBridge tmBridge;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;
    Button newConnection;
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
    };


    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();


        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        tmBridge = new TelemetryBridge();
        connected    = false;
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();
        initializeBluetooth();
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
    }

    //can have button call this
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



    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment objFragment = null;

        //Switch between screens here
        switch (position)
        {
            case SMART_REMOTE:
                objFragment = RemoteFragment.newInstance(tmBridge);
                break;
            case TM_STREAM:
                objFragment = TelemetryFragment.newInstance(tmBridge);
                break;
            case HELP_SCREEN:
                objFragment = new HelpFragment();
                break;
            case MAP_SCREEN:
                objFragment = GoogleMapsFragment.newInstance(tmBridge);
                ((GoogleMapsFragment) objFragment).setUpMapIfNeeded();
                break;
            default:
                objFragment = PlaceholderFragment.newInstance(position + 1);
                break;
        }
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, objFragment)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number + 1) {
            case SMART_REMOTE:
                mTitle = getString(R.string.title_remote);
                break;
            case TM_STREAM:
                mTitle = getString(R.string.title_tm_stream);
                break;
            case HELP_SCREEN:
                mTitle = getString(R.string.title_help);
                break;
            case MAP_SCREEN:
                mTitle = getString(R.string.title_google_maps);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void onHelpButtonClicked(View v) {
        onNavigationDrawerItemSelected(HELP_SCREEN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTelemetryFragmentInteraction()
    {
        TelemetryFragment fragment = (TelemetryFragment)
                getSupportFragmentManager().findFragmentById(R.id.telemetryFragment);

        if (fragment != null)
            fragment.updateTelemetryFields();
    }

    @Override
    public void onRemoteInteraction(RemoteFragment rf)
    {
        System.out.println("Starting Remote Fragment...");
    }

    @Override
    public void onMapInteraction(Uri uri)
    {
        System.out.println("Starting Map Fragment...");
    }

            @Override
    public void onHelpInteraction(Uri uri)
    {
        System.out.println("Starting Help Fragment...");
    }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(btAdapter.isDiscovering()){
                    btAdapter.cancelDiscovery();
                }
                if(listAdapter.getItem(position).contains("Paired")){
                    BluetoothDevice selectedDevice = devices.get(position);
                    ConnectThread connect = new ConnectThread(selectedDevice);
                    connect.start();
                }
                else{
                    Toast.makeText(getApplicationContext(), "This device is not paired", Toast.LENGTH_SHORT).show();
                }
            }

            /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
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

                /** Will cancel an in-progress connection, and close the socket */
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

                /* Call this from the main activity to send data to the remote device */
                public void write(byte[] bytes) {
                    try {
                        mmOutStream.write(bytes);
                    } catch (IOException e) { }
                }

                /* Call this from the main activity to shutdown the connection */
                public void cancel() {
                    try {
                        mmSocket.close();
                    } catch (IOException e) { }
                }
            }
}
