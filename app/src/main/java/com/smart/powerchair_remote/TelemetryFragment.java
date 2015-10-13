package com.smart.powerchair_remote;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;


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
    private OnFragmentInteractionListener mListener;
    private View mView;
    private TextView tvGrndSpeed, tvAltitude, tvLatitude, tvLongitude, tvRngToObj;
    private TextView tvLEDFwdFreq,tvLEDRightFreq,tvLEDLeftFreq,tvLEDBackFreq, tvTelemetryTitle, tvTmStatusBar;
    private GridLayout tvGridLayout;
    private boolean populated, connected;

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
        tmBridge = new TelemetryBridge();
        connected    = tmBridge.GetConnected();
        if(!connected)
        {
            tmBridge.connect();
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

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override public void run() {
                updateTelemetryFields();
            }
        }, 1000, 500);

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

    public void updateTelemetryFields()
    {
        SmartDataTypes.TMFrame tmFrameData = tmBridge.getDataFromPairedDevice();
        if (connected && tmFrameData!=null)
        {
            tvGrndSpeed.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.groundSpeed));
            tvAltitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.altitude));
            tvLatitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.latitude));
            tvLongitude.setText(Float.toString(tmFrameData.brsFrame.sensorData.gpsData.longitude));
            //think about range
            tvRngToObj.setText(Float.toString(tmFrameData.brsFrame.sensorData.rangeFinderData.rangeFront));
            tvLEDFwdFreq.setText(Float.toString(tmFrameData.ledForward.frequency));
            tvLEDRightFreq.setText(Float.toString(tmFrameData.ledRight.frequency));
            tvLEDLeftFreq.setText(Float.toString(tmFrameData.ledLeft.frequency));
            tvLEDBackFreq.setText(Float.toString(tmFrameData.ledBackward.frequency));
        }
        else if(!connected)
        {
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
