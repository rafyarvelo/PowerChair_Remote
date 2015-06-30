package com.smart.powerchair_remote;

/**
 * Created by Rafy on 6/25/2015.
 */
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.test.mock.MockContext;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TextAutoSize {

       static void resizeRelative(TextView v, Display d, double percentage)
       {
           Point size = new Point();
           int width;
           int height;

           d.getSize(size);
           width  = size.x;
           height = size.y;

           v.setText(Float.toString(width));
       }
}
