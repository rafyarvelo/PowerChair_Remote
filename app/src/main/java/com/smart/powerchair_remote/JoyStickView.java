package com.smart.powerchair_remote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by asand_000 on 6/15/2015.
 */
public class JoyStickView extends SurfaceView implements SurfaceHolder.Callback{

    backgroundThread thread;
    Paint paint;
    float x,y;
    boolean run = true;

    public JoyStickView(Context context) {
        super(context);

        initialize();

    }

    public JoyStickView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initialize();
    }

    public JoyStickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        initialize();
    }

    public void initialize(){
        thread = new backgroundThread(getHolder(), this);
        getHolder().addCallback(this);
    }

    protected void onDraw(Canvas canvas, float x, float y)
    {
        super.onDraw(canvas);
        //canvas.drawRGB(0,255,0);
        canvas.drawText(Float.toString(x), 20, 20, paint);
        canvas.drawText(Float.toString(y), 20, 100, paint);
    }

        //What?? Video 2 1:30
    public class backgroundThread extends AsyncTask<Void, Void, Void>{
        SurfaceHolder surfaceHolder;
        JoyStickView joyStickView;
        public backgroundThread(SurfaceHolder sHolder, JoyStickView jsView){
            surfaceHolder = sHolder;
            joyStickView = jsView;
            jsView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    x=event.getX();
                    y=event.getY();
                    return true;
                }
            });
        }
        @Override
        protected Void doInBackground(Void... params) {
            while (run) {
                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        joyStickView.onDraw(canvas, x, y);
                    }
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.execute((Void[])null);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
