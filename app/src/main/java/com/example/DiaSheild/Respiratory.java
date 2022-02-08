package com.example.DiaSheild;

import static android.media.CamcorderProfile.get;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;

public class Respiratory extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor Accelerometer;
    TextView resprate;
    static android.content.Context context;

    private Handler ServiceHandler = new Handler() {

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1: {
                    Toast.makeText(Respiratory.this, "Service Stopped", Toast.LENGTH_LONG).show();
                    stopSelf();
                }break;
            }
        }
    };
    ArrayList<Float> accelValuesZ = new ArrayList<Float>();
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    //float accelValuesZ[] = new float[128];
    int index = 0;
    int k=0;
    Bundle b;
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // TODO Auto-generated method stub
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ.add(sensorEvent.values[2]);
            //accelValuesZ[index] = sensorEvent.values[2];
            Log.i("AceelZ:" , String.valueOf(accelValuesZ.get(index-1)));
            Log.i("AceelX:" , String.valueOf(accelValuesX[index]));
            Log.i("Aceely:" , String.valueOf(accelValuesY[index]));

            //Log.i("AceelZ:" , String.valueOf(accelValuesZ[index]));
            if(index >= 127){
                index = 0;
                sensorManager.unregisterListener(this);

                sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        //TextView collRespRate = findViewById(R.id.collectedRespRate);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        ServiceHandler.sendEmptyMessageDelayed(1, 30 * 1000);

    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("Respiratory-Rate");
        // You can also include some extra data.
        intent.putExtra("RespRate", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this);
        int peakcount = peakdetector(accelValuesZ);
        int resprate = peakcount*2;
        sendMessageToActivity(String.valueOf(resprate));
        super.onDestroy();
    }
    private int peakdetector(ArrayList<Float> accelZ) {
        int n = accelZ.size();
        int peakcounts = 0, maxpeaks = 0;
        Float max = Float.MIN_VALUE, min = Float.MAX_VALUE;
        ArrayList<Float> peaks = new ArrayList<Float>();
        for (int i = 1; i < n; i++) {
            Float curValue = accelZ.get(i);
            if (isPeak(accelZ, n, curValue, i - 1, i + 1)) {
                peakcounts++;
                peaks.add(curValue);
                if (max < curValue)
                    max = curValue;
                if (min > curValue)
                    min = curValue;
            }
        }
        // Algorithm to maintain certain threshold to remove noise distraction

//        Collections.sort(peaks);
//        if(peaks.size() >0) {
//            if ((max - min) > 2 * (peaks.get(peaks.size() / 2) - min)) {
//                max = peaks.get(peaks.size() / 2);
//                maxpeaks = peakcounts / 2 +1;
//                if (max - min > 2 * (peaks.get(peaks.size() / 4) - min)) {
//                    maxpeaks += peakcounts/4 + 1;
//                }
//            }else{
//                maxpeaks = peakcounts;
//            }
//        }
//        peakcounts = peakcounts = maxpeaks;
        System.out.println(peakcounts);
        return peakcounts;
    }

//        int n = accelZ.size(), up =1, k=-1, possiblePeak = 0, Rpeak = 0, Qpeak = 0, Speak = 0,PeakType = 0;
//        ArrayList<Integer> Rpeak_index = new ArrayList<>();
//        Float sum=0.0F, max=-1000.0F, min= 1000.0F;
//        for( int i=1;i< n; i++) {
//            Float currvalue = accelZ.get(i);
//            sum = sum + currvalue;
//            if(max<currvalue)
//                max = currvalue;
//            if(min>currvalue)
//                min = currvalue;
//
//        }
//        Float Base = sum/n;
//        Float PreviousPeak=accelZ.get(1);
//        Float DynamicRangeUp = max - Base;
//        Float DynamicRangeDown = Base - min;
//        Float thresholdUp = 0.0002F*DynamicRangeUp;
//        Float thresholdR = 0.5F*DynamicRangeUp;
//        Float thresholdDown = 1.5F*DynamicRangeDown;
//        Float thresholdQ = 0.1F*DynamicRangeDown;
//        ArrayList<Integer> peak_index = new ArrayList<Integer>();
//
//        max=-1000.0F;
//        min= 1000.0F;
//         int i= 1;
//         while (i < accelZ.size()) {
//             Float curr = accelZ.get(i);
////             if (curr > max)
////                 max = accelZ.get(i);
////             if (curr < min)
////                 min = curr;
//             if (up == 1)
//             {
//                 if (curr < (max)) {
//                     if (possiblePeak == 0)
//                         possiblePeak = i;
//                     if (curr < (max - thresholdUp)) {
//                         k = k + 1;
//                         peak_index.add(possiblePeak - 1);
//                         min = curr;
//                         up = 0;
//                         possiblePeak = 0;
//                         if (PeakType == 0) {
//                             if (accelZ.get(peak_index.get(k)) > Base + thresholdR) {
//                                 Rpeak = Rpeak + 1;
//                                 Rpeak_index.add(peak_index.get(k));
//                                 PreviousPeak = accelZ.get(peak_index.get(k));
//                             }
//                         } else {
//                             Float temp= accelZ.get(peak_index.get(k)) - PreviousPeak;
//                             if(temp<0.0F)
//                                 temp = -temp;
//                             if (((temp /PreviousPeak) > 1.5) && (accelZ.get((peak_index.get(k))) > Base + thresholdR))
//                                 Rpeak = Rpeak + 1;
//                             Rpeak_index.add(peak_index.get(k));
//                             PreviousPeak = accelZ.get(peak_index.get(k));
//                             PeakType = 2;
//                         }
//                     }
//                 }else{
//                     if(curr > min){
//                         if(possiblePeak == 0)
//                             possiblePeak = i;
//                     }
//                     if(curr > (min + thresholdDown)){
//                         k = k + 1;
//                         peak_index.add(possiblePeak-1);
//                         max = curr;
//                         up = 1;
//                         possiblePeak = 0;
//                     }
//                 }
//             }
//             i=i+1;
//         }
//         ArrayList<Float> peak_values = new ArrayList<Float>();
//         for( int j=0; j< peak_index.size();j++){
//
//             peak_values.add(accelZ.get(peak_index.get(j)));
//         }
//        return peak_values.size();



    static boolean isPeak(ArrayList<Float> arr, int size, float value,
                          int prev, int next)
    {
        // If curr value is smaller than the element
        // on the left (if exists)
        if (prev >= 0 && arr.get(prev) >= value)
        {
            return false;
        }
        // If curr value  is smaller than the element
        // on the right (if exists)
        if (next < size && arr.get(next) >= value)
        {
            return false;
        }
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        //k = 0;
        return START_NOT_STICKY;
        //return START_STICKY;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
       // Toast.makeText(this, "Accuracy cahnged", Toast.LENGTH_LONG).show();

    }
}