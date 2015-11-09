package com.smart.powerchair_remote;
//Help Fragment
import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.smart.powerchair_remote.HelpFragment.OnHelpSelectedListener} interface
 * to handle interaction events.
 * Use the {@link HelpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HelpFragment extends android.support.v4.app.Fragment implements AdapterView.OnItemSelectedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Spinner spinner;
    private boolean connected;

    private OnHelpSelectedListener mListener;

    private View mView;

    private TelemetryBridge tmBridge;
    boolean firstTime = true;
    String availableDevicesArray[];

    TextView connectionLabel;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HelpFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HelpFragment newInstance(String param1, String param2) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public HelpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        tmBridge = TelemetryBridge.Instance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_help, container, false);
        firstTime = true;
        connectionLabel = (TextView) mView.findViewById(R.id.textViewConnStatus);
        if(tmBridge.GetConnected())
        {
            connectionLabel.setText("CONNECTED TO "+tmBridge.GetDevice());
            connectionLabel.setBackgroundColor(Color.GREEN);
        }
        else
        {
            connectionLabel.setText("NOT CONNECTED");
            connectionLabel.setBackgroundColor(Color.RED);
        }

        spinner = (Spinner) mView.findViewById(R.id.spinner);

        availableDevicesArray = new String[tmBridge.GetAvailableDevices().size()];

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, (String[]) tmBridge.GetAvailableDevices().toArray(availableDevicesArray));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        return mView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onHelpInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnHelpSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        if(!firstTime) {
            tmBridge.SetDevice((String) tmBridge.GetAvailableDevices().toArray()[i]);
            tmBridge = TelemetryBridge.Instance();
            connected = tmBridge.GetConnected();
            if (connected) {
                try {
                    tmBridge.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connected = tmBridge.GetConnected();
                System.out.println("Connection Closed");
                Toast.makeText(getActivity(), "Connection Closed", Toast.LENGTH_SHORT).show();
            }
            if (!connected) {
                tmBridge.connect();
                connected = tmBridge.GetConnected();
                if (connected) {
                    System.out.println("Bluetooth Opened");
                    Toast.makeText(getActivity(), "Bluetooth Opened", Toast.LENGTH_SHORT).show();
                    connectionLabel.setText("CONNECTED TO " + tmBridge.GetDevice());
                    connectionLabel.setBackgroundColor(Color.GREEN);
                } else {
                    System.out.println("Bluetooth Not Opened");
                    Toast.makeText(getActivity(), "Bluetooth Not Opened", Toast.LENGTH_SHORT).show();
                    connectionLabel.setText("NOT CONNECTED");
                    connectionLabel.setBackgroundColor(Color.RED);
                }
            }
        }
        firstTime = false;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
    public interface OnHelpSelectedListener {
        // TODO: Update argument type and name
        public void onHelpInteraction(Uri uri);
    }
}
