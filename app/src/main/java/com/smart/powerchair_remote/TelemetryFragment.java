package com.smart.powerchair_remote;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.internal.zzhl.runOnUiThread;
import static com.smart.powerchair_remote.SmartDataTypes.*;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TelemetryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TelemetryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TelemetryFragment extends android.support.v4.app.Fragment {

    private TelemetryBridge tmBridge;
    private SmartDataTypes sdt;
    private OnFragmentInteractionListener mListener;
    private View mView;
    private TextView tvGrndSpeed, tvAltitude, tvLatitude, tvLongitude, tvRngToObj;
    private TextView tvLEDFwdFreq,tvLEDRightFreq,tvLEDLeftFreq,tvLEDBackFreq, tvTelemetryTitle, tvTmStatusBar;
    private GridLayout tvGridLayout;
    private boolean populated, connected;

    public enum Emotiv_Electrodes
    {
        F3, FC6, P7, T8 , F7,
        F8 , T7 , P8, AF4, F4,
        AF3 , O2 , O1, FC5, NUM_EMOTIV_ELECTRODES
    };

//*****************************************************************************
//
// Message IDs for Communication Between Hardware
//
//*****************************************************************************

/*
 * General Message Format:
 * MESSAGE ID - 5 Bytes Unsigned (4 Characters and a Null terminator (0x00) '\0')
 * ...
 * N Bytes of Data
 *
 */

//5 Byte Message ID

    static class MsgIdType implements Comparable<MsgIdType>
    {
        public static final int MSG_ID_SIZE  = 5; //Bytes
        public byte[] id;

        public MsgIdType(String idValue) {
            id = idValue.getBytes();
        }

        @Override
        public int compareTo(MsgIdType rhs)
        {
            for (int i = 0; i < MSG_ID_SIZE; i++)
            {
                if (this.id[i] != rhs.id[i])
                {
                    return 1;
                }
            }
            return 0;
        }
    }

////BRS to Mobile Device Communication
//    public static final byte[] BRS2MD_MSG_ID  = {'B', 'L' , 'T' , '!', '\0'};
//
////Mobile Device to BRS Communication
//    public static final byte[] MD2BRS_MSG_ID  = {'T', 'L' , 'B' , '!', '\0'};

    public final MsgIdType BRS2MD_MSG_ID  = new MsgIdType("BLT!");
    public final MsgIdType MD2BRS_MSG_ID  = new MsgIdType("TLB!");

//*****************************************************************************
//
// Frame Types
//
//*****************************************************************************

    //This struct defines what a single frame of EEG Data looks like
    class EEGFrame
    {
        public static final int MAX_EEG_ELECTRODES = 14;

        //The type of EEG that the frame belongs to, appended by EEG IO class
        int eegType;

        int counter; //Value counter

        //Data from Each electrode, defaults to 0
        int[] electrodeData;

        //Contact Quality from Each electrode, defaults to 0
        short[] contactQuality;

        //Gyro Values
        byte gyroX;
        byte gyroY;

        //Percentage of Full Battery Charge
        byte batteryPercentage;

        public EEGFrame()
        {
            eegType = 0;
            counter = 0;
            electrodeData  = new int[MAX_EEG_ELECTRODES];
            contactQuality = new short[MAX_EEG_ELECTRODES];
            gyroX = 0;
            gyroY = 0;
            batteryPercentage = 0;
        }
    }

//===============================================

    //====================BRS Types====================
    class GPSData
    {
        float latitude;
        float longitude;
        float altitude;
        float groundSpeed;

        GPSData() {
            latitude = 0;
            longitude = 0;
            altitude = 0;
            groundSpeed = 0;
        }
    }
    class USData
    {
        float rangeFront;
        float rangeBack;

        USData() {
            rangeFront = 0;
            rangeBack  = 0;
        }
    }

    class SensorData
    {
        GPSData gpsData;
        USData rangeFinderData;

        SensorData() {
            gpsData          = new GPSData();
            rangeFinderData  = new USData();
        }
    }

    //Mobile Device to BRS Bluetooth Frame
    class BluetoothFrame
    {
        byte remoteCommand;

        BluetoothFrame()
        {
            remoteCommand = 'n';
        }
    }

    // A Frame of BRS Data
    class BRSFrame
    {
        MsgIdType MsgId; //Message Sent from BRS to BCI
        BluetoothFrame btFrame;
        SensorData sensorData;

        BRSFrame()
        {
            MsgId      = new MsgIdType("BRS!");
            btFrame    = new BluetoothFrame();
            sensorData = new SensorData();
        }
    }

//===============================================

//==================Flasher Types==================
    /*public enum LED_Group_ID {
        LED_FORWARD,
        LED_BACKWARD,
        LED_RIGHT,
        LED_LEFT,
        NUM_LED_GROUPS
    }*/

    class LEDGroup
    {
        public static final short LED_FORWARD_FREQ_DEFAULT  = 10;
        public static final short LED_BACKWARD_FREQ_DEFAULT = 20;
        public static final short LED_RIGHT_FREQ_DEFAULT    = 30;
        public static final short LED_LEFT_FREQ_DEFAULT     = 40;

        int id;
        short frequency;

        LEDGroup(int id, short freq)
        {
            this.id = id;
            this.frequency = freq;
        }
    }

//=================================================

    public enum BCIState
    {
        BCI_OFF,
        /*
        * 1) Remain in this state until Run() is called
        * 2) When Run() is called, Move to BCI_INITIALIZATION
        */
        BCI_INITIALIZATION,
        /*
        * 1) Create Instances of RVS, JA2BRS, BRS2JA, JA2PCC, and EEG IO
        * 2) Connect to Flasher, EEG, BRSH, and PCC
        * 3) Generate RVS Frequencies
        * 4) Send RVS to Flasher
        * 5) Send TM Packet to BRSH
        * 6) Move to BCI_STANDBY
        */
        BCI_STANDBY,
        /*
        * 1) Wait for EEG Data or Remote Commands
        * 2) Upon Receipt of EEG Data or Remote Commands, move to BCI_PROCESSING
        */
        BCI_PROCESSING,
        /*
        * 1) Process the data
        * 2) Generate PCC Command
        * 3) Move to BCI_READY
        */
        BCI_READY
 /*
 * 1) Send the Command
 * 2) Revert to BCI_STANDBY
 */
    }


    public class ProcessingResult
    {
        public char command;
        public int confidence;

        ProcessingResult()
        {
            command = 'n';
            confidence = 0;
        }
    }
    //Full Telemetry Frame
    public class TMFrame
    {
        MsgIdType MsgId; //Message Sent From BCI -> BRS -> MD
        int timeStamp;
        int bciState;
        char lastCommand;
        int lastConfidence;
        ProcessingResult processingResult;
        BRSFrame brsFrame;
        LEDGroup ledForward;
        LEDGroup ledBackward;
        LEDGroup ledRight;
        LEDGroup ledLeft;
        boolean eegConnectionStatus;
        boolean pccConnectionStatus;
        boolean brsConnectionStatus;
        boolean flasherConnectionStatus;

        TMFrame()
        {
            MsgId                   = new MsgIdType("BCI!");
            timeStamp               = 0;
            bciState                = 0;
            lastCommand             = 0;
            lastConfidence          = 0;
            processingResult        =  new ProcessingResult();
            brsFrame                = new BRSFrame();
            ledForward              = new LEDGroup(0, LEDGroup.LED_FORWARD_FREQ_DEFAULT );
            ledBackward             = new LEDGroup(1, LEDGroup.LED_BACKWARD_FREQ_DEFAULT );
            ledRight                = new LEDGroup(2, LEDGroup.LED_RIGHT_FREQ_DEFAULT );
            ledLeft                 = new LEDGroup(3, LEDGroup.LED_LEFT_FREQ_DEFAULT );
            eegConnectionStatus     = false;
            pccConnectionStatus     = false;
            brsConnectionStatus     = false;
            flasherConnectionStatus = false;
        }
    }

    public TMFrame createTMFrame()
    {
        return new TMFrame();
    }
    //###########################################
    public static TelemetryFragment newInstance(TelemetryBridge tmBridgeRef) {
        TelemetryFragment fragment = new TelemetryFragment();
        fragment.setTmBridge(tmBridgeRef);
        return fragment;
    }

    public TelemetryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tmBridge = TelemetryBridge.Instance();
        connected    = tmBridge.GetConnected();
        if(!connected)
        {
            tmBridge.connect();
            connected    = tmBridge.GetConnected();
        }

        //Optional
        if(connected) {
            System.out.println("Bluetooth Opened");
            Toast.makeText(getActivity(), "Bluetooth Opened", Toast.LENGTH_SHORT).show();
        } else{
            System.out.println("Bluetooth Not Opened");
            Toast.makeText(getActivity(), "Bluetooth Not Opened", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_telemetry, container, false);
        populated = false;
        tvTelemetryTitle = (TextView) mView.findViewById(R.id.Label);
        tvGrndSpeed    = (TextView) mView.findViewById(R.id.GrndSpdValue);
        tvAltitude     = (TextView) mView.findViewById(R.id.AltitudeValue);
        tvLatitude     = (TextView) mView.findViewById(R.id.LatitudeValue);
        tvLongitude    = (TextView) mView.findViewById(R.id.LongitudeValue);
        tvRngToObj     = (TextView) mView.findViewById(R.id.RngToObjValue);
        tvLEDFwdFreq   = (TextView) mView.findViewById(R.id.ledFwdFreq);
        tvLEDRightFreq = (TextView) mView.findViewById(R.id.ledRightFreq);
        tvLEDLeftFreq  = (TextView) mView.findViewById(R.id.ledLeftFreq);
        tvLEDBackFreq  = (TextView) mView.findViewById(R.id.ledBackFreq);

        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateTelemetryFields();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();



//        tvGridLayout = (GridLayout) mView.findViewById(R.id.gridLayout);
//        tvTmStatusBar = (TextView) mView.findViewById(R.id.gridLayout2);

//        int vWidth = mView.getWidth();
//        int vHeight = mView.getHeight();
//
//        tvGridLayout.setMinimumHeight(vHeight - tvTelemetryTitle.getHeight() - tvTmStatusBar.getHeight());

        populated = true;
        // Inflate the layout for this fragment
        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onRemoteInteraction() {
        if (mListener != null) {
            mListener.onTelemetryFragmentInteraction();
        }
    }

    public TMFrame getNextTMFrame()
    {
        TMFrame tmFrame = new TMFrame();
        byte[] buffer = tmBridge.getDataFromPairedDevice();

        int currentIndex = 0;

        for (int i = 0; i < buffer.length; i++)
        {
            //If we have enough bytes for a TM Frame, check if the Msg ID is correct
            if (buffer.length - i >= 100)
            {
                if (buffer[i]     == 'B' && buffer[i+1]   == 'L'&& buffer[i + 2] == 'T' &&
                    buffer[i + 3] == '!' && buffer[i + 4] == '\0' )
                {
                    ByteBuffer temp = ByteBuffer.wrap(buffer, i, i+99);

                    tmFrame.timeStamp = temp.getInt();
                    tmFrame.bciState  = temp.getInt();
                    tmFrame.lastCommand = temp.getChar();
                    tmFrame.lastConfidence = temp.getInt();
                    tmFrame.processingResult.command = temp.getChar();
                    tmFrame.processingResult.confidence = temp.getChar();

                    temp.getInt();
                    temp.getChar();
                    tmFrame.brsFrame.MsgId = new MsgIdType("BRS!");
                    tmFrame.brsFrame.btFrame.remoteCommand = (byte)temp.getChar();
                    tmFrame.brsFrame.sensorData.gpsData.latitude = temp.getFloat();
                    tmFrame.brsFrame.sensorData.gpsData.longitude = temp.getFloat();
                    tmFrame.brsFrame.sensorData.gpsData.altitude = temp.getFloat();
                    tmFrame.brsFrame.sensorData.gpsData.groundSpeed = temp.getFloat();
                    tmFrame.brsFrame.sensorData.rangeFinderData.rangeFront = temp.getFloat();
                    tmFrame.brsFrame.sensorData.rangeFinderData.rangeBack = temp.getFloat();
                    tmFrame.ledForward.id = temp.getInt();
                    tmFrame.ledForward.frequency = temp.getShort();
                    tmFrame.ledBackward.id = temp.getInt();
                    tmFrame.ledBackward.frequency = temp.getShort();
                    tmFrame.ledRight.id = temp.getInt();
                    tmFrame.ledRight.frequency = temp.getShort();
                    tmFrame.ledLeft.id = temp.getInt();
                    tmFrame.ledLeft.frequency = temp.getShort();
                    tmFrame.eegConnectionStatus = (temp.getChar() == 0x01) ? true : false;
                    tmFrame.pccConnectionStatus = (temp.getChar() == 0x01) ? true : false;
                    tmFrame.brsConnectionStatus = (temp.getChar() == 0x01) ? true : false;
                    tmFrame.flasherConnectionStatus = (temp.getChar() == 0x01) ? true : false;

                }
            }
        }

        return tmFrame;
    }




    public void updateTelemetryFields()
    {
        if(connected)
        {
            tmBridge.sendDataToPairedDevice("TM");//Remove me

            //Get TM frame from Bluetooth device
            final TMFrame tmFrameData = getNextTMFrame();

            if (tmFrameData != null) {
                tvGrndSpeed.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.groundSpeed));
                tvAltitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.altitude));
                tvLatitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.latitude));
                tvLongitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.longitude));
                //think about range
                tvRngToObj.setText(Float.toString(tmFrameData.brsFrame.sensorData.rangeFinderData.rangeFront));
                tvLEDFwdFreq.setText(Short.toString(tmFrameData.ledForward.frequency));
                tvLEDRightFreq.setText(Short.toString(tmFrameData.ledRight.frequency));
                tvLEDLeftFreq.setText(Short.toString(tmFrameData.ledLeft.frequency));
                tvLEDBackFreq.setText(Short.toString(tmFrameData.ledBackward.frequency));
            } else if (tmFrameData == null) {
                System.out.println("No TM Data");
                Toast.makeText(getActivity(), "No TM Data", Toast.LENGTH_LONG).show();
            }
        }   else if (!connected) {
            System.out.println("Bluetooth Not Connected TM Update");
            Toast.makeText(getActivity(), "Bluetooth Not Connected TM Update", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    public void setTmBridge(TelemetryBridge tmBridgeRef)
    {
        this.tmBridge = tmBridgeRef;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onTelemetryFragmentInteraction();
    }
}
