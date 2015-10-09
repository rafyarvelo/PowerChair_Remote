package com.smart.powerchair_remote;

/**
 * Created by asand_000 on 10/9/2015.
 */
public class SmartDataTypes
{
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
    
    class MsgIdType implements Comparable<MsgIdType>
    {
        public static final int MSG_ID_SIZE  = 5; //Bytes
        public byte[] id;
        
        public MsgIdType(String idValue) {
            id = idValue.getBytes();
        }
        
        @Override
        public int compareTo(MsgIdType rhs) 
        {
            int sum = 0;
            
            for (int i = 0; i < MSG_ID_SIZE; i++) {
                sum = this.id[i] - rhs.id[i];
            }
            
            return sum;
        }
    }
    
//BRS Frame to the BCI
//    public static final byte[] BRS2BCI_MSG_ID = {'B','R', 'S', '!','\0'};
//
////BCI TM Frame Back to the BRS
//    public static final byte[] BCI2BRS_MSG_ID = {'B','C', 'I', '!','\0'};
//
////BRS to Mobile Device Communication
//    public static final byte[] BRS2MD_MSG_ID  = {'B', 'L' , 'T' , '!', '\0'};
//
////Mobile Device to BRS Communication
//    public static final byte[] MD2BRS_MSG_ID  = {'T', 'L' , 'B' , '!', '\0'};
        
    public final MsgIdType BRS2BCI_MSG_ID = new MsgIdType("BRS!");
    public final MsgIdType BCI2BRS_MSG_ID = new MsgIdType("BCI!");
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
    public enum LED_Group_ID {
        LED_FORWARD,
        LED_BACKWARD,
        LED_RIGHT,
        LED_LEFT,
        NUM_LED_GROUPS
    }

    class LEDGroup
    {
        public static final short LED_FORWARD_FREQ_DEFAULT  = 10;
        public static final short LED_BACKWARD_FREQ_DEFAULT = 20;
        public static final short LED_RIGHT_FREQ_DEFAULT    = 30;
        public static final short LED_LEFT_FREQ_DEFAULT     = 40;

        LED_Group_ID id;
        short frequency;

        LEDGroup(LED_Group_ID id, short freq)
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

//Full Telemetry Frame
    class TMFrame
    {
        MsgIdType MsgId; //Message Sent From BCI -> BRS -> MD
        int timeStamp;
        BCIState bciState;
        EEGFrame eegFrame; //Only the Latest Frame, EEG Telemetry is managed by the C_EEG_IO class
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
            bciState                = BCIState.BCI_OFF;
            eegFrame                = new EEGFrame();
            brsFrame                = new BRSFrame();
            ledForward              = new LEDGroup(LED_Group_ID.LED_FORWARD, LEDGroup.LED_FORWARD_FREQ_DEFAULT );
            ledBackward             = new LEDGroup(LED_Group_ID.LED_BACKWARD, LEDGroup.LED_BACKWARD_FREQ_DEFAULT );
            ledRight                = new LEDGroup(LED_Group_ID.LED_RIGHT, LEDGroup.LED_RIGHT_FREQ_DEFAULT );
            ledLeft                 = new LEDGroup(LED_Group_ID.LED_LEFT, LEDGroup.LED_LEFT_FREQ_DEFAULT );
            eegConnectionStatus     = false;
            pccConnectionStatus     = false;
            brsConnectionStatus     = false;
            flasherConnectionStatus = false;
        }
    }

//###########################################
}
