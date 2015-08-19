//http://obviam.net/index.php/a-very-basic-the-game-loop-for-android/

package com.smart.powerchair_remote;

import com.smart.powerchair_remote.TelemetryBridge;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import com.smart.powerchair_remote.TextAutoSize;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.smart.powerchair_remote.RemoteFragment.OnRemoteInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RemoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteFragment extends android.support.v4.app.Fragment{

    private OnRemoteInteractionListener mListener;
    private Context mContext;
    private Display mDisplay;
    private View mView;
    private TelemetryBridge tmBridge;

    final byte delimiter = 33;
    int readBufferPosition = 0;

    private static final int SUCCESS_CONNECT = 0;
    private static final int MESSAGE_READ = 1;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;
    ArrayAdapter<String> listAdapter;
    ListView listView;
    ArrayList<String> pairedDevices;
    BluetoothAdapter btAdapter;
    Set<BluetoothDevice> devicesArray;
    ArrayList<BluetoothDevice> devices;
    IntentFilter filter;
    BroadcastReceiver receiver;

    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    //BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;

    String deviceName = "Aaron-0";

    BluetoothDevice bluetoothDevice = null;

    JoyStickClass js;

    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textViewX, textViewY, textViewAngle, textViewDistance, textViewDirection;

    //might need to make our own. can generate from online
    //public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // TODO: Rename and change types and number of parameters
    public static RemoteFragment newInstance(TelemetryBridge tmBridgeRef) {
        RemoteFragment fragment = new RemoteFragment();
        fragment.setTmBridge(tmBridgeRef);
        return fragment;
    }

    public RemoteFragment()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        connected    = false;
        dataSent     = false;
        dataReceived = false;



        Set<BluetoothDevice> devices = btAdapter.getDefaultAdapter().getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                if (deviceName.equals(device.getName())) {
                    bluetoothDevice = device;
                    connected = true;
                    break;
                }
                else{
                    connected = false;
                }
            }
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID

        try {
            mmSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }

        //Optional
        System.out.println("Bluetooth Opened");
        Toast.makeText(getActivity(), "Bluetooth Opened", Toast.LENGTH_SHORT).show();
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_remote, container, false);

        textViewX = (TextView)mView.findViewById(R.id.textViewX);
        textViewY = (TextView)mView.findViewById(R.id.textViewY);
        textViewAngle = (TextView)mView.findViewById(R.id.textViewAngle);
        textViewDistance = (TextView)mView.findViewById(R.id.textViewDistance);
        textViewDirection = (TextView)mView.findViewById(R.id.textViewDirection);
        layout_joystick = (RelativeLayout)mView.findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getActivity().getApplicationContext()
                , layout_joystick, R.drawable.image_button);
        js.setStickSize(150, 150);
        js.setLayoutSize(750, 750);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);


            final Handler handler = new Handler();

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    textViewX       .setText("X : " + String.valueOf(js.getX()));
                    textViewY       .setText("Y : " + String.valueOf(js.getY()));
                    textViewAngle   .setText("Angle : " + String.valueOf(js.getAngle()));
                    textViewDistance.setText("Distance : " + String.valueOf(js.getDistance()));

                    int direction = js.get8Direction();
                    if(direction == JoyStickClass.STICK_UP) {
                        if(connected) {
                            sendDataToPairedDevice("F");
                        }
                        textViewDirection.setText("Direction : Up");
                    } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                        textViewDirection.setText("Direction : Up Right");
                    } else if(direction == JoyStickClass.STICK_RIGHT) {
                        if(connected) {
                            sendDataToPairedDevice("R");
                        }
                        textViewDirection.setText("Direction : Right");
                    } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                        textViewDirection.setText("Direction : Down Right");
                    } else if(direction == JoyStickClass.STICK_DOWN) {
                        if(connected) {
                            sendDataToPairedDevice("D");
                        }
                        textViewDirection.setText("Direction : Down");
                    } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                        textViewDirection.setText("Direction : Down Left");
                    } else if(direction == JoyStickClass.STICK_LEFT) {
                        if(connected) {
                            sendDataToPairedDevice("L");
                        }
                    } else if(direction == JoyStickClass.STICK_UPLEFT) {
                        textViewDirection.setText("Direction : Up Left");
                    } else if(direction == JoyStickClass.STICK_NONE) {
                        if(connected) {
                            sendDataToPairedDevice("N");
                        }
                        textViewDirection.setText("Direction : Center");
                    }
                } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    textViewX.setText("X :");
                    textViewY.setText("Y :");
                    textViewAngle.setText("Angle :");
                    textViewDistance.setText("Distance :");
                    textViewDirection.setText("Direction :");
                }
                return true;
            }
        });

        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRemoteInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setTmBridge(TelemetryBridge tmBridgeRef)
    {
        this.tmBridge = tmBridgeRef;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnRemoteInteractionListener {
        // TODO: Update argument type and name
        public void onRemoteInteraction(RemoteFragment rf);
    }

    private void sendDataToPairedDevice(String message){


        try {
            mmOutputStream.write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}