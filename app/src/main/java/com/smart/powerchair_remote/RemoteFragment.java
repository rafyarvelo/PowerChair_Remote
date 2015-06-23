//http://obviam.net/index.php/a-very-basic-the-game-loop-for-android/

package com.smart.powerchair_remote;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.smart.powerchair_remote.RemoteFragment.OnRemoteInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RemoteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RemoteFragment extends android.support.v4.app.Fragment{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2, textView3, textView4, textView5;

    JoyStickClass js;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RemoteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RemoteFragment newInstance(String param1, String param2) {
        RemoteFragment fragment = new RemoteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RemoteFragment() {
        // Required empty public constructor

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

   @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.fragment_remote, container, false);

       textView1 = (TextView)view.findViewById(R.id.textView1);
       textView2 = (TextView)view.findViewById(R.id.textView2);
       textView3 = (TextView)view.findViewById(R.id.textView3);
       textView4 = (TextView)view.findViewById(R.id.textView4);
       textView5 = (TextView)view.findViewById(R.id.textView5);

       layout_joystick = (RelativeLayout)view.findViewById(R.id.layout_joystick);

       js = new JoyStickClass(getActivity().getApplicationContext()
               , layout_joystick, R.drawable.image_button);
       js.setStickSize(150, 150);
       js.setLayoutSize(500, 500);
       js.setLayoutAlpha(150);
       js.setStickAlpha(100);
       js.setOffset(90);
       js.setMinimumDistance(50);

       layout_joystick.setOnTouchListener(new View.OnTouchListener() {
           public boolean onTouch(View arg0, MotionEvent arg1) {
               js.drawStick(arg1);
               if(arg1.getAction() == MotionEvent.ACTION_DOWN
                       || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                   textView1.setText("X : " + String.valueOf(js.getX()));
                   textView2.setText("Y : " + String.valueOf(js.getY()));
                   textView3.setText("Angle : " + String.valueOf(js.getAngle()));
                   textView4.setText("Distance : " + String.valueOf(js.getDistance()));

                   int direction = js.get8Direction();
                   if(direction == JoyStickClass.STICK_UP) {
                       textView5.setText("Direction : Up");
                   } else if(direction == JoyStickClass.STICK_UPRIGHT) {
                       textView5.setText("Direction : Up Right");
                   } else if(direction == JoyStickClass.STICK_RIGHT) {
                       textView5.setText("Direction : Right");
                   } else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
                       textView5.setText("Direction : Down Right");
                   } else if(direction == JoyStickClass.STICK_DOWN) {
                       textView5.setText("Direction : Down");
                   } else if(direction == JoyStickClass.STICK_DOWNLEFT) {
                       textView5.setText("Direction : Down Left");
                   } else if(direction == JoyStickClass.STICK_LEFT) {
                       textView5.setText("Direction : Left");
                   } else if(direction == JoyStickClass.STICK_UPLEFT) {
                       textView5.setText("Direction : Up Left");
                   } else if(direction == JoyStickClass.STICK_NONE) {
                       textView5.setText("Direction : Center");
                   }
               } else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                   textView1.setText("X :");
                   textView2.setText("Y :");
                   textView3.setText("Angle :");
                   textView4.setText("Distance :");
                   textView5.setText("Direction :");
               }
               return true;
           }

       });

       return view;
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
        public void onRemoteInteraction(Uri uri);
    }
}
