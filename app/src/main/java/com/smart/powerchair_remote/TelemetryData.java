package com.smart.powerchair_remote;

/**
 * Created by Rafy on 6/29/2015.
 */
//Structure to Hold Data from SMART Electronics
public class TelemetryData
{
   private static final int longSize  = 8;
   private static final int wordSize  = 4;
   private static final int shortSize = 2;

    public RawData       rawData;
    public ConvertedData convertedData;

    class RawData
    {
       private Byte[] groundSpeed;
       private Byte[] altitude;
       private Byte[] latitude;
       private Byte[] longitude;
       private Byte[] rangeToObject;
       private Byte[] LED_Forward_Freq;
       private Byte[] LED_Right_Freq;
       private Byte[] LED_Left_Freq;
       private Byte[] LED_Backward_Freq;

      public RawData()
      {
          groundSpeed       = createByteArray(shortSize);
          altitude          = createByteArray(shortSize);
          latitude          = createByteArray(shortSize);
          longitude         = createByteArray(shortSize);
          rangeToObject     = createByteArray(shortSize);
          LED_Forward_Freq  = createByteArray(shortSize);
          LED_Right_Freq    = createByteArray(shortSize);
          LED_Left_Freq     = createByteArray(shortSize);
          LED_Backward_Freq = createByteArray(shortSize);
      }

      private Byte[] createByteArray(int size)
      {
          Byte[] byteArray = new Byte[size];
          for (int i = 0; i < size; i++)
              byteArray[i] = new Byte((byte) 0);

          return byteArray;
      }

        public Byte[] getGroundSpeed() {
            return groundSpeed;
        }

        public void setGroundSpeed(Byte[] groundSpeed) {
            this.groundSpeed = groundSpeed;
        }

        public Byte[] getAltitude() {
            return altitude;
        }

        public void setAltitude(Byte[] altitude) {
            this.altitude = altitude;
        }

        public Byte[] getLatitude() {
            return latitude;
        }

        public void setLatitude(Byte[] latitude) {
            this.latitude = latitude;
        }

        public Byte[] getLongitude() {
            return longitude;
        }

        public void setLongitude(Byte[] longitude) {
            this.longitude = longitude;
        }

        public Byte[] getRangeToObject() {
            return rangeToObject;
        }

        public void setRangeToObject(Byte[] rangeToObject) {
            this.rangeToObject = rangeToObject;
        }

        public Byte[] getLED_Forward_Freq() {
            return LED_Forward_Freq;
        }

        public void setLED_Forward_Freq(Byte[] LED_Forward_Freq) {
            this.LED_Forward_Freq = LED_Forward_Freq;
        }

        public Byte[] getLED_Right_Freq() {
            return LED_Right_Freq;
        }

        public void setLED_Right_Freq(Byte[] LED_Right_Freq) {
            this.LED_Right_Freq = LED_Right_Freq;
        }

        public Byte[] getLED_Left_Freq() {
            return LED_Left_Freq;
        }

        public void setLED_Left_Freq(Byte[] LED_Left_Freq) {
            this.LED_Left_Freq = LED_Left_Freq;
        }

        public Byte[] getLED_Backward_Freq() {
            return LED_Backward_Freq;
        }

        public void setLED_Backward_Freq(Byte[] LED_Backward_Freq) {
            this.LED_Backward_Freq = LED_Backward_Freq;
        }
    }

    class ConvertedData
    {
        private float groundSpeed;
        private float altitude;
        private float latitude;
        private float longitude;
        private float rangeToObject;
        private float LED_Forward_Freq;
        private float LED_Right_Freq;
        private float LED_Left_Freq;
        private float LED_Backward_Freq;

        public ConvertedData()
        {
            groundSpeed       = 0;
            altitude          = 0;
            latitude          = 0;
            longitude         = 0;
            rangeToObject     = 0;
            LED_Forward_Freq  = 0;
            LED_Right_Freq    = 0;
            LED_Left_Freq     = 0;
            LED_Backward_Freq = 0;
        }

        public float getGroundSpeed() {
            return groundSpeed;
        }

        public void setGroundSpeed(float groundSpeed) {
            this.groundSpeed = groundSpeed;
        }

        public float getAltitude() {
            return altitude;
        }

        public void setAltitude(float altitude) {
            this.altitude = altitude;
        }

        public float getLatitude() {
            return latitude;
        }

        public void setLatitude(float latitude) {
            this.latitude = latitude;
        }

        public float getLongitude() {
            return longitude;
        }

        public void setLongitude(float longitude) {
            this.longitude = longitude;
        }

        public float getRangeToObject() {
            return rangeToObject;
        }

        public void setRangeToObject(float rangeToObject) {
            this.rangeToObject = rangeToObject;
        }

        public float getLED_Forward_Freq() {
            return LED_Forward_Freq;
        }

        public void setLED_Forward_Freq(float LED_Forward_Freq) {
            this.LED_Forward_Freq = LED_Forward_Freq;
        }

        public float getLED_Right_Freq() {
            return LED_Right_Freq;
        }

        public void setLED_Right_Freq(float LED_Right_Freq) {
            this.LED_Right_Freq = LED_Right_Freq;
        }

        public float getLED_Left_Freq() {
            return LED_Left_Freq;
        }

        public void setLED_Left_Freq(float LED_Left_Freq) {
            this.LED_Left_Freq = LED_Left_Freq;
        }

        public float getLED_Backward_Freq() {
            return LED_Backward_Freq;
        }

        public void setLED_Backward_Freq(float LED_Backward_Freq) {
            this.LED_Backward_Freq = LED_Backward_Freq;
        }
    }

    public TelemetryData()
    {
    }
}
