package com.smart.powerchair_remote;

/**
 * Created by Rafy on 6/29/2015.
 */

import com.smart.powerchair_remote.TelemetryData;

public class TelemetryBridge
{
    public TelemetryData tmData;
    private boolean connected;
    private boolean dataSent;
    private boolean dataReceived;

    public TelemetryBridge()
    {
        connected    = false;
        dataSent     = false;
        dataReceived = false;
        tmData       = new TelemetryData();
    }

    //Try to Establish Connection with SMART Electronics
    public boolean connect()
    {
        return connected;
    }

    private boolean GetData()
    {
        return dataReceived;
    }

    private boolean SendData()
    {
        return dataSent;
    }
}