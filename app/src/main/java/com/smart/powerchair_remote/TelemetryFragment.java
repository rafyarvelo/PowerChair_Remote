package com.smart.powerchair_remote;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
    private TextView tvLEDFwdFreq,tvLEDRightFreq,tvLEDLeftFreq,tvLEDBackFreq;
    private boolean populated;

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
        populated = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_telemetry, container, false);
        populated = false;
        tvGrndSpeed    = (TextView) mView.findViewById(R.id.GrndSpdValue);
        tvAltitude     = (TextView) mView.findViewById(R.id.AltitudeValue);
        tvLatitude     = (TextView) mView.findViewById(R.id.LatitudeValue);
        tvLongitude    = (TextView) mView.findViewById(R.id.LongitudeValue);
        tvRngToObj     = (TextView) mView.findViewById(R.id.RngToObjValue);
        tvLEDFwdFreq   = (TextView) mView.findViewById(R.id.ledFwdFreq);
        tvLEDRightFreq = (TextView) mView.findViewById(R.id.ledRightFreq);
        tvLEDLeftFreq  = (TextView) mView.findViewById(R.id.ledLeftFreq);
        tvLEDBackFreq  = (TextView) mView.findViewById(R.id.ledBackFreq);
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
        if (populated)
        {
            tvGrndSpeed.setText(Float.toString(tmBridge.tmData.convertedData.getGroundSpeed()));
            tvAltitude.setText(Float.toString(tmBridge.tmData.convertedData.getAltitude()));
            tvLatitude.setText(Float.toString(tmBridge.tmData.convertedData.getLatitude()));
            tvLongitude.setText(Float.toString(tmBridge.tmData.convertedData.getLongitude()));
            tvRngToObj.setText(Float.toString(tmBridge.tmData.convertedData.getRangeToObject()));
            tvLEDFwdFreq.setText(Float.toString(tmBridge.tmData.convertedData.getLED_Forward_Freq()));
            tvLEDRightFreq.setText(Float.toString(tmBridge.tmData.convertedData.getLED_Right_Freq()));
            tvLEDLeftFreq.setText(Float.toString(tmBridge.tmData.convertedData.getLED_Left_Freq()));
            tvLEDBackFreq.setText(Float.toString(tmBridge.tmData.convertedData.getLED_Backward_Freq()));
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
